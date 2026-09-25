import io
from PIL import Image

MAX_PHOTO_SIZE = 10 * 1024 * 1024
MAX_AUDIO_SIZE = 5 * 1024 * 1024


def valid_audio(data):
    return ((data.startswith(b'RIFF') and data[8:12] == b'WAVE' and len(data) > 44)
            or (data[4:8] == b'ftyp' and len(data) > 32)
            or (data.startswith(b'ID3') and len(data) > 32)
            or (data.startswith(b'OggS') and len(data) > 32)
            or (len(data) > 32 and data[0] == 255 and data[1] & 0xE0 == 0xE0))


def validate_media_files(photo_content, photo_mime, photo_filename, audio_content, audio_mime, audio_filename, language):
    if language not in ('te', 'hi', 'en'):
        return False, 'validation_failed', 'Supported languages are Telugu, Hindi and English.'
    if len(photo_content) > MAX_PHOTO_SIZE or len(audio_content) > MAX_AUDIO_SIZE:
        return False, 'file_too_large', 'Use a photo up to 10 MB and audio up to 5 MB.'
    if photo_mime not in ('image/jpeg', 'image/jpg', 'image/png', 'application/octet-stream'):
        return False, 'validation_failed', 'Photo must be JPEG or PNG.'
    if audio_mime not in ('audio/wav', 'audio/x-wav', 'audio/mpeg', 'audio/mp3', 'audio/mp4', 'audio/m4a', 'audio/x-m4a', 'audio/aac', 'audio/ogg', 'application/octet-stream'):
        return False, 'validation_failed', 'Audio must be WAV, MP3, M4A, AAC or OGG.'
    try:
        with Image.open(io.BytesIO(photo_content)) as image:
            if image.format not in ('JPEG', 'PNG') or image.width * image.height > 40_000_000:
                raise ValueError('Invalid image')
            image.verify()
    except Exception:
        return False, 'validation_failed', 'Photo is damaged or unsupported. Please choose another photo.'
    if not valid_audio(audio_content):
        return False, 'validation_failed', 'Audio is empty, damaged or unsupported. Please record again.'
    return True, None, None
