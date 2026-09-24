import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

import httpx
from app.config import settings

print("================== LIVE API CONNECTIVITY AUDIT ==================")

# 1. SUPABASE
print("\n[1] SUPABASE DATABASE & STORAGE")
try:
    from app.services.supabase_service import supabase_service
    if supabase_service.client:
        try:
            res = supabase_service.client.table("products").select("id").limit(1).execute()
            print("  -> Database Connection: SUCCESS (connected to Supabase instance)")
            print(f"  -> Found rows in 'products': {len(res.data)}")
        except Exception as e:
            print("  -> Database Notice:", str(e)[:150])

        try:
            buckets = supabase_service.client.storage.list_buckets()
            names = [b.name for b in buckets] if buckets else []
            print(f"  -> Storage Connection: SUCCESS (Buckets: {names})")
        except Exception as e:
            print("  -> Storage Notice:", str(e)[:150])
    else:
        print("  -> Supabase Client: NOT INITIALIZED")
except Exception as e:
    print("  -> Supabase Error:", e)

# 2. SERPAPI
print("\n[2] SERPAPI (Google Shopping Real-Time Pricing)")
try:
    from serpapi import GoogleSearch
    params = {
        "engine": "google_shopping",
        "q": "Terracotta Pot handmade India",
        "location": "India",
        "api_key": settings.SERPAPI_KEY,
        "num": 3,
    }
    search = GoogleSearch(params)
    data = search.get_dict()
    if "error" in data:
        print(f"  -> SerpApi: FAILED - {data['error']}")
    elif "shopping_results" in data or "inline_shopping_results" in data:
        results = data.get("shopping_results", [])
        prices = [r.get("extracted_price") for r in results[:3] if "extracted_price" in r]
        print(f"  -> SerpApi: SUCCESS (live market results received, sample prices: {prices})")
    else:
        print(f"  -> SerpApi: Connected (Response keys: {list(data.keys())[:5]})")
except Exception as e:
    print("  -> SerpApi Error:", e)

# 3. SARVAM AI (Text to Speech)
print("\n[3] SARVAM AI (Indian Language Text-to-Speech)")
try:
    headers = {
        "api-subscription-key": settings.SARVAM_API_KEY,
        "Content-Type": "application/json",
    }
    payload = {
        "inputs": ["నమస్కారం, కళాసేతుకు స్వాగతం."],
        "target_language_code": "te-IN",
        "model": settings.SARVAM_MODEL,
    }
    resp = httpx.post("https://api.sarvam.ai/text-to-speech", headers=headers, json=payload, timeout=12.0)
    if resp.status_code == 200:
        data = resp.json()
        if "audios" in data and len(data["audios"]) > 0:
            print(f"  -> Sarvam TTS: SUCCESS (Audio synthesized, bytes length: {len(data['audios'][0])})")
        else:
            print("  -> Sarvam TTS: HTTP 200 but no audios returned.")
    else:
        print(f"  -> Sarvam TTS: FAILED (HTTP {resp.status_code}) - {resp.text[:150]}")
except Exception as e:
    print("  -> Sarvam TTS Error:", e)

# 4. NVIDIA NIM (Nemotron LLM)
print("\n[4] NVIDIA NIM (Nemotron LLM)")
try:
    base = settings.NVIDIA_NIM_BASE_URL.rstrip("/")
    headers = {
        "Authorization": f"Bearer {settings.NVIDIA_NIM_API_KEY}",
        "Content-Type": "application/json",
    }
    payload = {
        "model": settings.NEMOTRON_MODEL,
        "messages": [{"role": "user", "content": "Hello"}],
        "max_tokens": 10,
    }
    resp = httpx.post(f"{base}/chat/completions", headers=headers, json=payload, timeout=10.0)
    if resp.status_code == 200:
        print(f"  -> NVIDIA NIM: SUCCESS ({settings.NEMOTRON_MODEL} replied)")
    else:
        print(f"  -> NVIDIA NIM: FAILED (HTTP {resp.status_code}) - {resp.text[:150]}")
except Exception as e:
    print("  -> NVIDIA NIM Error:", e)

# 5. OPENAI / WHISPER (Speech-to-Text)
print("\n[5] OPENAI / WHISPER (Speech-to-Text)")
try:
    headers = {"Authorization": f"Bearer {settings.OPENAI_API_KEY}"}
    resp = httpx.get("https://api.openai.com/v1/models", headers=headers, timeout=10.0)
    if resp.status_code == 200:
        print("  -> OpenAI Whisper API: SUCCESS (Account active & models accessible)")
    else:
        print(f"  -> OpenAI Whisper API: FAILED (HTTP {resp.status_code}) - {resp.text[:150]}")
except Exception as e:
    print("  -> OpenAI Whisper Error:", e)

# 6. MOBILENETV3 (Local ML Model)
print("\n[6] MOBILENETV3 (Craft Category Classifier)")
try:
    from app.services.category_service import category_service
    if category_service.model is not None:
        print("  -> MobileNetV3: SUCCESS (ImageNet-pretrained model loaded & running on CPU)")
    else:
        print("  -> MobileNetV3: FAILED (Model weights not initialized)")
except Exception as e:
    print("  -> MobileNetV3 Error:", e)

print("\n=================================================================")
