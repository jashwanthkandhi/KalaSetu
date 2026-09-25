"""Isolated contract tests: no live provider or database writes."""
import io
import wave
import uuid
from unittest.mock import AsyncMock, Mock
import pytest
from PIL import Image
from fastapi.testclient import TestClient
from app.main import app
from app.config import settings
from app.services import stt_service, image_service, category_service, llm_service, pricing_service, supabase_service, tts_service
from app.schemas.listing_request import LLMListingSchema

client = TestClient(app)
OWNER = {'X-Owner-Key': 'test-installation-capability-32-characters'}

@pytest.fixture
def listing():
    return dict(title='Clay water pot', description='Handmade clay pot.', category='Pottery',
                tags=['clay', 'handmade'], suggested_price=450, attributes={'material': 'clay'})

@pytest.fixture
def media():
    image = io.BytesIO()
    Image.new('RGB', (30, 30), 'brown').save(image, 'JPEG')
    audio = io.BytesIO()
    with wave.open(audio, 'wb') as wav:
        wav.setnchannels(1); wav.setsampwidth(2); wav.setframerate(16000)
        wav.writeframes(b'\x00\x01' * 1600)
    return {'photo': ('pot.jpg', image.getvalue(), 'image/jpeg'), 'audio': ('voice.wav', audio.getvalue(), 'audio/wav')}

@pytest.fixture(autouse=True)
def isolated(monkeypatch, listing):
    monkeypatch.setattr(settings, 'MOCK_MODE', False)
    monkeypatch.setattr(stt_service, 'transcribe', AsyncMock(return_value='Handmade clay pot'))
    monkeypatch.setattr(image_service, 'process_and_enhance', AsyncMock(return_value=('https://storage.test/original.jpg', 'https://storage.test/original.jpg', True)))
    monkeypatch.setattr(category_service, 'predict', Mock(return_value={'name': 'Pottery', 'confidence': .7}))
    monkeypatch.setattr(llm_service, 'generate_listing', AsyncMock(return_value=listing.copy()))
    monkeypatch.setattr(llm_service, 'revise_listing', AsyncMock(return_value=listing.copy()))
    monkeypatch.setattr(pricing_service, 'market_guidance', Mock(return_value={'suggested_price': 450, 'source': 'ai_estimate', 'low': None, 'high': None, 'comparables': [], 'explanation': 'No live data'}))
    monkeypatch.setattr(supabase_service, 'insert_product', Mock(side_effect=lambda data, owner: data['product_id']))
    monkeypatch.setattr(supabase_service, 'own_products', Mock(return_value=[]))
    monkeypatch.setattr(supabase_service, 'discover', Mock(return_value=[]))
    monkeypatch.setattr(supabase_service, 'delete_product', Mock(return_value=True))
    monkeypatch.setattr(tts_service, 'synthesize', AsyncMock(return_value=b'RIFFtestWAVEdata'))

@pytest.fixture
def payload(listing):
    return dict(listing, product_id=str(uuid.uuid4()), original_image_url='https://storage.test/original.jpg',
        final_price=500, public_profile={'display_name': 'Artisan', 'contact': '1234567890', 'contact_public': False})


def test_health():
    assert client.get('/health').json()['mock_mode'] is False

@pytest.mark.parametrize('language', ['te', 'hi', 'en'])
def test_pipeline(media, language):
    response = client.post('/api/v1/listings/process', files=media, data={'language': language})
    assert response.status_code == 200
    body = response.json()
    assert body['success'] and body['image_warning']
    assert body['market_data']['source'] == 'ai_estimate'
    assert body['listing']['attributes']['material'] == 'clay'
    assert llm_service.generate_listing.call_args.args[0] == 'Handmade clay pot'

@pytest.mark.parametrize('field,value', [('language', 'xx'), ('language', ''), ('language', None)])
def test_language_validation(media, field, value):
    assert client.post('/api/v1/listings/process', files=media, data={field: value}).status_code == 422

@pytest.mark.parametrize('part,mime,content', [('photo', 'image/jpeg', b'not an image'), ('photo', 'text/plain', b'text'),
    ('audio', 'audio/wav', b''), ('audio', 'audio/wav', b'not audio'), ('audio', 'text/plain', b'bad')])
def test_invalid_media(media, part, mime, content):
    media[part] = ('file.jpg' if part == 'photo' else 'voice.wav', content, mime)
    assert client.post('/api/v1/listings/process', files=media, data={'language': 'en'}).status_code == 422

@pytest.mark.parametrize('part,size', [('photo', 10 * 1024 * 1024 + 1), ('audio', 5 * 1024 * 1024 + 1)])
def test_upload_limits(media, part, size):
    old = media[part]; media[part] = (old[0], b'x' * size, old[2])
    assert client.post('/api/v1/listings/process', files=media, data={'language': 'en'}).status_code == 413

