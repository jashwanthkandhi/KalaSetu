import io
import os
import tempfile
from pathlib import Path
from typing import Optional
from PIL import Image


def save_temp(data: bytes, suffix: str) -> Path:
    """Save bytes to a temporary file and return the Path."""
    with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as temp_file:
        temp_file.write(data)
        return Path(temp_file.name)


def compress_image(image_bytes: bytes, max_mb: float = 5.0, max_px: int = 1920) -> bytes:
    """
    Compress image to be under max_mb and resize if longer edge exceeds max_px.
    Returns JPEG bytes.
    """
    try:
        image = Image.open(io.BytesIO(image_bytes))
        
        # Convert RGBA/P to RGB for JPEG saving
        if image.mode in ("RGBA", "P"):
            image = image.convert("RGB")

        # Resize if dimensions exceed max_px
        width, height = image.size
        longest_edge = max(width, height)
        if longest_edge > max_px:
            scale = max_px / float(longest_edge)
            new_width = int(width * scale)
            new_height = int(height * scale)
            image = image.resize((new_width, new_height), Image.Resampling.LANCZOS)

        quality = 85
        output = io.BytesIO()
        image.save(output, format="JPEG", quality=quality, optimize=True)
        compressed = output.getvalue()

        # If still larger than max_mb, reduce quality progressively
        max_bytes = int(max_mb * 1024 * 1024)
        while len(compressed) > max_bytes and quality > 30:
            quality -= 10
            output = io.BytesIO()
            image.save(output, format="JPEG", quality=quality, optimize=True)
            compressed = output.getvalue()

        return compressed
    except Exception:
        # Fallback to returning original bytes if Pillow fails
        return image_bytes


def cleanup_files(*paths: Optional[Path]) -> None:
    """Safely delete temporary files."""
    for p in paths:
        if p and p.exists():
            try:
                os.remove(p)
            except OSError:
                pass
