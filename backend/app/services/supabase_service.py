import logging
import time
import uuid
from typing import Dict, Any, Optional
from supabase import create_client, Client
from ..config import settings

logger = logging.getLogger("kalasetu.supabase")


class SupabaseService:
    def __init__(self):
        self.client: Optional[Client] = None
        self._init_client()

    def _init_client(self):
        if settings.SUPABASE_URL and settings.SUPABASE_KEY:
            try:
                self.client = create_client(settings.SUPABASE_URL, settings.SUPABASE_KEY)
            except Exception as e:
                logger.warning(f"Failed to initialize Supabase client: {e}")
                self.client = None

    def upload_image(
        self,
        file_bytes: bytes,
        destination_path: str,
        content_type: str = "image/jpeg",
        bucket_name: str = "listing-images",
    ) -> str:
        """
        Uploads image to Supabase storage bucket with 1 retry (2s delay).
        Returns public URL or fallback URL.
        """
        if not self.client:
            # Local fallback mock URL if Supabase not configured
            return f"{settings.SUPABASE_URL}/storage/v1/object/public/{bucket_name}/{destination_path}"

        for attempt in range(2):
            try:
                # Upload file bytes
                res = self.client.storage.from_(bucket_name).upload(
                    path=destination_path,
                    file=file_bytes,
                    file_options={"content-type": content_type, "upsert": "true"},
                )
                # Obtain public URL
                public_url = self.client.storage.from_(bucket_name).get_public_url(destination_path)
                return public_url
            except Exception as e:
                logger.warning(f"Supabase upload attempt {attempt + 1} failed: {e}")
                if attempt == 0:
                    time.sleep(2)
                else:
                    # Construct public URL directly if upload bucket allows it
                    base = settings.SUPABASE_URL.rstrip("/")
                    return f"{base}/storage/v1/object/public/{bucket_name}/{destination_path}"

    def insert_product(self, product_data: Dict[str, Any]) -> str:
        """
        Inserts product row into Supabase 'products' table.
        Returns product ID.
        """
        product_id = str(uuid.uuid4())
        record = {
            "id": product_id,
            "artisan_id": product_data.get("artisan_id"),
            "title": product_data.get("title"),
            "description": product_data.get("description"),
            "category": product_data.get("category"),
            "tags": product_data.get("tags", []),
            "original_image_url": product_data.get("original_image_url"),
            "enhanced_image_url": product_data.get("enhanced_image_url"),
            "voice_transcript": product_data.get("transcript"),
            "suggested_price": product_data.get("suggested_price"),
            "final_price": product_data.get("final_price"),
            "status": "saved",
        }

        if not self.client:
            # Deterministic fallback when Supabase connection not available
            return product_id

        for attempt in range(2):
            try:
                response = self.client.table("products").insert(record).execute()
                if response.data and len(response.data) > 0:
                    return response.data[0].get("id", product_id)
                return product_id
            except Exception as e:
                logger.warning(f"Supabase insert attempt {attempt + 1} failed: {e}")
                if attempt == 0:
                    time.sleep(2)
                else:
                    raise e


supabase_service = SupabaseService()
