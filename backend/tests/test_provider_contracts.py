"""Provider contract tests: no network or cloud writes."""
import asyncio
import base64
import json
from pathlib import Path
import httpx
import pytest
from unittest.mock import Mock
from app.config import settings
from app.services.tts_service import TTSService
from app.services.stt_service import STTService
from app.services.image_service import ImageService


def transport(monkeypatch, handler):
    real_client = httpx.AsyncClient
    monkeypatch.setattr(httpx, 'AsyncClient', lambda **kwargs: real_client(transport=httpx.MockTransport(handler)))
    monkeypatch.setattr(settings, 'MOCK_MODE', False)


@pytest.mark.parametrize('language', ['en', 'hi', 'te'])
def test_sarvam_tts_uses_current_contract(monkeypatch, language):
    wav = b'RIFFtestWAVEfmt '
    def handle(request):
        body = json.loads(request.content)
        assert body['text'] == 'Handmade bowl'
        assert body['language_code'] == language + '-IN'
        assert body['output_audio_codec'] == 'wav'
        assert 'inputs' not in body and 'target_language_code' not in body
        return httpx.Response(200, json={'audios': [base64.b64encode(wav).decode()]})
    transport(monkeypatch, handle)
    monkeypatch.setattr(settings, 'SARVAM_API_KEY', 'test')
    monkeypatch.setattr(settings, 'GOOGLE_APPLICATION_CREDENTIALS', '')
    assert asyncio.run(TTSService().synthesize('Handmade bowl', language)) == wav


def test_tts_provider_failure_returns_no_audio(monkeypatch):
    transport(monkeypatch, lambda request: httpx.Response(503))
    monkeypatch.setattr(settings, 'SARVAM_API_KEY', 'test')
    monkeypatch.setattr(settings, 'GOOGLE_APPLICATION_CREDENTIALS', '')
    assert asyncio.run(TTSService().synthesize('Bowl', 'en')) is None


def test_stt_preserves_m4a_type_and_requested_language(monkeypatch, tmp_path):
    audio = tmp_path / 'voice.m4a'; audio.write_bytes(b'test-audio')
    def handle(request):
        assert b'filename="voice.m4a"' in request.content
        assert b'Content-Type: audio/' in request.content
        assert b'saaras:v3' in request.content and b'hi-IN' in request.content
        return httpx.Response(200, json={'transcript': 'A bowl'})
    transport(monkeypatch, handle)
    monkeypatch.setattr(settings, 'SARVAM_API_KEY', 'test')
    assert asyncio.run(STTService().transcribe(audio, 'hi')) == 'A bowl'


def test_empty_stt_retries_then_reports_failure(monkeypatch, tmp_path):
    calls = []
    def handle(request):
        calls.append(request)
        return httpx.Response(200, json={'transcript': ''})
    transport(monkeypatch, handle)
    monkeypatch.setattr(settings, 'SARVAM_API_KEY', 'test')
    monkeypatch.setattr(settings, 'LOCAL_WHISPER_ENABLED', False)
    audio = tmp_path / 'voice.wav'; audio.write_bytes(b'audio')
    service = STTService(); service.openai_client = None
    with pytest.raises(RuntimeError): asyncio.run(service.transcribe(audio, 'en'))
    assert len(calls) == 2


def test_image_failure_preserves_exact_original(monkeypatch):
    import importlib
    module = importlib.import_module('app.services.image_service')
    content = (Path(__file__).parent / 'fixtures/pottery_sample.jpg').read_bytes()
    upload = Mock(return_value='https://storage.test/original.jpg')
    monkeypatch.setattr(module.supabase_service, 'upload_image', upload)
    monkeypatch.setattr(settings, 'QWEN_IMAGE_ENDPOINT', 'https://image.test/v1/images/edits')
    transport(monkeypatch, lambda request: httpx.Response(503))
    result = asyncio.run(ImageService().process_and_enhance(content, 'test'))
    assert result == ('https://storage.test/original.jpg', 'https://storage.test/original.jpg', True)
    assert upload.call_args.args[0] == content and upload.call_count == 1


def test_image_edit_uses_documented_json_payload(monkeypatch):
    def handle(request):
        body = json.loads(request.content)
        assert body['image'] == 'data:image/jpeg;base64,' + base64.b64encode(b'original').decode()
        assert body['response_format'] == 'b64_json'
        return httpx.Response(200, json={'data': [{'b64_json': base64.b64encode(b'enhanced').decode()}]})
    transport(monkeypatch, handle)
    monkeypatch.setattr(settings, 'QWEN_IMAGE_ENDPOINT', 'https://image.test/v1/images/edits')
    assert asyncio.run(ImageService()._call_qwen_image_api(b'original', 'test', 'id')) == b'enhanced'
