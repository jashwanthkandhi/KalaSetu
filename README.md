# KalaSetu

KalaSetu is a Kotlin/Jetpack Compose artisan catalog app with a FastAPI backend, Room drafts and Supabase publishing. An artisan captures a photo and a voice note, reviews a generated listing, chooses a selling price, and explicitly confirms publication.

## Product

- Home dashboard with actual local catalog counts and recent work.
- Searchable catalog, category/status filters, sorting, product details, duplication, archival and deletion.
- Durable capture drafts, an offline processing queue, automatic connected retries and a sync center. Processing produces a draft; it never publishes without review.
- Editable profile and explicit public-phone preference. Discovery, local favorites, Android sharing and a dialer contact action.
- AI editing proposals, editable attributes, original/enhanced photo comparison and price provenance.
- Light/dark/system appearance, larger text, contrast, simple view, notification preferences and speech settings.
- Telugu, Hindi and English speech and common interface labels. Some explanatory/error copy remains English; full interface localization is unfinished.
- Insights use local catalog records. Buyer views, orders and sales are not tracked.

## Run locally

1. Install JDK 21 and Android SDK 36; set the SDK path in your ignored `local.properties`.
2. Create a Python environment under `backend/.venv` and install `backend/requirements.txt`.
3. Copy `backend/.env.example` to `backend/.env` and configure your services. Never commit credentials. The Android build does not import this file.
4. For a new database, apply `supabase/schema.sql`, then `supabase/product_expansion.sql`. For the existing database, apply only the additive expansion migration. It preserves rows but closes the original anonymous table access. Old ownerless rows remain private and need an explicit ownership migration if they must be published.
5. Configure the `listing-images` storage bucket. The original photo must upload successfully for processing to succeed. Product image URLs are public; do not upload private documents.
6. From `backend`, run `.venv\Scripts\python.exe -m uvicorn app.main:app --host 0.0.0.0 --port 8000`.
7. Build with `.\gradlew.bat assembleDebug`. Debug defaults to the Android emulator backend at `http://10.0.2.2:8000/`. Override using `'-PBACKEND_URL=https://your-backend.example/'`. Release blocks cleartext HTTP and requires your signing configuration.

## Provider behavior

Sarvam speech is primary, with configured Whisper fallback. Sarvam/Google speech synthesis and device TTS support reading listings. NVIDIA NIM or OpenAI produces validated structured listing proposals. Generation can fail or invent details; artisan review remains required.

The MobileNet ImageNet mapping is a category hint, not a validated craft classifier. Missing weights return Other at zero confidence. Qwen enhancement requires a deployed, compatible `QWEN_IMAGE_ENDPOINT`; without it the original is retained and the UI discloses that enhancement is unavailable. Supplying an API key alone does not enable image editing.

SerpApi Shopping India results provide a median/range when available. Otherwise an explicitly labeled AI estimate is shown. Comparable materials and workmanship may differ, so guidance is not a valuation or guaranteed selling price.

`MOCK_MODE` is false by default. Isolated tests replace provider clients; successful contract tests are not evidence of live provider quality.

## Data and access

The installation creates a random owner capability used by the backend to scope cloud writes and private catalog reads. This is installation ownership, not a user account: uninstalling or clearing app data loses that capability. Account recovery, cross-device identity, abuse controls and a production gateway remain deployment work.

The database exposes a sanitized marketplace view, never owner hashes, private transcripts or email. Phone is exposed only with opt-in. RLS and owner-checked RPCs protect writes. Do not restore the old demo policies after migration.

## Verification

Run `.\gradlew.bat assembleDebug testDebugUnitTest lintDebug` and, from `backend`, `.venv\Scripts\python.exe -m pytest tests/test_backend.py tests/test_services.py tests/test_provider_contracts.py -q`.

`node scripts/test-migration.mjs` validates the SQL in an isolated PGlite database (install `@electric-sql/pglite` under ignored `.test-tools` first). Live probe scripts are separate, may incur provider usage, and storage probes write a disposable cloud object; run them only with authorization.

See [the baseline audit](docs/PRODUCT_AUDIT.md) and [verification report](docs/VERIFICATION.md) for evidence and remaining gates. Do not treat this branch as fully verified for production until those gates are closed.
