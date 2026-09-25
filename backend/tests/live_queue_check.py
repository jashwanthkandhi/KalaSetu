"""Explicit live storage smoke check; creates and removes only its own test records."""
import hashlib
import uuid
from datetime import datetime, timedelta, timezone
from supabase import create_client
from app.config import settings
from app.services.job_store import JobStore


def main():
    store = JobStore()
    job_id, lease = str(uuid.uuid4()), str(uuid.uuid4())
    owner = hashlib.sha256(uuid.uuid4().bytes).hexdigest()
    row = dict(id=job_id, owner_hash=owner, request_hash='live-storage-check',
        language='en', photo_path=f'{job_id}/check-photo', audio_path=f'{job_id}/check-audio',
        audio_suffix='.wav', state='TRANSCRIBING', attempts=1, lease_token=lease,
        lease_until=(datetime.now(timezone.utc)+timedelta(minutes=10)).isoformat())
    try:
        store.create(row, b'storage-check-photo', b'storage-check-audio')
        assert JobStore().get(job_id, owner)['lease_token'] == lease
        assert store.get(job_id, '0'*64) is None
        assert store.inputs(row) == (b'storage-check-photo', b'storage-check-audio')
        try:
            store.update(dict(row, lease_token=str(uuid.uuid4())), state='COMPLETED')
        except RuntimeError:
            pass
        else:
            raise AssertionError('Stale lease accepted')
        store.update(row, state='COMPLETED', result={'storage_check': True})
        assert JobStore().get(job_id, owner)['state'] == 'COMPLETED'
        public = create_client(settings.SUPABASE_URL, settings.SUPABASE_KEY)
        try:
            response = public.table('processing_jobs').select('id').eq('id', job_id).execute()
            assert not response.data
        except Exception as error:
            assert getattr(error, 'code', None) == '42501', type(error).__name__
        try:
            public.storage.from_('processing-inputs').download(row['photo_path'])
        except Exception:
            pass
        else:
            raise AssertionError('Private media readable anonymously')
        print('PASS: durable row/media roundtrip, fresh-client persistence, owner filtering, stale lease rejection, public access denied.')
    finally:
        store.cleanup(row)
        store.client.table('processing_jobs').delete().eq('id', job_id).eq('owner_hash', owner).execute()
        print('Removed this check\'s temporary row and media.')


if __name__ == '__main__':
    main()
