import sys, asyncio, json, logging
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from app.config import settings
import httpx
logging.disable(logging.CRITICAL)
async def main():
    print('TTS provider recognized:', settings.TTS_PROVIDER in ('sarvam', 'google'))
    print('Sarvam configured:', bool(settings.SARVAM_API_KEY))
    print('TTS provider is Sarvam:', settings.TTS_PROVIDER == 'sarvam')
    async with httpx.AsyncClient(timeout=20) as client:
        response = await client.post('https://api.sarvam.ai/text-to-speech',
            headers={'api-subscription-key': settings.SARVAM_API_KEY},
            json={'text': 'Handmade clay pot.', 'language_code': 'en-IN', 'model': settings.SARVAM_MODEL, 'output_audio_codec': 'wav'})
        print('Sarvam HTTP:', response.status_code)
        data = response.json()
        print('Response fields:', list(data.keys()))
        if isinstance(data.get('detail'), list): print('Invalid fields:', [e.get('loc') for e in data['detail']])
        response = await client.get('https://serpapi.com/search.json', params={'engine': 'google_shopping', 'q': 'handmade wooden bowl India', 'gl':'in', 'hl':'en', 'api_key':settings.SERPAPI_KEY}, timeout=15)
        print('SerpApi HTTP:', response.status_code)
        result=response.json()
        print('SerpApi error present:', 'error' in result)
        print('Shopping count:', len(result.get('shopping_results', [])))
        print('Currencies:', json.dumps([x.get('price', '') for x in result.get('shopping_results', [])[:3]], ensure_ascii=True))
asyncio.run(main())

