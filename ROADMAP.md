# Patchy Roadmap

## Phase 0: Stabilize and speed up current app
- Remove avoidable frontend runtime errors and unused code paths.
- Keep crawler runs scheduled (cron or queue worker) instead of trigger-on-page-load patterns.
- Add indexes and lightweight query patterns for "latest patch note per tech" reads.
- Move secrets out of source-controlled files and into environment variables.

## Phase 1: Expand language and environment coverage
- Add the most-used coding languages, runtimes, and environments to the tech catalog (top of Stack Overflow survey / GitHub Octoverse rankings).
- Cover major package ecosystems (npm, Maven, PyPI, cargo, etc.) so most dependencies map to a tracked tech.
- Keep the catalog automated: each entry carries its patch-notes URL and content selector so the crawler picks it up without code changes.

## Phase 2: Personalized patch notes from package.json
- Add endpoint to accept `dependencies`, `devDependencies`, and `peerDependencies`.
- Let users drop in their `package.json` and have Patchy automatically build a custom list of their dependencies and languages.
- Map package names to tracked tech and return latest available summaries.
- Add UI upload flow (paste JSON / upload file) to show personalized summaries.
- Add telemetry for unmatched packages to improve package-to-tech mapping over time.

## Phase 3: UI refresh — GitHub visual parity, pirate accents
- Rework the UI toward GitHub's look and feel: clean typography, neutral palette, familiar card/list layouts, light and dark modes.
- Keep selected pirate elements as accents (logo, naming, small flourishes) rather than the dominant theme.
- Make the app friendlier: clearer navigation, readable patch note cards, sensible empty/loading states.
- Move the full tech catalog to its own page: a scrollable list of every tech with caret expanders to open each one's latest patch note, viewable without logging in; favorites still require an account.
- Keep The Bay as a list of the user's techs, using the same caret expanders so notes are easy to parse instead of a wall of full-size cards.

## Phase 4: Embeddable Patchy
- Build an embeddable version of Patchy (script tag or iframe widget) that developers can drop into their own site.
- Let the embed be configured with a tech list or a package.json so it shows only relevant patch notes.
- Keep the embed lightweight: backend does the heavy lifting, the widget only renders.

## Phase 5: True authentication
- Replace the current session-only login with real authentication: OAuth login via GitHub (Spring Security OAuth2 client), with email/password as a fallback.
- Store OAuth identities alongside existing user accounts and migrate current users.
- Protect API endpoints consistently (favorites, profile, bay) based on the authenticated principal instead of a raw session user id.

## Phase 6: Compare patch notes across tools
- Add compare API and side-by-side compare UI for selected techs.
- Add version/release metadata fields to patch notes for better diffing.
- Add "what changed since last release" summaries.

## Phase 7: Browser extension MVP
- Build Chrome extension with:
  - Content script: detect recognized domains/pages.
  - Background worker: send page URL + extracted content to Patchy backend.
  - Popup UI: show summary and save to user feed.
- Keep extension lightweight by letting backend do heavy summarization and storage.

## Phase 8: Email updates
- Add user-level notification preferences (daily/weekly/immediate).
- Queue and batch digests in background jobs.
- Send emails only when tracked tech has new summaries since last digest.

## Phase 9: Production hardening
- Add auth hardening (session security, CSRF strategy, stricter CORS).
- Add rate limiting and retry/backoff around model API calls.
- Add caching for latest summaries and source fetches.
- Add monitoring for crawler health, summary failures, and email delivery.
