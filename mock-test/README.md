# Mock end-to-end test

Shows how Patchy handles languages and packages it has never seen, and how new releases reach a developer's editor.

```bash
node mock-test/run.mjs
```

Requirements: local MySQL with the credentials in `server/src/main/resources/app.env`, network access, and
optionally a logged-in `gh` CLI (raises GitHub's API limit for the run). Takes about 4 minutes. The output of the
last run is saved in `last-run.txt` when run with `> mock-test/last-run.txt`.

## What it does

1. Starts a separate API on port 8091 with its own database, `patchy_mock` (your dev data isn't touched; drop it
   with `DROP DATABASE patchy_mock;`). The daily schedule is compressed to every 30 seconds.
2. **Phase 1:** scans five projects in languages Patchy doesn't track (`projects/`): Elixir (`mix.exs`), Gleam
   (`gleam.toml`), Scala (`build.sbt`), Julia (`Project.toml`) and Dart/Flutter (`pubspec.yaml`), exactly as the
   editor plugins do. Unknown languages and packages are queued for discovery; OSV security advisories come back
   immediately.
3. Discovery adds them to the shared tech table: GitHub releases, a CHANGELOG file (including monorepo package
   folders), endoflife.date for languages, or an AI-named repo / AI-found page and selector as a fallback. Every new
   note is queued and summarized.
4. **Phase 2:** scans again. Everything is matched now, nothing is re-queued, and nothing alerts.
5. **Phase 3:** injects new upstream releases: Gleam v9.9.0 (GitHub release with a breaking change and a CVE) and
   Julia 1.99.0 (endoflife.date plus a release-notes page).
6. **Phase 4:** scans again and prints what the user sees: the toast and the updates tab. A final scan confirms
   nothing alerts twice.

## What is mocked

`mock-upstream.mjs` runs on port 8099 and stands in for:

- **Gemini**: a deterministic keyword categorizer, so the summary pipeline runs without a key. Its headlines start
  with `[mock]`. Real summaries use the prompt in `SummaryService` and will read much better.
- **GitHub API and endoflife.date**: proxied to the real services, except the releases injected in phase 3.

Package registries (npm, PyPI, Hex, pub.dev, ...), raw changelog files and OSV.dev are real.
