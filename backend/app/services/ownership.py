"""Development installation ownership boundary; replace here when accounts arrive."""
import hashlib
from fastapi import Header


def owner_key(x_owner_key: str = Header(min_length=32, max_length=128)) -> str:
    return x_owner_key


def owner_digest(capability: str) -> str:
    return hashlib.sha256(capability.encode()).hexdigest()
