import logging
import time
import uuid
from typing import Optional
from fastapi import APIRouter, File, Form, UploadFile, status
from fastapi.responses import JSONResponse

from ..config import settings
from ..schemas.listing_request import ConfirmListingRequest
from ..schemas.listing_response import (
    ProcessSuccessResponse,
    ConfirmSuccessResponse,
    ErrorResponse,
    ErrorDetail,
    CategoryInfo,
    ListingDetail,
)
from ..services import (
    stt_service,
    image_service,
    category_service,
    llm_service,
    pricing_service,
    supabase_service,
)
from ..utils.file_utils import save_temp, cleanup_files
from ..utils.validation import validate_media_files

logger = logging.getLogger("kalasetu.listings")
router = APIRouter(prefix="/api/v1/listings", tags=["Listings"])


@router.post("/process", response_model=ProcessSuccessResponse)
async def process_listing(
    photo: UploadFile = File(...),
    audio: UploadFile = File(...),
    language: str = Form(...),
):
    request_id = str(uuid.uuid4())
    start_time = time.time()
    logger.info(f"[{request_id}] Started process request for language: {language}")

    photo_temp_path = None
    audio_temp_path = None

    try:
        # Read file contents
        photo_bytes = await photo.read()
        audio_bytes = await audio.read()

        # 1. Validation
        is_valid, err_code, err_msg = validate_media_files(
            photo_content=photo_bytes,
            photo_mime=photo.content_type,
            photo_filename=photo.filename,
            audio_content=audio_bytes,
            audio_mime=audio.content_type,
            audio_filename=audio.filename,
            language=language,
        )

        if not is_valid:
            http_status = status.HTTP_413_REQUEST_ENTITY_TOO_LARGE if err_code == "file_too_large" else status.HTTP_422_UNPROCESSABLE_ENTITY
            logger.warning(f"[{request_id}] Validation failed: {err_code} - {err_msg}")
            return JSONResponse(
                status_code=http_status,
                content=ErrorResponse(
                    request_id=request_id,
                    error=ErrorDetail(code=err_code or "validation_failed", message=err_msg or "Validation error"),
                ).model_dump(),
            )

        # 2. Save temporary files
        photo_temp_path = save_temp(photo_bytes, suffix="_photo.jpg")
        audio_temp_path = save_temp(audio_bytes, suffix="_audio.m4a")

        # 3. Speech-to-Text (Critical Path)
        try:
            transcript = await stt_service.transcribe(audio_temp_path, language=language)
            logger.info(f"[{request_id}] STT completed in {int((time.time() - start_time) * 1000)}ms")
        except Exception as e:
            logger.error(f"[{request_id}] STT failed: {e}")
            return JSONResponse(
                status_code=status.HTTP_200_OK,  # TH-4 table maps stt_failed to HTTP 200
                content=ErrorResponse(
                    request_id=request_id,
                    error=ErrorDetail(
                        code="stt_failed",
                        message="Could not understand the recording. Please try again.",
                    ),
                ).model_dump(),
            )

        # 4. Image Enhancement & Upload (Non-critical fallback)
        original_image_url, enhanced_image_url, image_warning = await image_service.process_and_enhance(
            image_bytes=photo_bytes,
            request_id=request_id,
        )

        # 5. Category Detection (Non-critical fallback)
        cat_result = category_service.predict(photo_bytes)
        category_name = cat_result.get("name", "Other")
        category_conf = cat_result.get("confidence", 0.0)

        # 6. LLM Listing Generation (Critical Path with 1 retry)
        try:
            llm_result = await llm_service.generate_listing(
                transcript=transcript,
                category_hint=category_name,
                language=language,
            )
        except Exception as e:
            logger.error(f"[{request_id}] LLM failed: {e}")
            return JSONResponse(
                status_code=status.HTTP_200_OK,  # TH-4 table maps llm_failed to HTTP 200
                content=ErrorResponse(
                    request_id=request_id,
                    error=ErrorDetail(
                        code="llm_failed",
                        message="Listing could not be generated. Please try again.",
                    ),
                ).model_dump(),
            )

        # 7. Pricing Suggestion (Non-critical fallback)
        final_category = llm_result.get("category", category_name)
        suggested_price = pricing_service.suggest_price(
            category=final_category,
            tags=llm_result.get("tags", []),
            llm_suggested=float(llm_result.get("suggested_price", 450.0)),
        )

        # Assemble full response
        response = ProcessSuccessResponse(
            success=True,
            request_id=request_id,
            transcript=transcript,
            category=CategoryInfo(name=final_category, confidence=category_conf),
            original_image_url=original_image_url,
            enhanced_image_url=enhanced_image_url,
            image_warning=image_warning,
            listing=ListingDetail(
                title=llm_result.get("title", "Handcrafted Artisan Product"),
                description=llm_result.get("description", "Handcrafted traditional item."),
                category=final_category,
                tags=llm_result.get("tags", []),
                suggested_price=float(suggested_price),
            ),
        )

        logger.info(f"[{request_id}] Processing pipeline successfully finished in {int((time.time() - start_time) * 1000)}ms")
        return response

    except Exception as e:
        logger.error(f"[{request_id}] Unexpected pipeline error: {e}", exc_info=True)
        return JSONResponse(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            content=ErrorResponse(
                request_id=request_id,
                error=ErrorDetail(code="internal_error", message="An unexpected server error occurred."),
            ).model_dump(),
        )
    finally:
        # Mandatory cleanup of temporary files (TH-8)
        cleanup_files(photo_temp_path, audio_temp_path)


@router.post("/confirm", response_model=ConfirmSuccessResponse)
async def confirm_listing(payload: ConfirmListingRequest):
    request_id = str(uuid.uuid4())
    logger.info(f"[{request_id}] Confirming listing: '{payload.title}' (category: {payload.category})")

    try:
        product_id = supabase_service.insert_product(payload.model_dump())
        logger.info(f"[{request_id}] Listing confirmed and persisted with ID: {product_id}")
        return ConfirmSuccessResponse(success=True, product_id=product_id)
    except Exception as e:
        logger.error(f"[{request_id}] Supabase save failed: {e}")
        return JSONResponse(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            content=ErrorResponse(
                request_id=request_id,
                error=ErrorDetail(
                    code="save_failed",
                    message="Couldn't save your listing. Please try again.",
                ),
            ).model_dump(),
        )
