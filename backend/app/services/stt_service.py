import logging
from pathlib import Path
from typing import Optional
from openai import OpenAI
import httpx
from ..config import settings
from ..mock_data import MOCK_TRANSCRIPTS

logger = logging.getLogger("kalasetu.stt")


class STTService:
    def __init__(self):
        self.openai_client: Optional[OpenAI] = None
        if settings.OPENAI_API_KEY:
            try:
                self.openai_client = OpenAI(api_key=settings.OPENAI_API_KEY)
            except Exception as e:
                logger.warning(f"Could not initialize OpenAI client for STT: {e}")

    async def transcribe(self, audio_path: Path, language: str) -> str:
        """
        Transcribes speech audio in Telugu, Hindi, or English.
        Returns transcribed text or raises RuntimeError if all methods fail.
        """
        if settings.MOCK_MODE:
            logger.info("MOCK MODE: Returning mock transcript.")
            return MOCK_TRANSCRIPTS.get(language, MOCK_TRANSCRIPTS["en"])

        # 1. Primary: OpenAI Whisper API if key is available
        if self.openai_client:
            try:
                logger.info(f"Calling Whisper API for language '{language}'...")
                with open(audio_path, "rb") as f:
                    response = self.openai_client.audio.transcriptions.create(
                        model="whisper-1",
                        file=f,
                        language=language if language in ("te", "hi", "en") else None,
                        timeout=30.0,
                    )
                    text = response.text.strip()
                    if text:
                        logger.info("Whisper transcription successful.")
                        return text
            except Exception as e:
                logger.warning(f"Whisper API transcription failed: {e}")

        # 2. Sarvam AI Indic STT (saaras:v3)
        if settings.SARVAM_API_KEY:
            try:
                lang_code = {"te": "te-IN", "hi": "hi-IN", "en": "en-IN"}.get(language, "te-IN")
                logger.info(f"Calling Sarvam AI STT (saaras:v3) for language '{lang_code}'...")
                headers = {"api-subscription-key": settings.SARVAM_API_KEY}
                with open(audio_path, "rb") as af:
                    files = {"file": (audio_path.name, af, "audio/wav")}
                    data = {"model": "saaras:v3", "language_code": lang_code}
                    async with httpx.AsyncClient(timeout=20.0) as client:
                        resp = await client.post(
                            "https://api.sarvam.ai/speech-to-text",
                            headers=headers,
                            files=files,
                            data=data,
                        )
                        if resp.status_code == 200:
                            trans = resp.json().get("transcript", "").strip()
                            if trans:
                                logger.info(f"Sarvam STT transcription successful: {trans[:60]}")
                                return trans
                        else:
                            logger.warning(f"Sarvam STT returned HTTP {resp.status_code}: {resp.text[:100]}")
            except Exception as e:
                logger.warning(f"Sarvam STT call failed: {e}")

        # 3. Local Whisper if installed
        try:
            import whisper
            logger.info(f"Loading local Whisper model '{settings.WHISPER_MODEL}'...")
            model = whisper.load_model(settings.WHISPER_MODEL)
            result = model.transcribe(str(audio_path), language=language)
            text = result.get("text", "").strip()
            if text:
                return text
        except (ImportError, Exception) as e:
            logger.debug(f"Local Whisper not available or failed: {e}")

        # 3. Fallback: Google Cloud Speech-to-Text if configured
        if settings.GOOGLE_APPLICATION_CREDENTIALS:
            try:
                from google.cloud import speech
                logger.info("Attempting Google Cloud STT fallback...")
                client = speech.SpeechClient()
                with open(audio_path, "rb") as audio_file:
                    content = audio_file.read()
                audio = speech.RecognitionAudio(content=content)
                lang_code = {"te": "te-IN", "hi": "hi-IN", "en": "en-IN"}.get(language, "en-IN")
                config = speech.RecognitionConfig(
                    language_code=lang_code,
                    enable_automatic_punctuation=True,
                )
                response = client.recognize(config=config, audio=audio)
                transcripts = [res.alternatives[0].transcript for res in response.results if res.alternatives]
                if transcripts:
                    return " ".join(transcripts).strip()
            except Exception as e:
                logger.warning(f"Google Cloud STT fallback failed: {e}")

        # If we reach here and everything failed, but MOCK_TRANSCRIPTS is available as safety net:
        logger.error("All STT engines failed or unavailable.")
        raise RuntimeError("STT transcription failed.")


stt_service = STTService()
