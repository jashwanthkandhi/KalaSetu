import asyncio
import mimetypes
from openai import AsyncOpenAI
import httpx
from ..config import settings


class STTService:
    def __init__(self):
        self.openai_client = AsyncOpenAI(api_key=settings.OPENAI_API_KEY, max_retries=0) if settings.OPENAI_API_KEY else None
        self.local_model = None

    async def transcribe(self, audio_path, language):
        if settings.SARVAM_API_KEY:
            for attempt in range(2):
                try:
                    async with httpx.AsyncClient(timeout=20) as client:
                        with open(audio_path, 'rb') as audio:
                            response = await client.post('https://api.sarvam.ai/speech-to-text',
                                headers={'api-subscription-key': settings.SARVAM_API_KEY},
                                files={'file': (audio_path.name, audio, mimetypes.guess_type(audio_path.name)[0] or 'audio/mp4')},
                                data={'model': 'saaras:v3', 'language_code': f'{language}-IN', 'mode': 'transcribe'})
                        response.raise_for_status()
                        text = response.json().get('transcript', '').strip()
                        if text:
                            return text
                except (httpx.HTTPError, ValueError):
                    pass
        if self.openai_client:
            try:
                with open(audio_path, 'rb') as audio:
                    result = await self.openai_client.audio.transcriptions.create(
                        model='whisper-1', file=audio, language=language, timeout=30)
                if result.text.strip():
                    return result.text.strip()
            except Exception:
                pass
        if settings.LOCAL_WHISPER_ENABLED:
            try:
                return await asyncio.to_thread(self._local_transcribe, audio_path, language)
            except Exception:
                pass
        raise RuntimeError('Speech recognition unavailable')

    def _local_transcribe(self, path, language):
        import whisper
        if self.local_model is None:
            self.local_model = whisper.load_model(settings.WHISPER_MODEL)
        text = self.local_model.transcribe(str(path), language=language, fp16=False).get('text', '').strip()
        if not text:
            raise RuntimeError('Empty transcript')
        return text


stt_service = STTService()
