"""Cloud persistence. Never convert a failure into a successful save."""
import hashlib
from typing import Optional
from supabase import create_client, Client
from ..config import settings


class SupabaseService:
    def __init__(self):
        self.client: Optional[Client] = None
        if settings.SUPABASE_URL and settings.SUPABASE_KEY:
            self.client = create_client(settings.SUPABASE_URL, settings.SUPABASE_KEY)

    def require_client(self):
        if not self.client:
            raise RuntimeError('Cloud storage is not configured')
        return self.client

    def upload_image(self, file_bytes, destination_path, content_type='image/jpeg', bucket_name='listing-images'):
        client = self.require_client()
        client.storage.from_(bucket_name).upload(destination_path, file_bytes,
            file_options={'content-type': content_type, 'upsert': 'true'})
        return client.storage.from_(bucket_name).get_public_url(destination_path)

    @staticmethod
    def owner_hash(owner_key: str):
        return hashlib.sha256(owner_key.encode()).hexdigest()

    def insert_product(self, product_data: dict, owner_key: str) -> str:
        data = dict(product_data)
        data['product_id'] = str(data['product_id'])
        if not data.get('public_profile', {}).get('contact_public'):
            data.get('public_profile', {}).pop('contact', None)
        result = self.require_client().rpc('kalasetu_save', {
            'payload': data, 'owner': self.owner_hash(owner_key)}).execute()
        if not result.data or str(result.data) != data['product_id']:
            raise RuntimeError('Save was not acknowledged')
        return str(result.data)

    def own_products(self, owner_key: str):
        result = self.require_client().rpc('kalasetu_catalog', {'owner': self.owner_hash(owner_key)}).execute()
        return result.data or []

    def discover(self, offset=0, limit=50):
        result = self.require_client().table('marketplace_products').select('*').order('created_at', desc=True).range(offset, offset + limit - 1).execute()
        return result.data or []

    def delete_product(self, product_id: str, owner_key: str):
        result = self.require_client().rpc('kalasetu_delete', {
            'product': product_id, 'owner': self.owner_hash(owner_key)}).execute()
        return bool(result.data)


supabase_service = SupabaseService()
