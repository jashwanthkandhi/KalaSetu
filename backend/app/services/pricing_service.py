import logging
import statistics
from typing import List, Optional
from ..config import settings

logger = logging.getLogger("kalasetu.pricing")

CATEGORY_FALLBACK_PRICES = {
    "Pottery": 450.0,
    "Textiles": 1200.0,
    "Bamboo": 799.0,
    "Wood": 850.0,
    "Jewellery": 950.0,
    "Paintings": 1500.0,
    "Leather": 1100.0,
    "Other": 500.0,
}


class PricingService:
    def suggest_price(self, category: str, tags: List[str], llm_suggested: Optional[float] = None) -> float:
        """
        Calculates suggested market price in INR.
        1. Queries SerpApi Google Shopping for current market benchmarks.
        2. Falls back to cost-plus heuristic.
        Guards: minimum 99 INR, maximum 9999 INR.
        Never raises exceptions to caller.
        """
        if settings.MOCK_MODE:
            return llm_suggested or CATEGORY_FALLBACK_PRICES.get(category, 450.0)

        if settings.SERPAPI_KEY and settings.SERPAPI_KEY != "placeholder":
            try:
                from serpapi import GoogleSearch
                query = f"{category} handmade India"
                params = {
                    "engine": "google_shopping",
                    "q": query,
                    "location": "India",
                    "hl": "en",
                    "gl": "in",
                    "api_key": settings.SERPAPI_KEY,
                    "num": 5,
                }
                search = GoogleSearch(params)
                results = search.get_dict()
                shopping_results = results.get("shopping_results", [])
                prices = []
                for item in shopping_results[:5]:
                    extracted_price = item.get("extracted_price")
                    if extracted_price and isinstance(extracted_price, (int, float)):
                        prices.append(float(extracted_price))

                if prices:
                    median_price = statistics.median(prices)
                    # Clamp within bounds
                    clamped = max(99.0, min(float(median_price), 9999.0))
                    logger.info(f"SerpApi pricing successful for '{category}': ₹{clamped}")
                    return round(clamped, 2)
            except Exception as e:
                logger.warning(f"SerpApi query failed: {e}. Falling back to cost-plus.")

        # Fallback heuristic
        base = llm_suggested if (llm_suggested and 99.0 <= llm_suggested <= 9999.0) else CATEGORY_FALLBACK_PRICES.get(category, 450.0)
        return float(base)


pricing_service = PricingService()
