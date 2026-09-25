import asyncio
import hashlib
import json
import time
import uuid
from pathlib import Path
from fastapi import APIRouter, Depends, File, Form, Header, HTTPException, UploadFile
from fastapi.responses import JSONResponse, StreamingResponse
from ..services.ownership import owner_key, owner_digest
from ..services.job_store import job_store
from ..services.fair_pricing import CostInputs, calculate_price
from ..utils.validation import validate_media_files, MAX_PHOTO_SIZE, MAX_AUDIO_SIZE

router = APIRouter(prefix='/api/v1/listings', tags=['Durable jobs'])


def public_job(row):
    return {key: row.get(key) for key in ('id', 'state', 'attempts', 'result', 'error', 'timings', 'updated_at')}


@router.post('/jobs', status_code=202)
async def create_job(photo: UploadFile = File(...), audio: UploadFile = File(...), language: str = Form(...),
                     owner: str = Depends(owner_key), idempotency_key: uuid.UUID = Header(alias='Idempotency-Key')):
    started = time.perf_counter()
    photo_bytes = await photo.read(MAX_PHOTO_SIZE + 1)
    audio_bytes = await audio.read(MAX_AUDIO_SIZE + 1)
    valid, code, message = validate_media_files(photo_bytes, photo.content_type, photo.filename,
        audio_bytes, audio.content_type, audio.filename, language)
    if not valid:
        raise HTTPException(413 if code == 'file_too_large' else 422, detail={'code': code, 'message': message})
    owner_hash = owner_digest(owner)
    job_id = str(uuid.uuid5(uuid.NAMESPACE_URL, owner_hash + str(idempotency_key)))
    request_hash = hashlib.sha256(photo_bytes + audio_bytes + language.encode()).hexdigest()
    try:
        existing = await asyncio.to_thread(job_store.get, job_id, owner_hash)
        if existing and existing['request_hash'] != request_hash:
            raise HTTPException(409, 'This request ID already belongs to different media.')
        if not existing:
            row = dict(id=job_id, owner_hash=owner_hash, request_hash=request_hash, language=language,
                photo_path=f'{job_id}/photo', audio_path=f'{job_id}/audio',
                audio_suffix=Path(audio.filename or 'audio.m4a').suffix.lower(), state='QUEUED')
            existing = await asyncio.to_thread(job_store.create, row, photo_bytes, audio_bytes)
        elif existing['state'] == 'FAILED' and existing['attempts'] < 3:
            await asyncio.to_thread(job_store.retry, job_id, owner_hash)
            existing = await asyncio.to_thread(job_store.get, job_id, owner_hash)
        return JSONResponse(status_code=202, content={'job_id': job_id,
            'poll_url': f'/api/v1/listings/jobs/{job_id}', 'state': existing['state'],
            'acceptance_seconds': round(time.perf_counter() - started, 3)})
    except HTTPException:
        raise
    except Exception:
        raise HTTPException(503, 'Job storage is unavailable. Your capture stays on your device.') from None


async def owned_job(job_id, owner):
    try:
        row = await asyncio.to_thread(job_store.get, str(job_id), owner_digest(owner))
    except Exception:
        raise HTTPException(503, 'Progress is temporarily unavailable. Try again.') from None
    if not row:
        raise HTTPException(404, 'Job not found.')
    return row


@router.get('/jobs/{job_id}')
async def get_job(job_id: uuid.UUID, owner: str = Depends(owner_key)):
    return public_job(await owned_job(job_id, owner))


@router.get('/jobs/{job_id}/events')
async def events(job_id: uuid.UUID, owner: str = Depends(owner_key)):
    await owned_job(job_id, owner)
    async def stream():
        last = None
        # Bounded stream; clients reconnect or poll to resume from durable state.
        for _ in range(120):
            row = public_job(await owned_job(job_id, owner))
            data = json.dumps(row, ensure_ascii=False)
            if data != last:
                yield f'event: progress\ndata: {data}\n\n'
                last = data
            else:
                yield ': heartbeat\n\n'
            if row['state'] in ('COMPLETED', 'FAILED'):
                return
            await asyncio.sleep(2)
    return StreamingResponse(stream(), media_type='text/event-stream', headers={'Cache-Control': 'no-cache'})


@router.post('/fair-price')
async def fair_price(costs: CostInputs):
    try:
        return calculate_price(costs)
    except ValueError as exc:
        raise HTTPException(422, str(exc)) from None
