# Patchy for IntelliJ

Watches each open project's dependency manifests and alerts you when a tracked tech ships a new release, with breaking and security changes flagged.

## Build

```sh
./gradlew buildPlugin
```

Requires JDK 21. The first build downloads the IntelliJ IDEA 2024.3 SDK. Output: `build/distributions/patchy-intellij-0.1.0.zip`.

## Install

Settings > Plugins > gear icon > **Install Plugin from Disk...** > pick the zip. Works on IntelliJ-based IDEs 2024.3 and newer.

## Sign in

**Tools > Patchy: Sign In** opens `{website}/connect?client=IntelliJ`. Copy the token it shows and paste it into the prompt. The token is saved to `~/.patchy/credentials.json`, which the VS Code extension and the MCP server share. The `PATCHY_TOKEN` environment variable overrides it. **Patchy: Sign Out** removes the token.

Signing in is optional. Without a token, scans still run anonymously. With one, each scan also adds the project's techs to your favorites (your "Bay") on the website.

## What it does

- When a project opens, every N minutes (default 60), and from **Tools > Patchy: Check for Updates**, it collects manifests up to 4 levels deep (`package.json`, `requirements.txt`, `pyproject.toml`, `Pipfile`, `Cargo.toml`, `go.mod`, `Gemfile`, `composer.json`, `pom.xml`, `build.gradle(.kts)`, `Dockerfile`) and sends them to `POST {api}/api/insights/project`.
- It saves the latest patch notes to `~/.patchy/data.json` (shared with the other Patchy clients), along with which note you have seen for each tech and which techs each project uses.
- When a tech has a newer note than the one you've seen, it shows a balloon (a warning if the release includes breaking or security changes). Clicking **Show updates** opens a Markdown tab with the notes and marks them seen.
- If the API can't be reached, a manual check shows the last saved notes instead.

Settings: **Settings > Tools > Patchy** (API URL, website URL, check interval).
