import os
from pathlib import Path
from typing import Optional
from pydantic import Field, model_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    # Supabase
    SUPABASE_URL: str = Field(default="", alias="SUPABASE_URL")
    SUPABASE_KEY: str = Field(default="", alias="SUPABASE_KEY")
    NEXT_PUBLIC_SUPABASE_URL: Optional[str] = None
    NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY: Optional[str] = None

    # NVIDIA NIM (Nemotron & Qwen)
    NVIDIA_NIM_API_KEY: str = ""
    NVIDIA_NIM_BASE_URL: str = "https://integrate.api.nvidia.com/v1"
    NEMOTRON_MODEL: str = "nemotron-3-nano-omni-30b-a3b-reasoning"
    QWEN_IMAGE_API_KEY: str = ""
    QWEN_IMAGE_MODEL: str = "Qwen-Image 2.0"

    # STT
    WHISPER_MODEL: str = "base"
    OPENAI_API_KEY: Optional[str] = None
    GOOGLE_APPLICATION_CREDENTIALS: str = ""

    # Pricing
    SERPAPI_KEY: str = ""

    # TTS (P1)
    TTS_PROVIDER: str = "google"
    SARVAM_API_KEY: str = ""
    SARVAM_MODEL: str = "bulbul:v3"

    # Runtime
    MOCK_MODE: bool = False
    LOG_LEVEL: str = "INFO"

    model_config = SettingsConfigDict(
        env_file=[
            Path(__file__).resolve().parent.parent / ".env",
            Path(__file__).resolve().parent.parent.parent / ".env",
            ".env",
        ],
        env_file_encoding="utf-8",
        extra="ignore",
    )

    @model_validator(mode="after")
    def resolve_aliases_and_cleanup(self) -> "Settings":
        # Resolve Supabase URL
        if not self.SUPABASE_URL and self.NEXT_PUBLIC_SUPABASE_URL:
            self.SUPABASE_URL = self.NEXT_PUBLIC_SUPABASE_URL.strip().strip('"').strip("'")
        else:
            self.SUPABASE_URL = self.SUPABASE_URL.strip().strip('"').strip("'")

        # Resolve Supabase Key
        if not self.SUPABASE_KEY and self.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY:
            self.SUPABASE_KEY = self.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY.strip().strip('"').strip("'")
        else:
            self.SUPABASE_KEY = self.SUPABASE_KEY.strip().strip('"').strip("'")

        # Resolve Qwen Image API Key
        if not self.QWEN_IMAGE_API_KEY and self.NVIDIA_NIM_API_KEY:
            self.QWEN_IMAGE_API_KEY = self.NVIDIA_NIM_API_KEY.strip().strip('"').strip("'")

        # Clean strings
        self.NEMOTRON_MODEL = self.NEMOTRON_MODEL.strip().strip('"').strip("'")
        if not self.NEMOTRON_MODEL.startswith("nvidia/") and not "/" in self.NEMOTRON_MODEL:
            self.NEMOTRON_MODEL = f"nvidia/{self.NEMOTRON_MODEL}"
        self.QWEN_IMAGE_MODEL = self.QWEN_IMAGE_MODEL.strip().strip('"').strip("'")
        self.WHISPER_MODEL = self.WHISPER_MODEL.strip().strip('"').strip("'")
        self.SARVAM_MODEL = self.SARVAM_MODEL.strip().strip('"').strip("'")

        # Check if WHISPER_MODEL is an OpenAI key
        if self.WHISPER_MODEL.startswith("sk-") and not self.OPENAI_API_KEY:
            self.OPENAI_API_KEY = self.WHISPER_MODEL
            self.WHISPER_MODEL = "whisper-1"

        return self


settings = Settings()
