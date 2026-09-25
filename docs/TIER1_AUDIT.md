# Review-readiness baseline — 2026-09-25

Baseline: main 9f78d82. Work branch: feature/kalasetu-tier1-review-ready.
Authentication is intentionally deferred to a future phase.

| Area | Classification | Severity / evidence |
|---|---|---|
| Android capture/catalog/details/profile/settings | Implemented, partial localization | Compose screens exist; English-only body text and small horizontal layouts remain |
| Room / WorkManager | Implemented | Transactional drafts, retries and confirmation revision checks; no durable remote job identity |
| Processing | Partially implemented | routes/listings.py /process awaits sequential STT, image, classifier, LLM and pricing; Android reports a local TRANSCRIBING state |
| Craft recognition | Unsuitable primary implementation | category_service uses generic ImageNet mapping; no image-plus-transcript craft analysis |
| Pricing | Unsuitable primary implementation | shopping-derived range and LLM estimate lack artisan cost inputs |
| Review audio / voice edits | Partial | Read-aloud exists; no summary transport controls; unconstrained full-listing revision |
| Distribution | Partial / missing | Text-only Android chooser; no image preview, social content or ONDC export |
| Ownership | Development mechanism | Random installation capability, SHA256 cloud lookup; no recovery across devices; keep behind a boundary |
| Supabase | Local schema implemented, live status unverified | complete_setup.sql added after prior work; old verification says expansion absent, so that report is stale until live inspection |
| Namespace | Prototype | com.example namespace; existing applicationId com.aistudio.kalasetu.vmbk |
| Production mocks | Explicit runtime switch exists | MOCK_MODE enables fixture responses; remove from production serving path while preserving test fixtures |
| Security | Partial | No Android privileged keys; bounded uploads; CORS wildcard; cloud storage policy and abuse controls need review |

## Baseline execution

- Android assembleDebug + testDebugUnitTest: BUILD SUCCESSFUL; tests were UP-TO-DATE, not newly executed.
- Backend first invoked from repository root: collection failed (wrong Python import root). Corrected backend-directory invocation: 71 tests passed in 3.42 seconds.
- These are baseline results, not evidence for the forthcoming changes.

## Intended architecture

Persist uploads in private Supabase storage and durable jobs in PostgreSQL. Return 202 before AI work. Bounded workers claim leased jobs atomically; polling returns actual persisted state and survives reconnects. STT and image work overlap. Validate image-plus-transcript analysis against a controlled taxonomy and decline unsupported traditions. Cost-plus pricing asks for artisan inputs. New tables remain server-only with RLS; the current development ownership boundary remains explicit.

The separate Remedy Implementation Plan was not attached; the supplied review-readiness brief is the implementation reference pending clarification.
