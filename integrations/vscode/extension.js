const vscode = require("vscode");
const fs = require("node:fs");
const os = require("node:os");
const path = require("node:path");
const crypto = require("node:crypto");
const { execFileSync } = require("node:child_process");
const core = require("patchy-mcp/core");

const SERVER_PATH = require.resolve("patchy-mcp/server.js");

let context;
let statusItem;
let panel;
let timer;
let latestNotes = [];
let latestAdvisories = [];
let pendingSignInState = null;

const setting = (key) => vscode.workspace.getConfiguration("patchy").get(key);
const config = () => ({ ...core.loadConfig(), apiUrl: setting("apiUrl").replace(/\/$/, "") });
const names = (notes) => notes.map((n) => `${n.techName}${n.releaseVersion ? ` ${n.releaseVersion}` : ""}`).join(", ");

function uniqueByTech(notes) {
  return [...new Map(notes.map((note) => [note.techName, note])).values()];
}

async function scanWorkspace() {
  const folders = vscode.workspace.workspaceFolders ?? [];
  return Promise.all(folders.map((folder) => core.scanProject(folder.uri.fsPath, config())));
}

async function check({ manual = false } = {}) {
  const scans = await scanWorkspace();
  latestNotes = uniqueByTech(scans.flatMap((scan) => scan.notes));
  const newNotes = uniqueByTech(scans.flatMap((scan) => scan.newNotes));
  latestAdvisories = scans.flatMap((scan) => scan.advisories);
  const newAdvisories = scans.flatMap((scan) => scan.newAdvisories);
  const offline = scans.find((scan) => scan.offline);
  const warningCount = latestNotes.filter(core.isBreaking).length + latestAdvisories.length;

  statusItem.text = `$(versions) ${latestNotes.length}${warningCount ? ` $(warning) ${warningCount}` : ""}`;
  statusItem.tooltip = offline
    ? `Patchy: offline, showing cached notes (${offline.error})`
    : `Patchy: tracking ${latestNotes.length} techs${warningCount ? `, ${warningCount} breaking changes or vulnerabilities` : ""}`;
  statusItem.show();

  if (newNotes.length > 0 || newAdvisories.length > 0) {
    // New releases and advisories open their own tab, then a toast summarizes them
    showUpdates(newNotes, newAdvisories.length > 0 ? "Security alert" : "New releases", newAdvisories);
    core.markSeen(newNotes);
    const breaking = newNotes.filter(core.isBreaking);
    const urgent = breaking.length > 0 || newAdvisories.length > 0;
    const message = newAdvisories.length > 0
      ? `Patchy: ${newAdvisories.length} known vulnerabilit${newAdvisories.length === 1 ? "y" : "ies"} in ${[...new Set(newAdvisories.map((a) => a.packageName))].join(", ")}`
      : breaking.length > 0
        ? `Patchy: breaking changes in ${names(breaking)}`
        : `Patchy: new releases for ${names(newNotes)}`;
    const pick = await (urgent ? vscode.window.showWarningMessage : vscode.window.showInformationMessage)(message, "Show all patch notes");
    if (pick) showUpdates(latestNotes, "Patch notes", latestAdvisories);
  } else if (scans.some((scan) => scan.firstScan && scan.notes.length > 0)) {
    const pick = await vscode.window.showInformationMessage(
      `Patchy is tracking ${latestNotes.length} techs in this workspace.`, "Show patch notes");
    if (pick) showUpdates(latestNotes, "Patch notes", latestAdvisories);
  } else if (manual) {
    vscode.window.showInformationMessage(offline
      ? `Patchy is offline (${offline.error}); showing cached notes.`
      : "Patchy: no new releases.");
  }

  const discovering = scans.flatMap((scan) => scan.discovering);
  if (manual && discovering.length > 0) {
    vscode.window.showInformationMessage(`Patchy is looking up ${discovering.length} untracked packages; they'll appear on the next check.`);
  }
}

// LLM summaries of third-party pages: neutralize raw HTML, and the CSP below
// blocks scripts and remote content regardless.
const escapeHtml = (value) => String(value ?? "").replace(/</g, "&lt;");

