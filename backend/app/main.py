import logging
import asyncio
import sys
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from .config import settings
from .routes import health_router, listings_router, tts_router
from .routes.jobs import router as jobs_router
from .routes.distribution import router as distribution_router
from .services.job_runner import worker

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
        raise RuntimeError('MOCK_MODE cannot be used by the server. Use isolated test doubles instead.')
    else:
        logger.info(f"NIM Base URL: {settings.NVIDIA_NIM_BASE_URL}")
        logger.info(f"Nemotron Model: {settings.NEMOTRON_MODEL}")
        logger.info(f"Qwen Image Model: {settings.QWEN_IMAGE_MODEL}")
        logger.info(f"TTS Provider: {settings.TTS_PROVIDER}")
    workers = [asyncio.create_task(worker()) for _ in range(settings.JOB_WORKERS)] if settings.JOB_WORKER_ENABLED and settings.SUPABASE_SERVICE_ROLE_KEY else []
    try:
        yield
    finally:
        for task in workers:
            task.cancel()
        await asyncio.gather(*workers, return_exceptions=True)
    # Shutdown
    logger.info("Shutting down KalaSetu Backend Service.")


app = FastAPI(
    title="KalaSetu Backend API",
    description="Backend API for AI-powered multilingual marketplace assistant for Indian artisans",
    version="1.0.0",
    lifespan=lifespan,
)


@app.exception_handler(RequestValidationError)
async def validation_error(request, exc):
    # Do not echo recordings, personal text or non-JSON numbers in error bodies.
    return JSONResponse(status_code=422, content={'detail': [
        {'loc': list(error['loc']), 'msg': error['msg'], 'type': error['type']}
        for error in exc.errors()]})

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
app.include_router(jobs_router)
app.include_router(distribution_router)
app.include_router(listings_router)
app.include_router(tts_router)


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
