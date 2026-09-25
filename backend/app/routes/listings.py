import asyncio
import logging
import uuid
from pathlib import Path
from uuid import UUID
from fastapi import APIRouter, Depends, File, Form, Header, HTTPException, Query, UploadFile
from fastapi.responses import JSONResponse
from ..schemas.listing_request import ConfirmListingRequest, AssistRequest
from ..services import stt_service, image_service, category_service, llm_service, pricing_service, supabase_service
from ..utils.file_utils import save_temp, cleanup_files
from ..utils.validation import validate_media_files, MAX_PHOTO_SIZE, MAX_AUDIO_SIZE

logger = logging.getLogger('kalasetu.listings')
from ..services.ownership import owner_key
router = APIRouter(prefix='/api/v1/listings', tags=['Listings'])


def failure(code, message, status=503, request_id=None):
    return JSONResponse(status_code=status, content={'success': False,
        'request_id': request_id or str(uuid.uuid4()), 'error': {'code': code, 'message': message}})


@router.post('/process')
async def process_listing(photo: UploadFile = File(...), audio: UploadFile = File(...), language: str = Form(...)):
    request_id = str(uuid.uuid4())
    paths = []
    try:
        photo_bytes = await photo.read(MAX_PHOTO_SIZE + 1)
        audio_bytes = await audio.read(MAX_AUDIO_SIZE + 1)
        valid, code, message = validate_media_files(photo_bytes, photo.content_type, photo.filename,
            audio_bytes, audio.content_type, audio.filename, language)
        if not valid:
            return failure(code, message, 413 if code == 'file_too_large' else 422, request_id)
        suffix = Path(audio.filename or 'audio.m4a').suffix.lower()
        audio_path = save_temp(audio_bytes, suffix=suffix)
        paths.append(audio_path)
        try:
            transcript = await stt_service.transcribe(audio_path, language)
            if not transcript.strip():
                raise ValueError('Empty transcript')
        except Exception:
            return failure('stt_failed', 'Could not understand the recording. Please record again.', 503, request_id)
        original, enhanced, warning = await image_service.process_and_enhance(photo_bytes, request_id)
        category = await asyncio.to_thread(category_service.predict, photo_bytes)
        try:
            listing = await llm_service.generate_listing(transcript, category['name'], language)
        except Exception:
            return failure('llm_failed', 'Listing could not be generated. Your recording is safe; try again.', 503, request_id)
        market = await asyncio.to_thread(pricing_service.market_guidance,
            listing['category'], listing['tags'], listing['suggested_price'])
        listing['suggested_price'] = market['suggested_price']
        return dict(success=True, request_id=request_id, transcript=transcript,
            category=category, original_image_url=original, enhanced_image_url=enhanced,
            image_warning=warning, listing=listing, market_data=market)
    except Exception as exc:
        logger.warning('Pipeline failed: %s', type(exc).__name__)
        return failure('processing_failed', 'Could not store the product photo. Please try again.', 503, request_id)
    finally:
        cleanup_files(*paths)


@router.post('/confirm')
async def confirm_listing(payload: ConfirmListingRequest, owner: str = Depends(owner_key)):
    try:
        product_id = await asyncio.to_thread(supabase_service.insert_product, payload.model_dump(mode='json'), owner)
        return {'success': True, 'product_id': product_id}
    except Exception as exc:
        logger.warning('Save failed: %s', type(exc).__name__)
        return failure('save_failed', 'Could not save to the marketplace. Your draft is kept on this device.')


@router.get('')
async def catalog(owner: str = Depends(owner_key)):
    try:
        return {'products': await asyncio.to_thread(supabase_service.own_products, owner)}
    except Exception:
        return failure('catalog_unavailable', 'Cloud catalog is unavailable. Your local catalog is still accessible.')


@router.get('/discover')
async def discover(offset: int = Query(0, ge=0), limit: int = Query(50, ge=1, le=100)):
    try:
        return {'products': await asyncio.to_thread(supabase_service.discover, offset, limit)}
    except Exception:
        return failure('discovery_unavailable', 'Marketplace is unavailable. Please try again.')


@router.delete('/{product_id}')
async def delete_listing(product_id: UUID, owner: str = Depends(owner_key)):
    try:
        deleted = await asyncio.to_thread(supabase_service.delete_product, str(product_id), owner)
        if not deleted:
            return failure('not_found', 'This listing is unavailable or belongs to another artisan.', 404)
        return {'success': True}
    except Exception:
        return failure('delete_failed', 'Could not delete this listing. Please try again.')


@router.post('/assist')
async def assist(payload: AssistRequest):
    try:
        proposal = await llm_service.revise_listing(payload.listing.model_dump(), payload.instruction, payload.language)
        return {'success': True, 'listing': proposal}
    except Exception:
        return failure('assistant_unavailable', 'AI editing is unavailable. You can still edit every field yourself.')


@router.post('/voice-edit')
async def voice_edit(audio: UploadFile = File(...), language: str = Form(...)):
    from ..utils.validation import valid_audio
    content = await audio.read(MAX_AUDIO_SIZE + 1)
    if language not in ('te', 'hi', 'en') or not content or len(content) > MAX_AUDIO_SIZE or not valid_audio(content):
        return failure('validation_failed', 'Choose a supported language and valid audio file up to 5 MB.', 422)
    path = save_temp(content, suffix=Path(audio.filename or 'audio.m4a').suffix)
    try:
        return {'transcript': await stt_service.transcribe(path, language)}
    except Exception:
        return failure('stt_failed', 'Could not understand the edit. Please try again.')
    finally:
        cleanup_files(path)
