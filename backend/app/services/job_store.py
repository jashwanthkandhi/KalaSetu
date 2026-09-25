"""Durable Supabase queue. No in-memory fallback masquerading as persistence."""
import uuid
from datetime import datetime, timezone
from supabase import create_client
from ..config import settings


class JobStore:
    def __init__(self):
        self._client = None

    @property
    def client(self):
        if not settings.SUPABASE_SERVICE_ROLE_KEY:
            raise RuntimeError('Server-side job storage is not configured')
        if self._client is None:
            self._client = create_client(settings.SUPABASE_URL, settings.SUPABASE_SERVICE_ROLE_KEY)
        return self._client

    def get(self, job_id, owner=None):
        query = self.client.table('processing_jobs').select('*').eq('id', job_id)
        if owner:
            query = query.eq('owner_hash', owner)
        rows = query.execute().data
        return rows[0] if rows else None

    def create(self, row, photo, audio):
        # Deterministic paths make retry uploads idempotent. Different content gets a different ID.
        bucket = self.client.storage.from_('processing-inputs')
        for path, data in ((row['photo_path'], photo), (row['audio_path'], audio)):
            bucket.upload(path, data, file_options={'upsert': 'true', 'content-type': 'application/octet-stream'})
        self.client.table('processing_jobs').upsert(row, on_conflict='id', ignore_duplicates=True).execute()
        return self.get(row['id'], row['owner_hash'])

    def claim(self):
        rows = self.client.rpc('kalasetu_claim_job', {'token': str(uuid.uuid4())}).execute().data
        return rows[0] if rows else None

    def retry(self, job_id, owner):
        self.client.table('processing_jobs').update({'state': 'QUEUED', 'lease_until': None, 'lease_token': None, 'error': None}).eq('id', job_id).eq('owner_hash', owner).eq('state', 'FAILED').lt('attempts', 3).execute()

    def update(self, job, **values):
        values['updated_at'] = datetime.now(timezone.utc).isoformat()
        rows = self.client.table('processing_jobs').update(values).eq('id', job['id']).eq('lease_token', job['lease_token']).execute().data
        if not rows:
            raise RuntimeError('Job lease lost')

    def inputs(self, job):
        bucket = self.client.storage.from_('processing-inputs')
        return bucket.download(job['photo_path']), bucket.download(job['audio_path'])

    def cleanup(self, job):
        self.client.storage.from_('processing-inputs').remove([job['photo_path'], job['audio_path']])


job_store = JobStore()
