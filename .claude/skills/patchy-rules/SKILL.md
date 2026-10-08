---
name: patchy-rules
description: Patchy-specific coding rules (React/Vite UI, Spring Boot API, crawler, browser extension). Use when writing, editing, or reviewing any code in this repo, together with the universal strict-rules skill.
---

These add to the universal `strict-rules` skill. On conflict, this file wins.

## Layout

- `ui/` — React 19 + Vite SPA. Pages in `src/pages/<Page>/`, shared pieces in `src/components/`, backend calls and client state helpers in `src/Services/`.
- `server/` — Spring Boot 3.5, Java 21, MySQL via JPA. Packages: `controllers`, `services`, `repositories`, `models`, `dto`, `config`.
- `crawler/my-crawler/` — Crawlee + Playwright; posts crawled pages to the backend.
- `extension/` — Manifest V3 browser extension, plain JS, no build step.
- `server/src/main/resources/tech-catalog.json` is the single source of truth for tracked techs (seeding, crawler targets, extension site matching). Add techs there, not in SQL or code.

## UI

- Every backend call goes through `apiFetch` in `src/Services/api.js` (it sets the base URL, JSON headers, and the session cookie). No raw `fetch` to the API.
- Render markdown with `react-markdown`; never `dangerouslySetInnerHTML`.
- Colors and radii come from the tokens in `src/index.css` (`--fg`, `--fg-muted`, `--bg`, `--bg-subtle`, `--bg-inset`, `--border`, `--accent`, `--radius`, ...). Check light and dark (`prefers-color-scheme`).
- GitHub-style look; pirate flavor only as accents (logo, naming, small flourishes).

## Server

- Controllers stay thin: validate input, resolve the user, call a service, return a DTO. Business logic lives in services.
- Return DTOs, not entities, for anything new. Never expose password hashes or other users' emails.
- Constructor injection for new classes.
- The current user comes from the session (`ApiController.getUserFromSession`) — never trust a `userId` path variable for reads or writes on user data.
- All Claude API calls go through `SummaryService`. Keep input capped and output structured.
- Secrets go in `server/src/main/resources/app.env` (gitignored) with a placeholder line in `app.env.example`.
- Schema changes rely on `ddl-auto=update`: new columns must be nullable or have a default so existing rows survive.

## Extension

- Plain JS, no bundler. Talk to the backend only through the API base constant in `popup.js`.
- Keep host permissions minimal; never request `<all_urls>` without the user's approval.
