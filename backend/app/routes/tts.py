from fastapi import APIRouter, Response, HTTPException
from ..schemas.listing_request import TTSRequest
from ..services.tts_service import tts_service

router = APIRouter(prefix='/api/v1', tags=['TTS'])


@router.post('/tts')
async def text_to_speech(payload: TTSRequest):
    try:
        audio = await tts_service.synthesize(payload.text, payload.language)
    except Exception:
        audio = None
    if not audio:
        raise HTTPException(503, 'Voice playback is unavailable')
    return Response(content=audio, media_type='audio/wav' if audio.startswith(b'RIFF') else 'audio/mpeg')
