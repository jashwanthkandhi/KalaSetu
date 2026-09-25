# Product expansion verification

Status: feature branch; merge gate remains open. This report separates isolated tests from live checks. No production-readiness claim is made.

## Scope and changes

Kotlin/Compose, Room, Retrofit, FastAPI and the existing provider pipeline remain in place. Added dashboard, searchable/sortable catalog, detail and discovery screens, durable drafts, profile/settings, local insights, favorites, sharing, contact actions and proposed AI edits. Removed automatic demo seeding, fake media, success-shaped provider fallbacks and publication without review. Added owner-scoped cloud operations and a sanitized public view.

Draft and queue writes are transactional. Interrupted work recovers to a retryable state; processing yields a draft. Failed confirmations remain pending. Revision checks prevent background confirmation from replacing newer edits. Publication requires a valid photo URL, title, description, tags and price.

## Automated evidence

| Check | Observed result |
| --- | --- |
| FastAPI endpoint, service and provider contract tests | 71 passed; providers isolated, no cloud writes |
| Android JVM / Room / Compose suite | 21 passed on the latest completed full run; later refinements awaiting final run |
| Android debug build and lint | Passed on latest completed full run; warnings remain |
| PostgreSQL migration and authorization | 23 checks passed in disposable PGlite; migration applied twice |
| Configured secret scan | 5 configured secret values checked across 127 source paths and debug APK; zero matches |
| Android emulator tests | 2 passed on Pixel 9 / Android 17 emulator, including default ViewModel startup and draft publication validation |

Regression coverage includes bad/missing/oversized uploads, failed providers and saves, owner headers, schema validation, exact-original retention, audio transport formats, draft persistence, Room v1 migration, retry counts, stale confirmations, search/filter/sorting, screen navigation, dark/large text and missing-media gating. Mocked tests establish contracts, not provider quality.

The first aggregate screenshot run hit an intermittent Compose startup idle timeout. The isolated test and subsequent complete suite passed. A KSP background-thread warning occurred during one build that still passed; monitor toolchain stability.

## Live evidence

See `live-provider-results.json` and subsequent probe result files in this directory.

The supplied visual brief is recorded in `UI_GAP_ANALYSIS.md`. The visual refinement keeps the existing KalaSetu palette and navigation while bringing Capture, Processing, Review and Catalog closer to the supplied rounded-card, voice-first reference screens.

- Sarvam generated audio and transcribed synthetic speech in English, Hindi and Telugu. Text similarity for these three examples was 1.0, 0.966 and 1.0 respectively. This is not an accent/noise accuracy benchmark.
- NVIDIA listing generation returned validated Wood and Textiles examples in initial probes; a later Textiles request failed after bounded retry. Wood output included inferred details despite constraints, so human factual review remains necessary. Follow-up diagnostics showed two provider InternalServerError responses. A later live request returned a grounded Textile listing with valid schema. Bounded fallback to configured OpenAI is now implemented and isolated-tested; that later successful response came from NVIDIA, so live fallback itself is not verified.
- SerpApi returned HTTP 200 with 40 public shopping results in a direct diagnostic. The initial guidance probe fell back to a disclosed AI estimate. The follow-up production-service probe returned Google Shopping guidance (median INR 578, range INR 389–2666). Some results use coconut or bamboo instead of wood; the UI discloses that materials and workmanship differ. These results are a reference range, not a fair-value guarantee.
- The classifier loaded its weights but returned Other at low confidence for the available fixture. It is an ImageNet mapping, not a craft-trained model.
- Supabase baseline read worked. The expanded discovery schema was absent. Live migration has not been applied by this task.
- Qwen enhancement cannot be verified because no deployed image-edit endpoint is configured. The supported original-preservation fallback is contract-tested.

Provider contract references: [Sarvam TTS](https://docs.sarvam.ai/api-reference/text-to-speech/convert), [Sarvam STT](https://docs.sarvam.ai/api-reference/speech-to-text/transcribe), and [NVIDIA deployed image editing](https://docs.nvidia.com/nim/visual-genai/1.7.1/getting-started.html).

## UI and accessibility

Robolectric renders cover home, details, review, capture and dark/large settings under `app/build/reports/product-ui`. Screens are scrollable, use Material controls and support larger text and contrasting colors. Image errors now show an explicit state. Full TalkBack traversal, real camera/microphone permission behavior, noisy recordings and lower-end physical-device performance remain unverified. Common labels and core capture instructions are translated; some body/error text remains English.

## Security and privacy

Backend credentials no longer enter Android BuildConfig; HTTP body logging is disabled. Release cleartext traffic is disabled. Installation ownership is a random capability; writes check its hash and return an acknowledgement before marking a listing published. Public discovery omits owner hashes, transcripts and email, and includes phone only on explicit opt-in. The additive SQL retains existing rows while revoking broad demo access.

This is not account-based authentication. Uninstalling loses ownership capability. Rate limits, abuse controls, cloud storage policy hardening, signed release configuration and production deployment need separate verification. Uploaded product images may be public by URL. Deleting a listing removes its discovery record, not shared image objects.

## Remaining gates

1. Complete the user-controlled Supabase sign-in, verify the target project, apply `supabase/product_expansion.sql`, and verify owner save/read/archive/delete and public discovery with disposable test records.
2. Obtain explicit authorization for the repository image storage upload/readback/cleanup. Automatic approval review rejected the upload because destination authorization was not established. No upload occurred in that attempt.
3. Configure a deployed Qwen image-edit endpoint to verify actual enhancement; otherwise accept the disclosed original-only behavior as a scoped limitation.
4. Broader real-media, accessibility, localization and performance verification remains deployment work; the emulator smoke suite is passing.
5. Commit this verified work on the feature branch. Merge only after the external gates pass, then repeat build and regression tests on the merged revision. Keep the feature branch.

Browser inspection also encountered Google sign-in and was denied by automatic approval review because only Supabase access was authorized. Authentication remains with the user; the task will resume browser work after return to the Supabase project dashboard.


