import logging
from fastapi import APIRouter, Response, HTTPException, status
from pydantic import BaseModel
from ..services.tts_service import tts_service

logger = logging.getLogger("kalasetu.tts_route")
router = APIRouter(prefix="/api/v1", tags=["TTS"])


class TTSRequest(BaseModel):
    text: str
    language: str = "te"


@router.post("/tts")
async def text_to_speech(payload: TTSRequest):
    if not payload.text:
        raise HTTPException(status_code=status.HTTP_422_UNPROCESSABLE_ENTITY, detail="Text cannot be empty")

    audio_bytes = await tts_service.synthesize(payload.text, payload.language)
    if not audio_bytes:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="TTS synthesis unavailable",
        )

    return Response(content=audio_bytes, media_type="audio/mpeg")
