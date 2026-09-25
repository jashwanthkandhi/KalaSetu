import asyncio
import io
import time
import uuid
import wave
from unittest.mock import AsyncMock
import pytest
from PIL import Image
from fastapi.testclient import TestClient
from app.main import app
from app.routes import jobs
from app.services import job_runner
from app.services.fair_pricing import CostInputs, calculate_price, missing_costs
from app.services.craft_intelligence import CraftAnalysis
from app.services.ownership import owner_digest


class MemoryStore:
    """Test double only; production has no memory persistence option."""
    def __init__(self): self.rows = {}; self.media = {}
    def get(self, job_id, owner=None):
        row = self.rows.get(job_id)
        return row if row and (owner is None or row['owner_hash'] == owner) else None
    def create(self, row, photo, audio):
        self.rows.setdefault(row['id'], dict(row, attempts=0, lease_token='lease'))
        self.media[row['id']] = (photo, audio)
        return self.rows[row['id']]
    def update(self, job, **values): self.rows[job['id']].update(values)
    def inputs(self, job): return self.media[job['id']]
    def cleanup(self, job): self.media.pop(job['id'], None)
    def retry(self, job_id, owner): self.rows[job_id]['state'] = 'QUEUED'


@pytest.fixture
def store(monkeypatch):
    value = MemoryStore()
    monkeypatch.setattr(jobs, 'job_store', value)
    return value


def media():
    image = io.BytesIO(); Image.new('RGB', (20, 20), 'brown').save(image, 'JPEG')
    audio = io.BytesIO()
    with wave.open(audio, 'wb') as output:
        output.setnchannels(1); output.setsampwidth(2); output.setframerate(16000)
        output.writeframes(b'\x00\x01' * 1600)
    return {'photo': ('pot.jpg', image.getvalue(), 'image/jpeg'), 'audio': ('voice.wav', audio.getvalue(), 'audio/wav')}


def test_accept_poll_duplicate_and_owner_isolation(store):
    client = TestClient(app)
    headers = {'X-Owner-Key': 'a' * 64, 'Idempotency-Key': str(uuid.uuid4())}
    accepted = client.post('/api/v1/listings/jobs', headers=headers, files=media(), data={'language': 'te'})
    assert accepted.status_code == 202
    assert accepted.json()['state'] == 'QUEUED'
    retry = client.post('/api/v1/listings/jobs', headers=headers, files=media(), data={'language': 'te'})
    assert retry.json()['job_id'] == accepted.json()['job_id']
    assert len(store.rows) == 1
    assert client.get(accepted.json()['poll_url'], headers=headers).json()['state'] == 'QUEUED'
    assert client.get(accepted.json()['poll_url'], headers={'X-Owner-Key': 'b' * 64}).status_code == 404
    assert client.post('/api/v1/listings/jobs', headers=headers, files=media(), data={'language': 'hi'}).status_code == 409


def test_queue_storage_outage_is_not_accepted(store, monkeypatch):
    monkeypatch.setattr(store, 'get', lambda *args: (_ for _ in ()).throw(RuntimeError('secret')))
    result = TestClient(app).post('/api/v1/listings/jobs', files=media(), data={'language': 'en'},
        headers={'X-Owner-Key': 'a' * 64, 'Idempotency-Key': str(uuid.uuid4())})
    assert result.status_code == 503 and 'secret' not in result.text


@pytest.mark.parametrize('concurrency', [5, 10])
def test_concurrent_jobs_overlap_independent_stages(store, monkeypatch, concurrency):
    active = 0; peak = 0
    async def speech(*args):
        nonlocal active, peak
        active += 1; peak = max(peak, active)
        await asyncio.sleep(.05)
        active -= 1
        return 'Clay pot'
    async def image(*args):
        nonlocal active, peak
        active += 1; peak = max(peak, active)
        await asyncio.sleep(.05)
        active -= 1
        return ('https://test/original.jpg', 'https://test/original.jpg', True)
    monkeypatch.setattr(job_runner.stt_service, 'transcribe', speech)
    monkeypatch.setattr(job_runner.image_service, 'process_and_enhance', image)
    monkeypatch.setattr(job_runner.craft_intelligence, 'analyze', AsyncMock(return_value=CraftAnalysis(category='Pottery', confidence=.8, evidence='shape')))
    monkeypatch.setattr(job_runner.llm_service, 'generate_listing', AsyncMock(side_effect=lambda *args: dict(title='Pot', description='Clay pot', tags=['clay'], category='Pottery', suggested_price=999, attributes={})))
    async def run():
        rows = [store.create(dict(id=str(uuid.uuid4()), owner_hash=owner_digest('test'), language='en', audio_suffix='.wav', state='QUEUED'), b'photo', b'audio') for _ in range(concurrency)]
        await asyncio.gather(*(job_runner.run_job(row, store) for row in rows))
    start = time.perf_counter(); asyncio.run(run()); elapsed = time.perf_counter() - start
    assert peak == concurrency * 2
    assert all(row['state'] == 'COMPLETED' for row in store.rows.values())
    assert all(row['result']['listing']['suggested_price'] == 0 for row in store.rows.values())
    assert not store.media
    print(f'{concurrency} isolated concurrent jobs: {elapsed:.3f}s; peak independent tasks={peak}')


def test_failure_retains_inputs_and_sanitizes_error(store, monkeypatch):
    monkeypatch.setattr(job_runner.stt_service, 'transcribe', AsyncMock(side_effect=TimeoutError('private secret')))
    monkeypatch.setattr(job_runner.image_service, 'process_and_enhance', AsyncMock(return_value=('url', 'url', True)))
    row = store.create(dict(id='failed', owner_hash='a'*64, language='en', audio_suffix='.wav', state='QUEUED'), b'p', b'a')
    asyncio.run(job_runner.run_job(row, store))
    assert row['state'] == 'FAILED' and 'private' not in str(row['error'])
    assert 'failed' in store.media


def test_cost_plus_no_fabricated_range():
    result = calculate_price(CostInputs(hours=3, hourly_rate=150, material_cost=200, margin_percent=30))
    assert result['labor_cost'] == 450 and result['margin'] == 195
    assert result['recommended_price'] == 845
    assert result['low'] == result['high'] == 845
    assert missing_costs()['suggested_price'] == 0


@pytest.mark.parametrize('field,value', [('hours', -1), ('hourly_rate', 0), ('material_cost', float('nan')), ('margin_percent', 201)])
def test_invalid_costs(field, value):
    data = dict(hours=1, hourly_rate=100, material_cost=20, margin_percent=10); data[field] = value
    assert TestClient(app).post('/api/v1/listings/fair-price', content=__import__('json').dumps(data), headers={'Content-Type': 'application/json'}).status_code == 422


def test_vision_cannot_assert_unverified_tradition():
    result = CraftAnalysis(category='Metalwork', tradition='Dhokra', materials=['brass'], technique='lost wax', confidence=.9, evidence='visual inference')
    assert result.tradition == 'UNKNOWN' and result.materials == []
    assert CraftAnalysis(category='Pottery', confidence=.2, evidence='uncertain').category == 'Other'
