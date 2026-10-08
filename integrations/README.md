# Patchy integrations

Patchy reads your project's manifests (`package.json`, `pom.xml`, `build.gradle(.kts)`, `requirements.txt`,
`pyproject.toml`, `Pipfile`, `go.mod`, `Cargo.toml`, `Gemfile`, `composer.json`, `Dockerfile`) and shows the
latest AI-summarized release notes for every language, framework and library in them. Packages Patchy hasn't
seen before are looked up on the fly (npm, PyPI, crates.io, RubyGems, Packagist, Go modules → GitHub releases)
and added to the shared tech catalog for everyone.

## Install (no account needed)

```bash
curl -fsSL https://raw.githubusercontent.com/BlueRasputin/Patchy-patch-notes-app/main/install.sh | sh
```

This installs the VS Code extension into every VS Code-compatible editor it finds. Append `-s -- --mcp` after `sh`
to also install the `patchy-mcp` server globally via npm. The installer downloads from the latest GitHub
release, which `.github/workflows/release.yml` builds when you push a `v*` tag
(`git tag v0.1.0 && git push origin v0.1.0`). Bump the versions in `vscode/package.json`, `mcp/package.json` and
`intellij/build.gradle.kts` first.

All clients share one local dataset in `~/.patchy/` (owner-only permissions):

| File | Contents |
|---|---|
| `credentials.json` | `{ "apiUrl", "token" }`, written by editor sign-in. `PATCHY_API_URL` / `PATCHY_TOKEN` env vars override it. |
| `data.json` | Latest note per tech, which releases you've already seen, and each project's tech list. Used offline. |

Your project tech lists stay on your machine. Signing in is optional: it mirrors your techs into your website
"Bay" and enables breaking-change emails.

## VS Code (also Cursor / Windsurf / VSCodium): `vscode/`

- Scans the workspace on startup, whenever a manifest changes, and every `patchy.pollMinutes`.
- New releases open a **Patchy** tab and a notification (a warning for breaking/security changes). The status bar shows tracked/breaking counts.
- **GitHub Copilot**: Patchy's tools are registered for agent mode automatically, and `@patchy` in Copilot Chat answers questions about your dependencies' releases (`@patchy /breaking`).
- **Patchy: Sign In** opens the website's `/connect` page and returns a token to the editor. Afterwards Patchy offers to add its tools to Claude Code, Claude Desktop, OpenAI Codex and Gemini CLI (**Patchy: Connect AI Assistants** to change this later).

Run from source: `cd integrations/vscode && npm install`, open the folder in VS Code, and press F5.
Package it with `npx @vscode/vsce package`. After editing `integrations/mcp`, refresh the bundled copy with
`rm -rf node_modules/patchy-mcp && npm install`.

## AI assistants (MCP server): `mcp/`

One stdio MCP server provides the tools `project_updates`, `breaking_changes`, `patch_notes` and `my_techs`.
Run `npm install` in `integrations/mcp` first. The VS Code extension can do all of this for you.

| Assistant | Setup |
|---|---|
| Claude Code | `claude plugin marketplace add /path/to/Patchy-patch-notes-app` then `claude plugin install patchy@patchy` (includes a skill that checks breaking changes before upgrades) |
| Claude Desktop | `claude_desktop_config.json` → `"mcpServers": { "patchy": { "command": "/abs/path/to/node", "args": ["/abs/path/integrations/mcp/server.js"] } }` |
| OpenAI Codex (CLI / IDE) | `~/.codex/config.toml` → `[mcp_servers.patchy]` with `command = "node"` and `args = ["/abs/path/integrations/mcp/server.js"]` |
| Gemini CLI | `gemini extensions link /path/to/integrations/mcp` |
| GitHub Copilot | Automatic with the VS Code extension; otherwise add it to `.vscode/mcp.json` under `servers` |

ChatGPT on the web needs a remote (HTTPS) MCP server, which isn't built yet.

## IntelliJ: `intellij/`

See [intellij/README.md](intellij/README.md). The plugin provides the same scan, alerts and Markdown updates tab, and signs in by pasting the token from `/connect`.
