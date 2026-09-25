"""Bounded leased workers. Durable queue is independent of API request lifetime."""
import asyncio
import logging
import time
from .job_store import job_store
from . import stt_service, image_service, llm_service
from . import craft_intelligence
from .fair_pricing import missing_costs
from ..utils.file_utils import save_temp, cleanup_files

logger = logging.getLogger(__name__)


async def run_job(job, store=job_store):
    timings = {}
    started = time.perf_counter()
    async def stage(state):
        await asyncio.to_thread(store.update, job, state=state, timings=timings)
    async def timed(name, operation):
        start = time.perf_counter()
        try:
            return await operation
        finally:
            timings[name] = round(time.perf_counter() - start, 3)
    async def pipeline():
        photo, audio = await asyncio.to_thread(store.inputs, job)
        path = save_temp(audio, job['audio_suffix'])
        try:
            await stage('TRANSCRIBING')
            # TaskGroup cancels the other task when an essential operation fails.
            async with asyncio.TaskGroup() as group:
                speech = group.create_task(timed('stt_seconds', asyncio.wait_for(stt_service.transcribe(path, job['language']), 65)))
                image = group.create_task(timed('image_seconds', asyncio.wait_for(image_service.process_and_enhance(photo, job['id']), 65)))
                transcript = await speech
                if not transcript.strip():
                    raise ValueError('Empty transcript')
                if not image.done():
                    await stage('ENHANCING')
            original, enhanced, warning = image.result()
        finally:
            cleanup_files(path)
        await stage('ANALYZING')
        try:
            craft = await timed('vision_seconds', asyncio.wait_for(craft_intelligence.analyze(photo, transcript), 40))
        except Exception:
            craft = craft_intelligence.CraftAnalysis(category='Other', confidence=0,
                evidence='Craft analysis unavailable. Ask the artisan to choose a category.')
        await stage('PRICING')
        market = missing_costs()
        timings['pricing_seconds'] = 0.0
        await stage('GENERATING')
        listing = await timed('llm_seconds', asyncio.wait_for(llm_service.generate_listing(transcript, craft.category, job['language']), 85))
        listing['category'] = craft.category
        listing['suggested_price'] = 0
        listing['attributes']['craft_tradition'] = craft.tradition
        timings['total_seconds'] = round(time.perf_counter() - started, 3)
        result = dict(success=True, request_id=job['id'], transcript=transcript,
            category={'name': craft.category, 'confidence': craft.confidence}, craft=craft.model_dump(),
            original_image_url=original, enhanced_image_url=enhanced, image_warning=warning,
            listing=listing, market_data=market)
        await asyncio.to_thread(store.update, job, state='COMPLETED', result=result, timings=timings, error=None)
    try:
        await asyncio.wait_for(pipeline(), 240)
    except asyncio.CancelledError:
        # Lease expires after process shutdown; another worker can recover the durable inputs.
        raise
    except Exception:
        timings['total_seconds'] = round(time.perf_counter() - started, 3)
        await asyncio.to_thread(store.update, job, state='FAILED', timings=timings,
            error={'code': 'processing_failed', 'message': 'Processing could not finish. Your local recording is safe. Retry from the sync center.'})
    else:
        try:
            await asyncio.to_thread(store.cleanup, job)
        except Exception:
            logger.warning('Job input cleanup needs retry for %s', job['id'])


async def worker(store=job_store):
    while True:
        try:
            job = await asyncio.to_thread(store.claim)
            if job:
                await run_job(job, store)
            else:
                await asyncio.sleep(2)
        except asyncio.CancelledError:
            raise
        except Exception:
            logger.warning('Durable queue temporarily unavailable')
            await asyncio.sleep(5)