@pytest.mark.parametrize('service,method,code', [(stt_service, 'transcribe', 'stt_failed'),
    (llm_service, 'generate_listing', 'llm_failed'), (image_service, 'process_and_enhance', 'processing_failed')])
@pytest.mark.parametrize('error', [TimeoutError, ConnectionError, ValueError])
def test_provider_failures_preserve_failure(media, service, method, code, error):
    getattr(service, method).side_effect = error('private details must not leak')
    response = client.post('/api/v1/listings/process', files=media, data={'language': 'en'})
    assert response.status_code == 503 and response.json()['error']['code'] == code
    assert 'private details' not in response.text


def test_confirm_and_retry_are_same_id(payload):
    for _ in range(2):
        response = client.post('/api/v1/listings/confirm', headers=OWNER, json=payload)
        assert response.status_code == 200 and response.json()['product_id'] == payload['product_id']

@pytest.mark.parametrize('field,value', [('title', ''), ('title', 'x' * 81), ('description', ' '),
    ('description', 'x' * 401), ('category', 'Invented'), ('tags', []), ('tags', [' ']),
    ('final_price', -1), ('final_price', 0), ('final_price', 50001), ('suggested_price', 'NaN'),
    ('product_id', 'not-a-uuid'), ('original_image_url', 'file:///private/photo')])
def test_confirm_validation(payload, field, value):
    payload[field] = value
    assert client.post('/api/v1/listings/confirm', headers=OWNER, json=payload).status_code == 422


def test_owner_required(payload):
    assert client.post('/api/v1/listings/confirm', json=payload).status_code == 422
    assert client.get('/api/v1/listings').status_code == 422
    assert client.delete('/api/v1/listings/' + payload['product_id']).status_code == 422


def test_save_failure(payload):
    supabase_service.insert_product.side_effect = RuntimeError('database unavailable')
    assert client.post('/api/v1/listings/confirm', headers=OWNER, json=payload).status_code == 503

@pytest.mark.parametrize('path,method', [('/api/v1/listings', 'own_products'), ('/api/v1/listings/discover', 'discover')])
def test_reads(path, method):
    assert client.get(path, headers=OWNER).json() == {'products': []}
    getattr(supabase_service, method).side_effect = TimeoutError()
    assert client.get(path, headers=OWNER).status_code == 503

@pytest.mark.parametrize('query', ['limit=0', 'limit=101', 'offset=-1'])
def test_discovery_bounds(query):
    assert client.get('/api/v1/listings/discover?' + query).status_code == 422


def test_delete(payload):
    path = '/api/v1/listings/' + payload['product_id']
    assert client.delete(path, headers=OWNER).json() == {'success': True}
    supabase_service.delete_product.return_value = False
    assert client.delete(path, headers=OWNER).status_code == 404
    supabase_service.delete_product.side_effect = RuntimeError()
    assert client.delete(path, headers=OWNER).status_code == 503


def test_assist(listing):
    request = dict(listing=listing, instruction='Improve title', language='hi')
    assert client.post('/api/v1/listings/assist', json=request).json()['listing']['title'] == listing['title']
    llm_service.revise_listing.side_effect = ValueError()
    assert client.post('/api/v1/listings/assist', json=request).status_code == 503
    request['instruction'] = ''
    assert client.post('/api/v1/listings/assist', json=request).status_code == 422


def test_voice_edit(media):
    response = client.post('/api/v1/listings/voice-edit', files={'audio': media['audio']}, data={'language': 'en'})
    assert response.json()['transcript'] == 'Handmade clay pot'
    stt_service.transcribe.side_effect = TimeoutError()
    assert client.post('/api/v1/listings/voice-edit', files={'audio': media['audio']}, data={'language': 'en'}).status_code == 503


def test_tts():
    response = client.post('/api/v1/tts', json={'text': 'Clay pot', 'language': 'en'})
    assert response.status_code == 200 and response.headers['content-type'] == 'audio/wav'
    tts_service.synthesize.return_value = None
    assert client.post('/api/v1/tts', json={'text': 'Clay pot'}).status_code == 503

@pytest.mark.parametrize('body', [{'text': ''}, {'text': '   '}, {'text': 'x' * 501}, {'text': 'a', 'language': 'xx'}, {}])
def test_tts_validation(body):
    assert client.post('/api/v1/tts', json=body).status_code == 422


def test_missing_files_wrong_content_type():
    assert client.post('/api/v1/listings/process', json={}).status_code == 422
    assert client.post('/api/v1/listings/voice-edit', json={}).status_code == 422
