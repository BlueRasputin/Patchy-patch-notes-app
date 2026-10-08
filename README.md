# Welcome to Patchy!
### Your First Mate for Navigating the Seas of Changelogs.


One of the biggest problems faced by developers today is attempting to keep track of all the different updates to software development languages, frameworks and libraries. With each new language learned, it becomes  why I built Patchy.

Patchy is a tool for finding the latest updates on development tools and software you use. It aggregates release notes from official sources and security advisories from OSV.dev, and lets the AI assistant you already use (Copilot, Claude, Codex or Gemini) summarize them, so it costs nothing to run and needs no API key.

Patchy is a means to streamline your development workflow and enhance your productivity by spending less time hunting down the latest updates on the frameworks and libraries you use every day. To begin, select your most utilized patch notes on the Homepage.



#### Tech stack:

- React (JS/HTML)
- CSS
- React Toastify for browser notifications
- Java
- Maven
- Node.js
- SpringBoot
- MySQL


#### Installation and run steps:

1. Clone repository to local machine
2. Copy `app.env.example` to `server/src/main/resources/app.env` and fill in the database password (no AI key needed)
3. open Patchy/Server file in IntelliJ (or similar IDE)
4. Open MySQL Workbench and create schema titles 'patchy'
5. start Patchy.js to instantiate database and start server
6. run the 'tech\_table\_sample\_data.sql' file in sql to instantiate tech table data (and add other queries for whatever tech you want to see on the site)
7. open Patchy/ui in VSCode
8. navigate into the ui folder and enter 'npm run dev' to open on localhost
9. create account and begin adding techs to your bay






[Link to Wireframe](https://docs.google.com/presentation/d/1R4BeHVkl3Rgo0GHLR5LxLE0B74aQVonB5e7gJyndMOw/edit?usp=drive_link)







[Link to ERD](https://www.figma.com/board/d9h6N4kq2ZuTrusCRf8lQC/Unit-2-Final-ERD?node-id=0-1&t=Fd4bsHiCHGkbfZxt-1)







#### Future development plans:

- Integrate a patch notes table to update daily rather than upon opening the bay
- Add an admin profile to add techs to tech list
- format patch notes with different fields such as version, and latest release, and type (framework, library, language)
- integrate a page for users without accounts to quickly view all patch notes with a search function




Tech catalog automation:

Patchy now uses a shared catalog file at `server/src/main/resources/tech-catalog.json` to drive both tech seeding and crawler targets.

To add a new dev tool or resource:

1. Add one entry to `server/src/main/resources/tech-catalog.json`
2. Set:
   * `name`: display name stored in the tech table
   * `patchNotesUrl`: canonical release notes or updates URL
   * `contentSelector`: CSS selector for the main release-note body
   * `contentStrategy`: extraction mode, usually `default`
3. Restart the backend so missing techs are inserted automatically
4. Run the crawler to begin collecting patch notes for the new source

Notes:

* `tech_table_sample_data.sql` is now just sample/bootstrap data. The backend will sync missing tech names from the catalog on startup.
* Most sources should use `contentStrategy: "default"`.
* Use a custom strategy only when a site has unstable or highly structured markup, like the existing `spring-blog` and `java-sections` handlers in `crawler/my-crawler/src/main.js`.
* The selector still matters. Adding a source is automated, but choosing a stable patch-note URL and reliable content selector still requires judgment.





## Embedding Patchy

Patchy ships an embeddable, read-only patch note feed at `/embed`. Drop it into any site with an iframe:

```html
<iframe
  src="https://your-patchy-host/embed?techs=React,Node.js,TypeScript"
  style="width: 100%; height: 480px; border: 1px solid #d0d7de; border-radius: 6px;"
  title="Patchy patch notes"
></iframe>
```

* `techs` is a comma-separated list of tracked tech names (case-insensitive). Omit it to show every tracked tech.
* The embed inherits the viewer's light/dark preference and renders no Patchy navigation, only the feed.

## Editors and AI assistants

```bash
curl -fsSL https://raw.githubusercontent.com/BlueRasputin/Patchy-patch-notes-app/main/install.sh | sh
```

Patchy lives in your editor: VS Code (with GitHub Copilot integration), IntelliJ, and an MCP server for
Claude, Codex and Gemini. See [integrations/README.md](integrations/README.md).

## Accounts and alerts

Configure these in `server/src/main/resources/app.env` (see `app.env.example`):

* **GitHub / Google sign-in**: create OAuth apps with the callbacks `http://localhost:8080/login/oauth2/code/github`
  and `.../google`. Providers without credentials are hidden on the login page.
* **Crawler**: set `CRAWLER_API_KEY` in both `app.env` and the crawler's environment. The backend rejects crawled notes without it.
* **Summaries at no cost**: when a release changes, the raw notes are stored (capped at 8,000 chars) and shown
  right away, then `SummaryService` summarizes them with Gemini's free tier into a headline, an urgency
  (critical/high/normal/low) and itemized categories (Breaking Changes with migration steps, Security with
  CVE/GHSA IDs, Deprecations, New Requirements, New Features, Bug Fixes, Performance, Known Issues, Documentation).
  Use a key from a Google AI Studio project with no billing account: it can't be charged, and when the daily
  quota runs out the queue waits for the next run. Without `GEMINI_API_KEY`, notes stay as published.
* **Schedule**: GitHub-release techs (31 of 36 in the catalog, plus discovered packages) are checked once a day in
  four rotating slices, 20:00-23:00 UTC (`patchy.refresh.cron`). Measured over 1,624 releases of tracked repos, 80%
  publish by 20:00 UTC and 96% by 23:00 UTC, mostly Tuesday to Thursday. The other five (Java, Python, Go, Swift,
  PostgreSQL) are crawled daily at 22:00 UTC by `.github/workflows/crawl.yml`.
* **Security advisories**: every editor scan checks the project's dependency versions against OSV.dev (free, no
  key), so vulnerabilities alert immediately instead of waiting for the daily schedule.
* **Tech discovery**: unknown packages are added from their GitHub releases. Without releases, Patchy reads the
  repo's CHANGELOG file, and failing that, Gemini picks the release-notes page from the package's registry and
  homepage links and proposes a CSS selector. The selector is kept only if a test extraction returns real release
  text; the daily Playwright crawl then uses it, and an empty extraction triggers rediscovery. Capped at
  `patchy.discovery.max-per-hour` (default 30) to respect GitHub's rate limit. A free `GITHUB_TOKEN` (no scopes)
  raises that limit from 60 to 5,000 requests an hour.

## Browser extension (MVP)

A Manifest V3 Chrome extension lives in `extension/`. It recognizes when the
current tab belongs to a tracked tech (matched against the tech catalog),
shows Patchy's latest summary for it, and can submit the page's content for
summarization ("Summarize this page": requires being signed in on the Patchy
website, and is deduped server-side, so unchanged pages cost nothing).

To install locally:

1. Start the backend (`cd server && ./mvnw spring-boot:run`)
2. Open `chrome://extensions`, enable Developer mode
3. "Load unpacked" → select the `extension/` folder
4. Visit a tracked site (e.g. react.dev) and click the Patchy icon
