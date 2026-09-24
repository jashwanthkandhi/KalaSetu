import json
import logging
import re
from typing import Dict, Any, List
from openai import OpenAI
from ..config import settings
from ..schemas.listing_request import LLMListingSchema
from ..mock_data import MOCK_LISTINGS

logger = logging.getLogger("kalasetu.llm")

SYSTEM_PROMPT = """You are KalaSetu, an AI catalog assistant for Indian artisans.
Convert the artisan's spoken description into a professional English product listing.
Return ONLY a valid JSON object with these exact keys:
{
  "title": "string (max 80 chars)",
  "description": "string (max 400 chars)",
  "category": "one of: Pottery, Textiles, Bamboo, Wood, Jewellery, Paintings, Leather, Other",
  "tags": ["string"],
  "suggested_price": number
}
Rules: suggested_price MUST be a number (not a string), INR value 50–50000.
Do not invent details not in the transcript. No markdown outside the JSON."""


class LLMService:
    def __init__(self):
        self.nim_client = None
        if settings.NVIDIA_NIM_API_KEY:
            try:
                self.nim_client = OpenAI(
                    base_url=settings.NVIDIA_NIM_BASE_URL,
                    api_key=settings.NVIDIA_NIM_API_KEY,
                )
            except Exception as e:
                logger.warning(f"Could not initialize NIM client: {e}")

        # Optional OpenAI client fallback
        self.openai_client = None
        if settings.OPENAI_API_KEY:
            try:
                self.openai_client = OpenAI(api_key=settings.OPENAI_API_KEY)
            except Exception as e:
                logger.warning(f"Could not initialize OpenAI client: {e}")

    def _clean_json_text(self, text: str) -> str:
        text = text.strip()
        # Remove markdown code block fences if present
        match = re.search(r"```(?:json)?\s*(\{.*?\})\s*```", text, re.DOTALL)
        if match:
            return match.group(1).strip()
        # Find first { and last }
        start = text.find("{")
        end = text.rfind("}")
        if start != -1 and end != -1 and end > start:
            return text[start : end + 1]
        return text

    async def generate_listing(self, transcript: str, category_hint: str, language: str) -> Dict[str, Any]:
        """
        Generates structured listing using NVIDIA NIM Nemotron (with 1 retry on malformed JSON).
        """
        if settings.MOCK_MODE:
            logger.info("MOCK MODE: Returning deterministic mock listing.")
            return MOCK_LISTINGS.get(language, MOCK_LISTINGS["en"])

        prompt = f"Transcript: {transcript}\nCategory hint: {category_hint}"
        
        # Primary: NVIDIA NIM Nemotron
        client_to_use = self.nim_client or self.openai_client
        model_name = settings.NEMOTRON_MODEL if self.nim_client else "gpt-4o-mini"

        if not client_to_use:
            logger.warning("No LLM client configured. Returning mock fallback.")
            return MOCK_LISTINGS.get(language, MOCK_LISTINGS["en"])

        for attempt in range(2):
            timeout = 35.0 if attempt == 0 else 25.0
            messages = [
                {"role": "system", "content": SYSTEM_PROMPT},
                {"role": "user", "content": prompt},
            ]
            if attempt == 1:
                messages.append(
                    {"role": "user", "content": "Return ONLY the JSON object. No other text or markdown."}
                )

            try:
                logger.info(f"Calling LLM ({model_name}) attempt {attempt + 1}...")
                response = client_to_use.chat.completions.create(
                    model=model_name,
                    messages=messages,
                    temperature=0.3,
                    max_tokens=512,
                    timeout=timeout,
                )
                raw_content = response.choices[0].message.content or ""
                cleaned_json = self._clean_json_text(raw_content)
                parsed = json.loads(cleaned_json)
                
                # Validate with Pydantic
                validated = LLMListingSchema(**parsed)
                return validated.model_dump()
            except Exception as e:
                logger.warning(f"LLM generation attempt {attempt + 1} failed: {e}")
                if attempt == 0:
                    continue
                else:
                    # If NIM failed and OpenAI client exists as secondary
                    if client_to_use is self.nim_client and self.openai_client:
                        try:
                            logger.info("Attempting secondary OpenAI fallback...")
                            res = self.openai_client.chat.completions.create(
                                model="gpt-4o-mini",
                                messages=messages,
                                temperature=0.3,
                                max_tokens=512,
                                timeout=15.0,
                            )
                            parsed = json.loads(self._clean_json_text(res.choices[0].message.content or ""))
                            return LLMListingSchema(**parsed).model_dump()
                        except Exception as inner_e:
                            logger.error(f"Secondary LLM fallback also failed: {inner_e}")

                    raise RuntimeError("llm_failed")

        raise RuntimeError("llm_failed")


llm_service = LLMService()
