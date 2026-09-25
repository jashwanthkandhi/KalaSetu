import asyncio
import base64
import io
import httpx
from PIL import Image
from ..config import settings
from ..utils.file_utils import compress_image
from .supabase_service import supabase_service


class ImageService:
    async def process_and_enhance(self, image_bytes, request_id):
        # Keep exact original bytes; enhancement gets a resized copy.
        with Image.open(io.BytesIO(image_bytes)) as source:
            extension = 'png' if source.format == 'PNG' else 'jpg'
        original = await asyncio.to_thread(supabase_service.upload_image, image_bytes,
            f'original/{request_id}.{extension}', 'image/png' if extension == 'png' else 'image/jpeg')
        if settings.MOCK_MODE or not settings.QWEN_IMAGE_ENDPOINT:
            return original, original, True
        try:
            prepared = compress_image(image_bytes)
            enhanced = await self._call_qwen_image_api(prepared, settings.QWEN_IMAGE_API_KEY or settings.NVIDIA_NIM_API_KEY, request_id)
            with Image.open(io.BytesIO(enhanced)) as result:
                if result.width * result.height > 40_000_000:
                    raise ValueError('Image exceeds pixel limit')
                result.verify()
            enhanced = compress_image(enhanced)
            url = await asyncio.to_thread(supabase_service.upload_image, enhanced, f'enhanced/{request_id}.jpg')
            return original, url, False
        except Exception:
            return original, original, True

    async def _call_qwen_image_api(self, image_bytes, api_key, request_id):
        # Deployed NIM OpenAI-compatible editing endpoint, never a chat/VLM endpoint.
        async with httpx.AsyncClient(timeout=25) as client:
            response = await client.post(settings.QWEN_IMAGE_ENDPOINT,
                headers={'Authorization': f'Bearer {api_key}'},
                json={'model': settings.QWEN_IMAGE_MODEL, 'response_format': 'b64_json',
                      'image': 'data:image/jpeg;base64,' + base64.b64encode(image_bytes).decode(),
                      'prompt': 'Improve lighting and simplify the background. Preserve the exact product identity, shape, color and details.'})
            response.raise_for_status()
            return base64.b64decode(response.json()['data'][0]['b64_json'], validate=True)


image_service = ImageService()
