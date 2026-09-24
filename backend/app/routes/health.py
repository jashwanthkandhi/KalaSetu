from fastapi import APIRouter
from ..config import settings
from ..schemas.listing_response import HealthResponse

router = APIRouter(tags=["Health"])


@router.get("/health", response_model=HealthResponse)
async def health_check():
    return HealthResponse(
        status="ok",
        model=settings.WHISPER_MODEL,
        stack="SIH26090",
        mock_mode=settings.MOCK_MODE,
    )
