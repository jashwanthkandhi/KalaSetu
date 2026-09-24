from typing import Optional, Tuple


ALLOWED_IMAGE_TYPES = {
    "image/jpeg",
    "image/jpg",
    "image/png",
    "application/octet-stream",  # Fallback for some mobile HTTP clients
}

ALLOWED_AUDIO_TYPES = {
    "audio/wav",
    "audio/x-wav",
    "audio/mpeg",
    "audio/mp3",
    "audio/mp4",
    "audio/m4a",
    "audio/x-m4a",
    "audio/aac",
    "audio/ogg",
    "application/octet-stream",  # Fallback for multipart audio upload
}

MAX_PHOTO_SIZE = 10 * 1024 * 1024  # 10 MB
MAX_AUDIO_SIZE = 5 * 1024 * 1024   # 5 MB


def validate_media_files(
    photo_content: bytes,
    photo_mime: Optional[str],
    photo_filename: Optional[str],
    audio_content: bytes,
    audio_mime: Optional[str],
    audio_filename: Optional[str],
    language: str,
) -> Tuple[bool, Optional[str], Optional[str]]:
    """
    Validates photo, audio, and language.
    Returns (is_valid, error_code, error_message).
    """
    # 1. Validate Language
    if language not in ("te", "hi", "en"):
        return False, "validation_failed", f"Unsupported language: '{language}'. Allowed: te, hi, en"

    # 2. Validate Photo Size
    if len(photo_content) > MAX_PHOTO_SIZE:
        return False, "file_too_large", "Photo exceeds maximum allowed size of 10 MB."
    if len(photo_content) == 0:
        return False, "validation_failed", "Photo file cannot be empty."

    # 3. Validate Audio Size
    if len(audio_content) > MAX_AUDIO_SIZE:
        return False, "file_too_large", "Audio exceeds maximum allowed size of 5 MB."
    if len(audio_content) == 0:
        return False, "validation_failed", "Audio file cannot be empty."

    # 4. Check photo extension/mime
    ext = (photo_filename or "").lower().split(".")[-1]
    if ext not in ("jpg", "jpeg", "png") and (photo_mime or "").lower() not in ALLOWED_IMAGE_TYPES:
        return False, "validation_failed", "Photo must be a JPEG or PNG image."

    # 5. Check audio extension/mime
    audio_ext = (audio_filename or "").lower().split(".")[-1]
    if audio_ext not in ("wav", "mp3", "mp4", "m4a", "aac", "ogg") and (audio_mime or "").lower() not in ALLOWED_AUDIO_TYPES:
        return False, "validation_failed", "Audio must be in WAV, MP3, M4A, AAC, or OGG format."

    return True, None, None
