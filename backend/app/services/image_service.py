import base64
import logging
from typing import Tuple
import httpx
from ..config import settings
from .supabase_service import supabase_service
from ..utils.file_utils import compress_image

logger = logging.getLogger("kalasetu.image")


class ImageService:
    async def process_and_enhance(
        self,
        image_bytes: bytes,
        request_id: str,
    ) -> Tuple[str, str, bool]:
        """
        1. Compresses image and uploads original to Supabase Storage.
        2. Calls NVIDIA NIM Qwen-Image-Edit API (or fallback).
        3. If enhancement succeeds, uploads to Supabase Storage and returns enhanced URL.
        4. If enhancement fails, returns original URL with image_warning=True.
        Returns: (original_image_url, enhanced_image_url, image_warning).
        """
        # Compress image for storage & API processing
        prepared_bytes = compress_image(image_bytes, max_mb=5.0, max_px=1920)

        # Upload original image
        original_path = f"original/{request_id}.jpg"
        original_url = supabase_service.upload_image(
            file_bytes=prepared_bytes,
            destination_path=original_path,
            content_type="image/jpeg",
        )

        if settings.MOCK_MODE:
            logger.info("MOCK MODE: Returning original image as enhanced.")
            return original_url, original_url, False

        # Attempt Qwen-Image-Edit via NVIDIA NIM
        api_key = settings.QWEN_IMAGE_API_KEY or settings.NVIDIA_NIM_API_KEY
        if not api_key:
            logger.warning(f"[{request_id}] No image enhancement API key configured. Passing original.")
            return original_url, original_url, True

        try:
            enhanced_bytes = await self._call_qwen_image_api(prepared_bytes, api_key, request_id)
            if enhanced_bytes:
                enhanced_path = f"enhanced/{request_id}.jpg"
                enhanced_url = supabase_service.upload_image(
                    file_bytes=enhanced_bytes,
                    destination_path=enhanced_path,
                    content_type="image/jpeg",
                )
                logger.info(f"[{request_id}] Image enhancement successful.")
                return original_url, enhanced_url, False
        except Exception as e:
            logger.warning(f"[{request_id}] Qwen image enhancement failed gracefully: {e}")

        # Fallback to original image
        return original_url, original_url, True

    async def _call_qwen_image_api(self, image_bytes: bytes, api_key: str, request_id: str) -> bytes:
        """
        Calls NVIDIA NIM image editing service.
        """
        headers = {
            "Authorization": f"Bearer {api_key}",
            "Accept": "application/json",
        }

        # Encode image to base64 data URI
        b64_img = base64.b64encode(image_bytes).decode("utf-8")
        data_uri = f"data:image/jpeg;base64,{b64_img}"

        instruction = (
            "Remove or replace any distracting background. "
            "Place the product on a clean white or light neutral studio background. "
            "Improve lighting to clearly show product details and texture. "
            "Preserve the actual product appearance — do not modify or invent product details."
        )

        # Standard NVIDIA NIM Image Generation / Edit endpoint
        base_url = settings.NVIDIA_NIM_BASE_URL.rstrip("/")
        endpoint = f"{base_url}/images/edits"

        payload = {
            "model": settings.QWEN_IMAGE_MODEL,
            "prompt": instruction,
            "image": data_uri,
            "response_format": "b64_json",
        }

        async with httpx.AsyncClient(timeout=25.0) as client:
            resp = await client.post(endpoint, headers=headers, json=payload)
            if resp.status_code == 200:
                data = resp.json()
                if "data" in data and len(data["data"]) > 0:
                    b64_result = data["data"][0].get("b64_json")
                    if b64_result:
                        return base64.b64decode(b64_result)
            elif resp.status_code == 404:
                # Try chat/completions fallback format if image/edits is routed differently
                chat_endpoint = f"{base_url}/chat/completions"
                chat_payload = {
                    "model": settings.QWEN_IMAGE_MODEL,
                    "messages": [
                        {
                            "role": "user",
                            "content": [
                                {"type": "text", "text": instruction},
                                {"type": "image_url", "image_url": {"url": data_uri}},
                            ],
                        }
                    ],
                    "max_tokens": 1024,
                }
                chat_resp = await client.post(chat_endpoint, headers=headers, json=chat_payload)
                if chat_resp.status_code == 200:
                    # Some endpoints return text or image url
                    pass

        raise RuntimeError(f"NIM image edit returned unexpected status: {resp.status_code if 'resp' in locals() else 'error'}")


image_service = ImageService()
