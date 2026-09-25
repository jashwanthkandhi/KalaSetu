import math
import statistics
import httpx
from ..config import settings


class PricingService:
    def market_guidance(self, category: str, tags: list[str], llm_suggested: float = 0) -> dict:
        estimate = float(llm_suggested) if math.isfinite(llm_suggested) else 0
        fallback = dict(suggested_price=max(0, min(estimate, 50000)), source='ai_estimate',
                        low=None, high=None, comparables=[],
                        explanation='Live market data is unavailable. This AI estimate is not a market quote; set your own price.')
        if not settings.SERPAPI_KEY or settings.MOCK_MODE:
            return fallback
        try:
            query = ' '.join([category, *tags[:3], 'handmade India'])
            response = httpx.get('https://serpapi.com/search.json', params={
                'engine': 'google_shopping', 'q': query, 'location': 'India',
                'hl': 'en', 'gl': 'in', 'api_key': settings.SERPAPI_KEY, 'num': 5,
            }, timeout=5)
            response.raise_for_status()
            comparables = []
            for item in response.json().get('shopping_results', [])[:10]:
                price = item.get('extracted_price')
                label = str(item.get('price', ''))
                if not isinstance(price, (float, int)) or not math.isfinite(price) or not 0 < price <= 50000:
                    continue
                if not any(currency in label for currency in ('₹', 'INR', 'Rs.')):
                    continue
                comparables.append({'title': str(item.get('title', ''))[:160], 'price': price,
                                    'source': str(item.get('source', ''))[:100]})
            if comparables:
                prices = [item['price'] for item in comparables]
                return dict(suggested_price=round(statistics.median(prices), 2), source='google_shopping',
                            low=min(prices), high=max(prices), comparables=comparables,
                            explanation='Median of current Google Shopping India results. Materials, size and workmanship may differ.')
        except (httpx.HTTPError, ValueError, TypeError):
            pass
        return fallback

    def suggest_price(self, category, tags, llm_suggested=0):
        return self.market_guidance(category, tags, llm_suggested)['suggested_price']


pricing_service = PricingService()
