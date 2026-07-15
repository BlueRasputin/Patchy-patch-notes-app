# Patchy Roadmap

## Phase 0: Stabilize and speed up current app
- Remove avoidable frontend runtime errors and unused code paths.
- Keep crawler runs scheduled (cron or queue worker) instead of trigger-on-page-load patterns.
- Add indexes and lightweight query patterns for "latest patch note per tech" reads.
- Move secrets out of source-controlled files and into environment variables.

## Phase 1: Personalized patch notes from package.json
- Add endpoint to accept `dependencies`, `devDependencies`, and `peerDependencies`.
- Map package names to tracked tech and return latest available summaries.
- Add UI upload flow (paste JSON / upload file) to show personalized summaries.
- Add telemetry for unmatched packages to improve package-to-tech mapping over time.

## Phase 2: Compare patch notes across tools
- Add compare API and side-by-side compare UI for selected techs.
- Add version/release metadata fields to patch notes for better diffing.
- Add "what changed since last release" summaries.

## Phase 3: Browser extension MVP
- Build Chrome extension with:
  - Content script: detect recognized domains/pages.
  - Background worker: send page URL + extracted content to Patchy backend.
  - Popup UI: show summary and save to user feed.
- Keep extension lightweight by letting backend do heavy summarization and storage.

## Phase 4: Email updates
- Add user-level notification preferences (daily/weekly/immediate).
- Queue and batch digests in background jobs.
- Send emails only when tracked tech has new summaries since last digest.

## Phase 5: Production hardening
- Add auth hardening (session security, CSRF strategy, stricter CORS).
- Add rate limiting and retry/backoff around model API calls.
- Add caching for latest summaries and source fetches.
- Add monitoring for crawler health, summary failures, and email delivery.

