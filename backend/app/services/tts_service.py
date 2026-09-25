import base64
import logging
from typing import Optional
import httpx
from ..config import settings

logger = logging.getLogger("kalasetu.tts")


class TTSService:
    async def synthesize(self, text: str, language: str) -> Optional[bytes]:
        """
        Synthesizes text to speech audio bytes (MP3/WAV).
        Supports Sarvam Bulbul and Google Cloud TTS.
        """
        # 1. Sarvam Bulbul
        if settings.SARVAM_API_KEY and (settings.TTS_PROVIDER == "sarvam" or not settings.GOOGLE_APPLICATION_CREDENTIALS):
            try:
                lang_code = {"te": "te-IN", "hi": "hi-IN", "en": "en-IN"}.get(language, "en-IN")
                url = "https://api.sarvam.ai/text-to-speech"
                headers = {
                    "api-subscription-key": settings.SARVAM_API_KEY,
                    "Content-Type": "application/json",
                }
                payload = {
                    "text": text[:500],
                    "language_code": lang_code,
                    "output_audio_codec": "wav",
                    "model": settings.SARVAM_MODEL,
                }
                async with httpx.AsyncClient(timeout=15.0) as client:
                    resp = await client.post(url, headers=headers, json=payload)
                    if resp.status_code == 200:
                        data = resp.json()
                        audios = data.get("audios", [])
                        if audios:
                            return base64.b64decode(audios[0])
            except Exception as e:
                logger.warning("Sarvam TTS failed: %s", type(e).__name__)

        # 2. Google Cloud TTS
        if settings.GOOGLE_APPLICATION_CREDENTIALS:
            try:
                from google.cloud import texttospeech
                client = texttospeech.TextToSpeechClient()
                s_input = texttospeech.SynthesisInput(text=text[:500])
                lang_code = {"te": "te-IN", "hi": "hi-IN", "en": "en-IN"}.get(language, "en-IN")
                voice = texttospeech.VoiceSelectionParams(
                    language_code=lang_code,
                    ssml_gender=texttospeech.SsmlVoiceGender.FEMALE,
                )
                audio_config = texttospeech.AudioConfig(
                    audio_encoding=texttospeech.AudioEncoding.MP3
                )
                response = client.synthesize_speech(
                    input=s_input, voice=voice, audio_config=audio_config
                )
                return response.audio_content
            except Exception as e:
                logger.warning("Google Cloud TTS failed: %s", type(e).__name__)

        return None


tts_service = TTSService()
