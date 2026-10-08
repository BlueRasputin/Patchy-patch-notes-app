#!/usr/bin/env node
// Patchy MCP server (stdio). One server covers Claude Code/Desktop, OpenAI Codex,
// Gemini CLI, GitHub Copilot (VS Code agent mode) and any other MCP client.
const { McpServer } = require("@modelcontextprotocol/sdk/server/mcp.js");
const { StdioServerTransport } = require("@modelcontextprotocol/sdk/server/stdio.js");
const { z } = require("zod");
const core = require("./core");

const server = new McpServer({ name: "patchy", version: require("./package.json").version });
const text = (value) => ({ content: [{ type: "text", text: value }] });
const projectPath = z.string().optional()
  .describe("Project directory to scan. Defaults to the directory the assistant was started in.");

function describeScan(scan, notes) {
  const lines = [];
  if (scan.offline) lines.push(`_Patchy API unreachable (${scan.error}); showing the local cache from ~/.patchy._`);
  if (scan.discovering.length > 0) {
    lines.push(`_Looking up release notes for ${scan.discovering.length} untracked packages (${scan.discovering.slice(0, 8).join(", ")}${scan.discovering.length > 8 ? ", …" : ""}); ask again in a few minutes._`);
  }
  lines.push(core.formatNotes(notes));
  if (scan.advisories.length > 0) lines.push(core.formatAdvisories(scan.advisories));
  lines.push("_Summaries are categorized by Patchy; notes still being summarized appear as published. Call out anything that needs action._");
  return lines.join("\n\n");
}

server.registerTool("project_updates", {
  title: "Project patch notes",
  description: "Latest release notes for every language, framework and library the project uses " +
    "(read from package.json, pom.xml, requirements.txt, go.mod, Cargo.toml, Gemfile, composer.json, build.gradle, Dockerfile). " +
    "Use before upgrading dependencies or when the user asks what changed in their stack.",
  inputSchema: { path: projectPath },
}, async ({ path }) => {
  const scan = await core.scanProject(path ?? process.cwd());
  return text(describeScan(scan, scan.notes));
});

server.registerTool("breaking_changes", {
  title: "Breaking changes in project dependencies",
  description: "Releases with breaking changes or security fixes, plus known vulnerabilities (OSV.dev) in the project's dependency versions. " +
    "Use before upgrading, when a build breaks after an update, or when reviewing dependency risk.",
  inputSchema: { path: projectPath },
}, async ({ path }) => {
  const scan = await core.scanProject(path ?? process.cwd());
  const breaking = scan.notes.filter(core.isBreaking);
  return text(breaking.length === 0 && scan.advisories.length === 0 && !scan.offline
    ? `No breaking changes or known vulnerabilities among the ${scan.techNames.length} tracked techs in this project.`
    : describeScan(scan, breaking));
});

server.registerTool("patch_notes", {
  title: "Patch notes for one tech",
  description: "Latest release notes for a single language, framework or library by name (e.g. React, Spring, Python).",
  inputSchema: { tech: z.string().describe("Tech name, e.g. \"React\"") },
}, async ({ tech }) => {
  const note = await core.findNote(tech);
  return text(note ? core.formatNotes([note]) : `Patchy doesn't track "${tech}" yet.`);
});

server.registerTool("my_techs", {
  title: "My followed techs",
  description: "Latest release notes for every tech the signed-in user follows on Patchy (their Bay).",
  inputSchema: {},
}, async () => {
  const config = core.loadConfig();
  if (!config.token) {
    return text("Not signed in to Patchy. Sign in from the Patchy VS Code/IntelliJ plugin, or set PATCHY_TOKEN.");
  }
  return text(core.formatNotes(await core.api(config, "GET", "/api/me/patch-notes")));
});

server.connect(new StdioServerTransport());
