from typing import List, Optional
from pydantic import BaseModel, Field, field_validator


class ConfirmListingRequest(BaseModel):
    artisan_id: Optional[str] = None
    original_image_url: Optional[str] = None
    enhanced_image_url: Optional[str] = None
    image_warning: bool = False
    transcript: Optional[str] = None
    title: str = Field(..., max_length=80)
    description: str = Field(..., max_length=400)
    category: str
    tags: List[str] = Field(default_factory=list)
    final_price: float
    suggested_price: float

    @field_validator("final_price", "suggested_price")
    @classmethod
    def validate_positive_price(cls, v: float) -> float:
        if v < 0:
            raise ValueError("Price cannot be negative")
        return v


class LLMListingSchema(BaseModel):
    title: str = Field(..., max_length=120)
    description: str = Field(..., max_length=600)
    category: str
    tags: List[str] = Field(default_factory=list)
    suggested_price: float

    @field_validator("suggested_price")
    @classmethod
    def validate_price_range(cls, v: float) -> float:
        # Clamp to realistic range if needed
        return max(50.0, min(float(v), 50000.0))