async function showUpdates(notes, title, advisories = []) {
  const markdown = [
    core.formatAdvisories(advisories.map((a) => ({ ...a, summary: escapeHtml(a.summary) }))),
    notes.length > 0 || advisories.length === 0
      ? core.formatNotes(notes.map((note) => ({ ...note, content: escapeHtml(note.content) })))
      : "",
  ].filter(Boolean).join("\n\n---\n\n");
  const html = await vscode.commands.executeCommand("markdown.api.render", markdown);
  if (!panel) {
    panel = vscode.window.createWebviewPanel("patchy.updates", title, { viewColumn: vscode.ViewColumn.Active, preserveFocus: true });
    panel.onDidDispose(() => { panel = undefined; });
  }
  panel.title = `Patchy: ${title}`;
  panel.webview.html = `<!DOCTYPE html><html><head><meta charset="utf-8">
<meta http-equiv="Content-Security-Policy" content="default-src 'none'; style-src 'unsafe-inline';">
<style>body{font-family:var(--vscode-font-family);color:var(--vscode-foreground);max-width:860px;margin:0 auto;padding:16px 24px;line-height:1.5}
a{color:var(--vscode-textLink-foreground)}hr{border:0;border-top:1px solid var(--vscode-panel-border);margin:24px 0}
strong{color:var(--vscode-editorWarning-foreground)}code{font-family:var(--vscode-editor-font-family)}</style>
</head><body>${html}</body></html>`;
  panel.reveal(undefined, true);
}

async function signIn() {
  pendingSignInState = crypto.randomBytes(16).toString("hex");
  const callback = await vscode.env.asExternalUri(vscode.Uri.parse(`${vscode.env.uriScheme}://barrcon.patchy/auth`));
  const url = `${setting("websiteUrl").replace(/\/$/, "")}/connect?client=${encodeURIComponent(vscode.env.appName)}`
    + `&redirect_uri=${encodeURIComponent(callback.toString())}&state=${pendingSignInState}`;
  await vscode.env.openExternal(vscode.Uri.parse(url));
}

async function handleUri(uri) {
  const params = new URLSearchParams(uri.query);
  const token = params.get("token");
  if (uri.path !== "/auth" || !pendingSignInState || params.get("state") !== pendingSignInState || !/^pat_[\w-]+$/.test(token ?? "")) {
    vscode.window.showWarningMessage("Patchy ignored a sign-in link it didn't request.");
    return;
  }
  pendingSignInState = null;
  core.saveCredentials({ apiUrl: config().apiUrl, token });
  vscode.window.showInformationMessage("Signed in to Patchy. Your project's techs will sync to your Bay.");
  await check();
  await connectAssistants();
}

function signOut() {
  core.saveCredentials({ token: null });
  vscode.window.showInformationMessage("Signed out of Patchy. Revoke the token on your Patchy profile page to fully disconnect.");
}

// Absolute paths via the login shell: VS Code and GUI apps like Claude Desktop
// often don't inherit the user's PATH. Callers pass fixed command names only.
function which(command) {
  try {
    return execFileSync(process.env.SHELL || "/bin/sh", ["-lc", `command -v ${command}`], { encoding: "utf8" }).trim() || null;
  } catch {
    return null;
  }
}

const nodePath = () => which("node") ?? "node";

function mergeJsonConfig(file, server) {
  let current = {};
  try {
    current = JSON.parse(fs.readFileSync(file, "utf8"));
  } catch (error) {
    if (fs.existsSync(file)) throw new Error(`${file} isn't valid JSON; not touching it`, { cause: error });
  }
  current.mcpServers = { ...current.mcpServers, patchy: server };
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, JSON.stringify(current, null, 2));
}

const claudeDesktopConfig = () => ({
  darwin: path.join(os.homedir(), "Library", "Application Support", "Claude", "claude_desktop_config.json"),
  win32: path.join(process.env.APPDATA ?? "", "Claude", "claude_desktop_config.json"),
}[process.platform] ?? path.join(os.homedir(), ".config", "Claude", "claude_desktop_config.json"));

