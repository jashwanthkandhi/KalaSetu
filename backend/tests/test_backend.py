import os
from pathlib import Path
import pytest
from fastapi.testclient import TestClient

# Set MOCK_MODE for deterministic unit testing
os.environ["MOCK_MODE"] = "true"
from app.main import app

client = TestClient(app)
FIXTURES_DIR = Path(__file__).parent / "fixtures"


def test_health_check():
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "ok"
    assert data["stack"] == "SIH26090"
    assert data["mock_mode"] is True


def test_validation_unsupported_language():
    photo_path = FIXTURES_DIR / "pottery_sample.jpg"
    audio_path = FIXTURES_DIR / "sample_audio.wav"

    with open(photo_path, "rb") as p, open(audio_path, "rb") as a:
        response = client.post(
            "/api/v1/listings/process",
            files={
                "photo": ("pottery.jpg", p, "image/jpeg"),
                "audio": ("audio.wav", a, "audio/wav"),
            },
            data={"language": "invalid_lang"},
        )
    assert response.status_code == 422
    data = response.json()
    assert data["success"] is False
    assert data["error"]["code"] == "validation_failed"


def test_validation_empty_photo():
    audio_path = FIXTURES_DIR / "sample_audio.wav"

    with open(audio_path, "rb") as a:
        response = client.post(
            "/api/v1/listings/process",
            files={
                "photo": ("pottery.jpg", b"", "image/jpeg"),
                "audio": ("audio.wav", a, "audio/wav"),
            },
            data={"language": "te"},
        )
    assert response.status_code == 422
    assert response.json()["error"]["code"] == "validation_failed"


def test_process_listing_pipeline_success():
    photo_path = FIXTURES_DIR / "pottery_sample.jpg"
    audio_path = FIXTURES_DIR / "sample_audio.wav"

    with open(photo_path, "rb") as p, open(audio_path, "rb") as a:
        response = client.post(
            "/api/v1/listings/process",
            files={
                "photo": ("pottery.jpg", p, "image/jpeg"),
                "audio": ("audio.wav", a, "audio/wav"),
            },
            data={"language": "te"},
        )
    assert response.status_code == 200
    data = response.json()
    assert data["success"] is True
    assert "request_id" in data
    assert "transcript" in data
    assert "category" in data
    assert "listing" in data
    assert data["listing"]["suggested_price"] > 0
    assert isinstance(data["listing"]["suggested_price"], (int, float))


def test_confirm_listing_success():
    payload = {
        "artisan_id": None,
        "original_image_url": "https://example.com/original.jpg",
        "enhanced_image_url": "https://example.com/enhanced.jpg",
        "image_warning": False,
        "transcript": "Terracotta water pitcher",
        "title": "Terracotta Water Pitcher",
        "description": "Handcrafted clay pot for cooling water naturally.",
        "category": "Pottery",
        "tags": ["terracotta", "handmade", "kitchen"],
        "final_price": 450.0,
        "suggested_price": 450.0,
    }
    response = client.post("/api/v1/listings/confirm", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["success"] is True
    assert "product_id" in data
