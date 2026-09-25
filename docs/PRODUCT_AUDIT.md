# Product expansion audit

Starting revision: `0b70bfa`, clean `main`. Development branch:
`feature/kala-setu-product-expansion`.

The new product brief overrides the original plan's Flutter and five-screen scope;
Kotlin, Compose, Room and FastAPI are retained.

## Independently observed baseline

* Android assembleDebug and existing unit-test task: successful (cached baseline).
* Existing tests exercise language, in-memory editing, arithmetic and a greeting
  screenshot, not the critical journey.
* Android network errors invoke MockApiService, including fabricated media.
* Recording permission denial still starts recording; microphone errors return
  simulated success. Capture permits generation without real media.
* Three sample products and a fictional artisan/contact are inserted by default.
* Confirmation failures are reported as saved, and offline processing auto-confirms
  without artisan review. Drafts live only in memory.
* Connectivity is manually toggled; no actual reconnect listener exists.
* Supabase failures can return fabricated URLs/IDs. There are no retrieval,
  ownership, update, deletion or discovery endpoints.
* Missing LLM configuration returns a fixture. Missing classifier returns Pottery
  at 0.87 confidence. Pricing contains fixed category prices without provenance.
* Image editing uses an assumed endpoint and an ineffective chat fallback.
* TTS MIME type is always MP3 even for WAV; audio upload MIME is always WAV.
* Secrets Gradle plugin imports the backend environment by denylist; newly added
  credentials could be bundled. HTTP body logging exposes private listing/media.
* Supabase demo policies allow anonymous insertion and reading private transcripts.

Provider claims in README are historical claims, not independent evidence.
Live checks, hardware checks and automated contract tests must be reported
separately. No production readiness or speech accuracy claim is justified by
fixture tests alone.