// Each writer is idempotent: rerunning it points the assistant at the current server path.
// No secrets go into these files; the server reads ~/.patchy/credentials.json itself.
const ASSISTANTS = [
  {
    id: "claude-code", label: "Claude Code", detect: () => which("claude") !== null,
    connect: (node) => {
      const claude = which("claude");
      try {
        execFileSync(claude, ["mcp", "remove", "patchy", "--scope", "user"], { stdio: "ignore" });
      } catch {
        // not registered yet
      }
      execFileSync(claude, ["mcp", "add", "--scope", "user", "patchy", "--", node, SERVER_PATH], { stdio: "ignore" });
    },
  },
  {
    id: "claude-desktop", label: "Claude Desktop", detect: () => fs.existsSync(path.dirname(claudeDesktopConfig())),
    connect: (node) => mergeJsonConfig(claudeDesktopConfig(), { command: node, args: [SERVER_PATH] }),
  },
  {
    id: "codex", label: "OpenAI Codex (CLI / IDE)", detect: () => which("codex") !== null || fs.existsSync(path.join(os.homedir(), ".codex")),
    connect: (node) => {
      const file = path.join(os.homedir(), ".codex", "config.toml");
      const existing = fs.existsSync(file) ? fs.readFileSync(file, "utf8") : "";
      const block = `[mcp_servers.patchy]\ncommand = ${JSON.stringify(node)}\nargs = [${JSON.stringify(SERVER_PATH)}]\n`;
      const withoutOld = existing.replace(/\[mcp_servers\.patchy\]\n(?:(?!\[)[^\n]*\n?)*/, "");
      fs.mkdirSync(path.dirname(file), { recursive: true });
      fs.writeFileSync(file, `${withoutOld.trimEnd()}${withoutOld.trim() ? "\n\n" : ""}${block}`);
    },
  },
  {
    id: "gemini", label: "Gemini CLI", detect: () => which("gemini") !== null || fs.existsSync(path.join(os.homedir(), ".gemini")),
    connect: (node) => mergeJsonConfig(path.join(os.homedir(), ".gemini", "settings.json"), { command: node, args: [SERVER_PATH] }),
  },
];

async function connectAssistants() {
  const connected = context.globalState.get("connectedAssistants", []);
  const picks = await vscode.window.showQuickPick(
    ASSISTANTS.map((assistant) => ({
      label: assistant.label,
      id: assistant.id,
      description: assistant.detect() ? "detected" : "not detected",
      picked: connected.includes(assistant.id) || assistant.detect(),
    })),
    { canPickMany: true, title: "Give these AI assistants Patchy's patch-note tools? (Copilot gets them automatically)" },
  );
  if (!picks) return;
  connectTo(picks.map((pick) => pick.id), true);
}

function connectTo(ids, report) {
  const node = nodePath();
  const done = [];
  for (const assistant of ASSISTANTS.filter((a) => ids.includes(a.id))) {
    try {
      assistant.connect(node);
      done.push(assistant.id);
    } catch (error) {
      vscode.window.showErrorMessage(`Patchy couldn't connect ${assistant.label}: ${error.message}`);
    }
  }
  context.globalState.update("connectedAssistants", done);
  context.globalState.update("connectedServerPath", SERVER_PATH);
  if (report && done.length > 0) {
    vscode.window.showInformationMessage(`Patchy tools added to ${done.length} assistant(s). Restart them to pick it up.`);
  }
}

function registerCopilot() {
  // Copilot agent mode picks Patchy's MCP tools up from here (VS Code 1.101+)
  if (vscode.lm?.registerMcpServerDefinitionProvider) {
    context.subscriptions.push(vscode.lm.registerMcpServerDefinitionProvider("patchy.mcp", {
      provideMcpServerDefinitions: () => [
        new vscode.McpStdioServerDefinition("Patchy", nodePath(), [SERVER_PATH], { PATCHY_API_URL: config().apiUrl }),
      ],
    }));
  }

  // @patchy in Copilot Chat: answers from the project's release notes using the user's chosen model
  const participant = vscode.chat.createChatParticipant("patchy.chat", async (request, _chatContext, stream, token) => {
    if (latestNotes.length === 0) {
      stream.progress("Scanning project manifests…");
      latestNotes = uniqueByTech((await scanWorkspace()).flatMap((scan) => scan.notes));
    }
    const notes = request.command === "breaking" ? latestNotes.filter(core.isBreaking) : latestNotes;
    const context = [core.formatAdvisories(latestAdvisories), core.formatNotes(notes)].filter(Boolean).join("\n\n");
    // Notes are stored as published; the user's own Copilot model does the summarizing
    const question = request.prompt.trim()
      || (request.command === "breaking"
        ? "Which of these need action before I upgrade? List breaking changes and vulnerabilities with what to do."
        : "Summarize what changed in my project's dependencies, most important first. Flag anything breaking or security-related.");
    const prompt = "Answer the developer's question using these release notes and security advisories for their project's dependencies. "
      + "Cite tech names and versions; say so if the notes don't cover it.\n\n"
      + `<release_notes>\n${context}\n</release_notes>\n\nQuestion: ${question}`;
    const response = await request.model.sendRequest([vscode.LanguageModelChatMessage.User(prompt)], {}, token);
    for await (const chunk of response.text) {
      stream.markdown(chunk);
    }
  });
  participant.iconPath = new vscode.ThemeIcon("versions");
  context.subscriptions.push(participant);
}

function schedule() {
  clearInterval(timer);
  timer = setInterval(() => check().catch(() => {}), Math.max(5, setting("pollMinutes")) * 60_000);
}

function activate(extensionContext) {
  context = extensionContext;
  statusItem = vscode.window.createStatusBarItem(vscode.StatusBarAlignment.Right, 50);
  statusItem.command = "patchy.showUpdates";
  statusItem.text = "$(versions) Patchy";

  context.subscriptions.push(
    statusItem,
    vscode.commands.registerCommand("patchy.showUpdates", () => showUpdates(latestNotes, "Patch notes", latestAdvisories)),
    vscode.commands.registerCommand("patchy.checkNow", () => check({ manual: true })),
    vscode.commands.registerCommand("patchy.signIn", signIn),
    vscode.commands.registerCommand("patchy.signOut", signOut),
    vscode.commands.registerCommand("patchy.connectAssistants", connectAssistants),
    vscode.window.registerUriHandler({ handleUri }),
    vscode.workspace.onDidChangeConfiguration((event) => event.affectsConfiguration("patchy.pollMinutes") && schedule()),
    vscode.workspace.onDidChangeWorkspaceFolders(() => check().catch(() => {})),
    { dispose: () => clearInterval(timer) },
  );

  // Manifest edits (new dependency added) trigger a re-check
  const watcher = vscode.workspace.createFileSystemWatcher(
    "**/{package.json,requirements.txt,pyproject.toml,Pipfile,Cargo.toml,go.mod,Gemfile,composer.json,pom.xml,build.gradle,build.gradle.kts,Dockerfile,mix.exs,gleam.toml,pubspec.yaml,build.sbt,Project.toml,Directory.Packages.props,*.csproj,*.fsproj}");
  let debounce;
  const recheck = (uri) => {
    if (uri.fsPath.includes(`${path.sep}node_modules${path.sep}`)) return;
    clearTimeout(debounce);
    debounce = setTimeout(() => check().catch(() => {}), 5_000);
  };
  context.subscriptions.push(watcher, watcher.onDidChange(recheck), watcher.onDidCreate(recheck));

  registerCopilot();

  // Extension updates move SERVER_PATH; re-point assistants connected earlier
  const connected = context.globalState.get("connectedAssistants", []);
  if (connected.length > 0 && context.globalState.get("connectedServerPath") !== SERVER_PATH) {
    connectTo(connected, false);
  }

  check().catch((error) => { statusItem.tooltip = `Patchy: ${error.message}`; statusItem.show(); });
  schedule();
}

function deactivate() {}

module.exports = { activate, deactivate };
