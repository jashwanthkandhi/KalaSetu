# KalaSetu (కళాసేతు)

> **From Voice and Vision to Marketplace in Seconds**  
> An AI-powered catalog assistant bridging traditional Indian artisans directly to e-commerce marketplaces through multilingual voice and computer vision.

---

## 🌟 Overview

**KalaSetu** enables grassroots Indian artisans to create professional, marketplace-ready product listings in under 30 seconds. Artisans simply take a photo of their craft and speak naturally in their native mother tongue (Telugu, Hindi, or English). 

The platform leverages edge-to-cloud AI pipelines to transcribe regional dialects, detect craft categories, generate optimized titles, descriptions, and tags, discover real-time market prices, and synchronize listings across cloud catalogs.

---

## 🏗️ Architecture

```
Artisan opens App → Selects Language → Takes Photo → Speaks in Telugu/Hindi
                           │
                 [Offline Detection]
                ┌──────────┴──────────┐
          (If Online)            (If Offline)
                │                     │
                │              Room Offline Queue
                │         (Syncs when signal returns)
                │                     │
                ▼                     ▼
┌───────────────────────────────────────────────────────────┐
│                 FastAPI AI Engine                         │
│                                                           │
│ 1. Sarvam AI Saaras / Whisper STT → Indic Speech-to-Text  │
│ 2. PyTorch MobileNetV3-Small      → Craft Classification  │
│ 3. NVIDIA NIM Nemotron LLM        → Catalog Synthesis     │
│ 4. SerpApi (Google Shopping)      → Real-time Pricing     │
│ 5. Supabase Database & Storage    → Cloud Persistence     │
│ 6. Sarvam Bulbul / Android TTS    → Voice Guidance        │
└───────────────────────────────────────────────────────────┘
                           │
                           ▼
Artisan Reviews & Edits Listing Card → Confirms → Saved to Catalog
```

---

## 📱 Features

- **Multilingual Indic Voice Ingestion**: Native speech recognition tuned for Indian languages (Telugu, Hindi, English).
- **Computer Vision Craft Classification**: Automatically identifies craft domains (Pottery, Textiles, Bamboo, Wood, Jewellery, Leather, Paintings).
- **AI-Powered Catalog Generation**: High-quality English titles, rich artisan-grounded descriptions, search tags, and pricing via NVIDIA NIM Nemotron LLM.
- **Fair-Market Pricing Discovery**: Real-time Google Shopping price benchmarks with intelligent cost-plus margin safeguards.
- **Offline-First Resilience**: Robust Room database queue with automatic synchronization when network connectivity restores.
- **Interactive Voice Guide**: Text-to-Speech assistant reading listing summaries aloud for low-literacy artisans.

---

## 📁 Repository Structure

```
├── app/                          # Native Android Jetpack Compose Application
│   ├── src/main/java/com/example/
│   │   ├── core/
│   │   │   ├── data/             # Room Database, DAOs, Repository
│   │   │   ├── model/            # Domain Models & Enums
│   │   │   ├── network/          # Retrofit Client & Network Services
│   │   │   └── service/          # Audio Recorder, Player, TTS, Mock Service
│   │   └── ui/
│   │       ├── screens/          # Onboarding, Capture, Processing, Review, Catalog
│   │       ├── viewmodel/        # KalaSetuViewModel (StateFlow & MVI)
│   │       └── theme/            # Material 3 Warm Artisan Theme System
│   └── build.gradle.kts          # Android build configuration
│
├── backend/                      # FastAPI AI & Cloud Integration Backend
│   ├── app/
│   │   ├── routes/               # /health, /process, /confirm, /tts
│   │   ├── services/             # STT, LLM, Vision, Pricing, Storage, TTS
│   │   ├── schemas/              # Pydantic v2 Request/Response validation
│   │   └── utils/                # File lifecycle, validation, image compression
│   ├── tests/                    # Pytest test suite & Live API probe
│   ├── requirements.txt          # Python dependencies
│   └── .env.example              # Environment variables template
│
├── supabase/
│   └── schema.sql                # PostgreSQL Database Schema & RLS policies
│
└── implementationplan.txt        # Detailed technical implementation blueprint
```

---

## 🚀 Getting Started

### 1. Backend Setup

1. **Navigate to the backend directory:**
   ```bash
   cd backend
   ```

2. **Create and activate a Python virtual environment (Python 3.11+):**
   ```bash
   python -m venv .venv
   # On Windows:
   .\.venv\Scripts\activate
   # On Linux/macOS:
   source .venv/bin/activate
   ```

3. **Install dependencies:**
   ```bash
   pip install -r requirements.txt
   ```

4. **Configure environment variables:**
   ```bash
   cp .env.example .env
   # Edit .env with your provider keys:
   # - SUPABASE_URL & SUPABASE_KEY
   # - NVIDIA_NIM_API_KEY
   # - SARVAM_API_KEY
   # - SERPAPI_KEY
   ```

5. **Start the FastAPI server:**
   ```bash
   uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
   ```

6. **Verify health:**
   Navigate to `http://localhost:8000/health` or `http://localhost:8000/docs`.

---

### 2. Android App Setup

1. Open **Android Studio** (Ladybug or newer recommended).
2. Choose **Open** and select the `Kalasetu` root folder.
3. Allow Gradle to sync dependencies.
4. Launch an emulator (e.g. Pixel 8/9 with API 34+).
5. Click **Run** (`Shift + F10`).
   *(Note: The Android client automatically communicates with the host machine backend via `http://10.0.2.2:8000`).*

---

## 🧪 Testing

- **Backend Pytest Suite:**
  ```bash
  cd backend
  pytest tests/test_backend.py -v
  ```

- **Live Cloud API Probe:**
  ```bash
  cd backend
  python tests/probe_apis.py
  ```

- **Android Unit & Robolectric Tests:**
  ```bash
  ./gradlew testDebugUnitTest
  ```

---

## 📄 License
This project is licensed under the Apache 2.0 License.
