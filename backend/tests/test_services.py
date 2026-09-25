import pytest
from unittest.mock import Mock, AsyncMock
from pydantic import ValidationError
from app.config import settings
from app.schemas.listing_request import LLMListingSchema
from app.services.supabase_service import SupabaseService
from app.services.pricing_service import PricingService
from app.services.category_service import CategoryService
from app.services.llm_service import LLMService
import asyncio


def test_unconfigured_supabase_cannot_fake_success():
    service = SupabaseService(); service.client = None
    with pytest.raises(RuntimeError): service.upload_image(b'photo', 'original/a.jpg')
    with pytest.raises(RuntimeError): service.insert_product({'product_id': 'a'}, 'owner')


def test_storage_failure_cannot_return_url():
    service = SupabaseService(); service.client = Mock()
    service.client.storage.from_.return_value.upload.side_effect = RuntimeError()
    with pytest.raises(RuntimeError): service.upload_image(b'photo', 'original/a.jpg')


def test_private_contact_redacted_before_save():
    service = SupabaseService(); service.client = Mock()
    service.client.rpc.return_value.execute.return_value.data = 'a'
    data = {'product_id': 'a', 'public_profile': {'contact_public': False, 'contact': 'private'}}
    assert service.insert_product(data, 'secret') == 'a'
    submitted = service.client.rpc.call_args.args[1]
    assert 'contact' not in submitted['payload']['public_profile']
    assert submitted['owner'] != 'secret'


def test_missing_classifier_has_zero_confidence(monkeypatch):
    monkeypatch.setattr(settings, 'MOCK_MODE', False)
    service = CategoryService(); service._load_attempted = True
    assert service.predict(b'invalid') == {'name': 'Other', 'confidence': 0.0}


def test_price_fallback_discloses_estimate(monkeypatch):
    monkeypatch.setattr(settings, 'SERPAPI_KEY', '')
    result = PricingService().market_guidance('Wood', ['carved'], 1234)
    assert result['suggested_price'] == 1234 and result['source'] == 'ai_estimate'
    assert result['low'] is None and result['comparables'] == []


def test_live_price_rejects_foreign_currency(monkeypatch):
    import app.services.pricing_service as unused
    monkeypatch.setattr(settings, 'MOCK_MODE', False); monkeypatch.setattr(settings, 'SERPAPI_KEY', 'test')
    response = Mock(); response.json.return_value = {'shopping_results': [
        {'extracted_price': 100, 'price': '₹100', 'title': 'A'}, {'extracted_price': 300, 'price': 'INR300', 'title': 'B'},
        {'extracted_price': 9999, 'price': '$9999', 'title': 'C'}]}
    monkeypatch.setattr('httpx.get', Mock(return_value=response))
    result = PricingService().market_guidance('Wood', ['carved'], 1234)
    assert result['suggested_price'] == 200 and result['low'] == 100 and result['high'] == 300


def test_no_llm_client_never_returns_mock(monkeypatch):
    monkeypatch.setattr(settings, 'MOCK_MODE', False)
    service = LLMService(); service.nim_client = None; service.openai_client = None
    with pytest.raises(RuntimeError): asyncio.run(service.generate_listing('clay pot', 'Pottery', 'en'))


def test_llm_malformed_retried_exactly_once(monkeypatch):
    monkeypatch.setattr(settings, 'MOCK_MODE', False)
    service = LLMService(); service.nim_client = Mock(); service.openai_client = None
    completion = Mock(); completion.choices = [Mock()]; completion.choices[0].message.content = 'not json'
    service.nim_client.chat.completions.create = AsyncMock(return_value=completion)
    with pytest.raises(RuntimeError): asyncio.run(service.generate_listing('clay pot', 'Pottery', 'en'))
    assert service.nim_client.chat.completions.create.await_count == 2


def test_configured_llm_fallback_after_primary_failure(monkeypatch):
    import json
    monkeypatch.setattr(settings, 'MOCK_MODE', False)
    service = LLMService(); service.nim_client = Mock(); service.openai_client = Mock()
    service.nim_client.chat.completions.create = AsyncMock(side_effect=RuntimeError('provider unavailable'))
    listing = dict(title='Wood bowl', description='Hand-carved bowl.', category='Wood', tags=['wood'], suggested_price=500)
    response = Mock(); response.choices = [Mock()]; response.choices[0].message.content = json.dumps(listing)
    service.openai_client.chat.completions.create = AsyncMock(return_value=response)
    actual = asyncio.run(service.generate_listing('Wood bowl', 'Wood', 'en'))
    assert actual['title'] == 'Wood bowl'
    assert service.nim_client.chat.completions.create.await_count == 2
    assert service.openai_client.chat.completions.create.await_count == 1
