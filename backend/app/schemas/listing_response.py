from typing import List, Optional
from pydantic import BaseModel, Field


class CategoryInfo(BaseModel):
    name: str
    confidence: float


class ListingDetail(BaseModel):
    title: str
    description: str
    category: str
    tags: List[str] = Field(default_factory=list)
    suggested_price: float


class ProcessSuccessResponse(BaseModel):
    success: bool = True
    request_id: str
    transcript: str
    category: CategoryInfo
    original_image_url: str
    enhanced_image_url: str
    image_warning: bool = False
    listing: ListingDetail


class ErrorDetail(BaseModel):
    code: str
    message: str


class ErrorResponse(BaseModel):
    success: bool = False
    request_id: str
    error: ErrorDetail


class ConfirmSuccessResponse(BaseModel):
    success: bool = True
    product_id: str


class HealthResponse(BaseModel):
    status: str
    model: str
    stack: str
    mock_mode: bool
