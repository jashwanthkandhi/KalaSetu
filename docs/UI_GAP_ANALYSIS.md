# KalaSetu UI gap analysis

The supplied screens are treated as the visual source of truth. The existing warm cream background, terracotta accents, white rounded cards, large controls, voice-first capture flow and artisan-friendly language remain the foundation.

## Critical

| Problem | Solution | Screen |
| --- | --- | --- |
| Capture looked like a generic form and did not make the photo and voice actions dominant. | Added a rounded photo capture card, compact online/language header, concentric voice recorder, waveform and bottom action treatment. | Add Product |
| Processing did not communicate which pipeline step was active. | Added a calm step card with completed, active and pending states driven by the real processing-stage callback. | AI Processing |
| Review did not clearly separate AI suggestions from artisan decisions. | Added an AI-generated disclosure and a five-item publish checklist while retaining editable fields and explicit confirmation. | Review Listing |

## Important

| Problem | Solution | Screen |
| --- | --- | --- |
| Catalog cards were a single-column list and did not match the supplied marketplace layout. | Reused the established product-card component in a responsive two-column grid with search, filters and sorting kept above it. | My Catalog |
| Missing or failed images left an ambiguous blank region. | Image components now show a localized loading or unavailable state. | Catalog, Detail, Review |
| Draft, pending and failed work could be hard to distinguish at a glance. | Status chips, sync-center copy and review checklist keep local state visible without inventing marketplace activity. | Catalog, Home, Sync |

## Useful

| Problem | Solution | Screen |
| --- | --- | --- |
| Longer Telugu and Hindi labels could crowd fixed controls. | Language selection uses compact pills and existing controls keep minimum touch heights rather than fixed text widths. | Capture, Settings, Navigation |
| AI editing could look like an automatic overwrite. | Proposals remain in a review dialog and the selling price stays unchanged until the artisan edits it. | Review Listing |
| Offline and failed provider work needed recovery paths. | Durable queue states, retry actions and explicit error copy keep the capture safe while reconnecting. | Capture, Processing, Sync |

## Deliberately not added

Buyer counts, sales charts, followers, orders, social-account connections and direct social publishing remain absent because the backend does not provide those facts or integrations. Android sharing is available where it is real; the UI does not claim a listing was published until cloud acknowledgement returns.

## Verification status

The refinement is source-reviewed and `git diff --check` is clean. The Gradle verification rerun is pending because the environment approval service is currently at its usage limit; the last completed build before this visual pass passed Android tests, lint, emulator smoke tests and backend tests.
