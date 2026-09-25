from typing import Literal
from uuid import UUID
from pydantic import BaseModel, ConfigDict, Field, field_validator

Category = Literal['Pottery', 'Textiles', 'Bamboo', 'Wood', 'Home Decor', 'Jewellery', 'Paintings', 'Leather', 'Other']
Language = Literal['te', 'hi', 'en']


class LLMListingSchema(BaseModel):
    model_config = ConfigDict(str_strip_whitespace=True)
    title: str = Field(min_length=1, max_length=80)
    description: str = Field(min_length=1, max_length=400)
    category: Category
    tags: list[str] = Field(min_length=1, max_length=12)
    suggested_price: float = Field(ge=0, le=50000, allow_inf_nan=False)
    attributes: dict[str, str] = Field(default_factory=dict, max_length=12)

    @field_validator('tags')
    @classmethod
    def valid_tags(cls, tags):
        if any(not tag.strip() or len(tag) > 40 for tag in tags):
            raise ValueError('Tags must contain 1-40 characters')
        return list(dict.fromkeys(tag.strip() for tag in tags))


class PublicProfile(BaseModel):
    model_config = ConfigDict(str_strip_whitespace=True)
    display_name: str = Field(default='', max_length=80)
    shop_name: str = Field(default='', max_length=80)
    craft: str = Field(default='', max_length=80)
    location: str = Field(default='', max_length=120)
    bio: str = Field(default='', max_length=400)
    contact: str = Field(default='', max_length=100)
    contact_public: bool = False


class ConfirmListingRequest(LLMListingSchema):
    product_id: UUID
    original_image_url: str = Field(min_length=1, max_length=2048)
    enhanced_image_url: str | None = Field(default=None, max_length=2048)
    image_warning: bool = False
    transcript: str | None = Field(default=None, max_length=10000)
    final_price: float = Field(gt=0, le=50000, allow_inf_nan=False)
    language: Language = 'en'
    public_profile: PublicProfile = Field(default_factory=PublicProfile)
    market_data: dict = Field(default_factory=dict)
    status: Literal['saved', 'archived'] = 'saved'

    @field_validator('original_image_url', 'enhanced_image_url')
    @classmethod
    def validate_url(cls, value):
        if value is not None and not value.startswith('https://'):
            raise ValueError('A stored HTTPS image is required')
        return value


class AssistRequest(BaseModel):
    listing: LLMListingSchema
    instruction: str = Field(min_length=1, max_length=500)
    language: Language = 'en'


class TTSRequest(BaseModel):
    model_config = ConfigDict(str_strip_whitespace=True)
    text: str = Field(min_length=1, max_length=500)
    language: Language = 'te'
