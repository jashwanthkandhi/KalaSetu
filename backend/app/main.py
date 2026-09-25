import logging
import sys
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from .config import settings
from .routes import health_router, listings_router, tts_router

# Configure structured logging (TH-18)
logging.basicConfig(
    level=getattr(logging, settings.LOG_LEVEL.upper(), logging.INFO),
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
    handlers=[logging.StreamHandler(sys.stdout)],
)

logger = logging.getLogger("kalasetu.main")


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    logger.info("Initializing KalaSetu Backend Service (SIH26090)...")
    if settings.MOCK_MODE:
        logger.warning(">>> MOCK MODE ACTIVE - Deterministic fixtures will be served <<<")
    else:
        logger.info(f"NIM Base URL: {settings.NVIDIA_NIM_BASE_URL}")
        logger.info(f"Nemotron Model: {settings.NEMOTRON_MODEL}")
        logger.info(f"Qwen Image Model: {settings.QWEN_IMAGE_MODEL}")
        logger.info(f"TTS Provider: {settings.TTS_PROVIDER}")
    yield
    # Shutdown
    logger.info("Shutting down KalaSetu Backend Service.")


app = FastAPI(
    title="KalaSetu Backend API",
    description="Backend API for AI-powered multilingual marketplace assistant for Indian artisans",
    version="1.0.0",
    lifespan=lifespan,
)

# CORS middleware for mobile client communication
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Mount routes
app.include_router(health_router)
app.include_router(listings_router)
app.include_router(tts_router)


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
