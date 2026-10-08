// Shared by the MCP server and the VS Code extension (the IntelliJ plugin
// mirrors the same file formats in Java). Everything Patchy knows is cached in
// ~/.patchy so it keeps working offline and stays on the user's device.
const fs = require("node:fs");
const os = require("node:os");
const path = require("node:path");

const HOME = process.env.PATCHY_HOME ?? path.join(os.homedir(), ".patchy");
const CREDENTIALS_FILE = path.join(HOME, "credentials.json");
const DATA_FILE = path.join(HOME, "data.json");

const MANIFESTS = new Set(["package.json", "requirements.txt", "pyproject.toml", "Pipfile", "Cargo.toml",
  "go.mod", "Gemfile", "composer.json", "pom.xml", "build.gradle", "build.gradle.kts", "Dockerfile",
  "mix.exs", "gleam.toml", "pubspec.yaml", "build.sbt", "Project.toml", "Directory.Packages.props"]);
const isManifest = (name) => MANIFESTS.has(name) || name.endsWith(".csproj") || name.endsWith(".fsproj");
const SKIP_DIRS = new Set(["node_modules", ".git", "target", "build", "dist", "out", ".venv", "venv",
  "vendor", ".gradle", ".idea", ".next"]);
const MAX_FILES = 25;
const MAX_DEPTH = 4;
const MAX_FILE_BYTES = 512 * 1024;
const ALERT_CATEGORIES = ["Breaking Changes", "Security"];

function readJson(file) {
  try {
    return JSON.parse(fs.readFileSync(file, "utf8"));
  } catch {
    return {};
  }
}

// Owner-only perms: credentials.json holds the API token
function writeJson(file, value) {
  fs.mkdirSync(HOME, { recursive: true, mode: 0o700 });
  fs.writeFileSync(file, JSON.stringify(value, null, 2), { mode: 0o600 });
}

function loadConfig() {
  const saved = readJson(CREDENTIALS_FILE);
  return {
    apiUrl: (process.env.PATCHY_API_URL ?? saved.apiUrl ?? "http://localhost:8080").replace(/\/$/, ""),
    token: process.env.PATCHY_TOKEN ?? saved.token ?? null,
  };
}

function saveCredentials(changes) {
  const merged = { ...readJson(CREDENTIALS_FILE), ...changes };
  for (const key of Object.keys(merged)) {
    if (merged[key] == null) delete merged[key];
  }
  writeJson(CREDENTIALS_FILE, merged);
}

function loadData() {
  return { notes: {}, seen: {}, projects: {}, seenAdvisories: {}, ...readJson(DATA_FILE) };
}

// Read-modify-write so the editor plugins and MCP server can share the file
function updateData(mutate) {
  const data = loadData();
  const result = mutate(data);
  writeJson(DATA_FILE, data);
  return result;
}

function findManifests(root) {
  const files = {};
  const walk = (dir, depth) => {
    let entries;
    try {
      entries = fs.readdirSync(dir, { withFileTypes: true });
    } catch {
      return;
    }
    for (const entry of entries) {
      if (Object.keys(files).length >= MAX_FILES) return;
      const full = path.join(dir, entry.name);
      if (entry.isDirectory() && !SKIP_DIRS.has(entry.name) && depth < MAX_DEPTH) {
        walk(full, depth + 1);
      } else if (entry.isFile() && isManifest(entry.name) && fs.statSync(full).size <= MAX_FILE_BYTES) {
        files[path.relative(root, full).split(path.sep).join("/")] = fs.readFileSync(full, "utf8");
      }
    }
  };
  walk(root, 0);
  return files;
}

async function api(config, method, route, body) {
  const response = await fetch(`${config.apiUrl}${route}`, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(config.token ? { Authorization: `Bearer ${config.token}` } : {}),
    },
    body: body === undefined ? undefined : JSON.stringify(body),
    signal: AbortSignal.timeout(20_000),
  });
  if (!response.ok) {
    throw new Error(`Patchy API ${route} returned ${response.status}`);
  }
  return response.status === 204 ? null : response.json();
}

const URGENT = ["critical", "high"];
const isBreaking = (note) => URGENT.includes(note.urgency)
  || (note.categories ?? []).some((category) => ALERT_CATEGORIES.includes(category));

// Scans a project, caches the results locally, and reports which notes are new.
// Offline: answers from the local dataset instead (offline: true, no new notes).
async function scanProject(root, config = loadConfig()) {
  const projectPath = path.resolve(root);
  const files = findManifests(projectPath);
  if (Object.keys(files).length === 0) {
    return { projectPath, notes: [], techNames: [], discovering: [], advisories: [], newNotes: [], newAdvisories: [], firstScan: false, offline: false };
  }

  let insights;
  try {
    insights = await api(config, "POST", "/api/insights/project", { files });
  } catch (error) {
    const data = loadData();
    const techNames = data.projects[projectPath]?.techNames ?? [];
    return {
      projectPath, techNames, discovering: [], newNotes: [], newAdvisories: [], firstScan: false, offline: true, error: error.message,
      advisories: data.projects[projectPath]?.advisories ?? [],
      notes: techNames.map((name) => data.notes[name]).filter(Boolean),
    };
  }

  const notes = insights.matches.map((match) => match.patchNote);
  const advisories = insights.advisories ?? [];
  const { newNotes, newAdvisories, firstScan } = updateData((data) => {
    const firstScan = !data.projects[projectPath];
    const newNotes = notes.filter((note) => data.seen[note.techName] !== undefined && data.seen[note.techName] !== note.id);
    for (const note of notes) {
      data.notes[note.techName] = note;
      // Techs seen for the first time are recorded silently; only later releases alert
      data.seen[note.techName] ??= note.id;
    }
    // Advisories alert even on a first scan: they describe a current problem, not a new release
    const newAdvisories = advisories.filter((a) => !data.seenAdvisories[`${a.packageName}@${a.id}`]);
    for (const a of advisories) data.seenAdvisories[`${a.packageName}@${a.id}`] = true;
    data.projects[projectPath] = { techNames: insights.techNames, advisories, scannedAt: new Date().toISOString() };
    return { newNotes, newAdvisories, firstScan };
  });

  if (config.token && insights.techNames.length > 0) {
    // Mirror the project's techs into the user's Bay on the website
    await api(config, "POST", "/api/me/favorites", { techNames: insights.techNames }).catch(() => {});
  }

  return {
    projectPath, notes, techNames: insights.techNames, discovering: insights.discovering, advisories,
    newNotes, newAdvisories, firstScan, offline: false,
  };
}

function markSeen(notes) {
  updateData((data) => {
    for (const note of notes) data.seen[note.techName] = note.id;
  });
}

async function findNote(techName, config = loadConfig()) {
  const wanted = techName.trim().toLowerCase();
  try {
    const notes = await api(config, "GET", "/api/patch-notes");
    const note = notes.find((candidate) => candidate.techName.toLowerCase() === wanted);
    if (note) updateData((data) => { data.notes[note.techName] = note; });
    return note ?? null;
  } catch {
    return Object.values(loadData().notes).find((note) => note.techName.toLowerCase() === wanted) ?? null;
  }
}

function formatNotes(notes) {
  if (notes.length === 0) return "No tracked patch notes for this project yet.";
  return notes.map((note) => [
    `## ${note.techName}${note.releaseVersion ? ` ${note.releaseVersion}` : ""}${URGENT.includes(note.urgency) ? ` (${note.urgency} urgency)` : ""}`,
    isBreaking(note) ? `**⚠ ${note.categories.filter((c) => ALERT_CATEGORIES.includes(c)).join(" / ")}**` : null,
    note.content,
    note.sourceUrl ? `[Release notes](${note.sourceUrl})` : null,
  ].filter(Boolean).join("\n\n")).join("\n\n---\n\n");
}

function formatAdvisories(advisories) {
  if (advisories.length === 0) return "";
  return "## Security advisories\n\n_Checked against the lowest version your manifest allows; your lockfile may already include the fix._\n\n"
    + advisories.map((a) => `- **${a.packageName} ${a.version}**: [${a.id}](${a.url}) ${a.summary}${a.fixedIn ? ` (fixed in ${a.fixedIn})` : ""}`).join("\n");
}

module.exports = {
  HOME, CREDENTIALS_FILE, DATA_FILE, ALERT_CATEGORIES,
  loadConfig, saveCredentials, loadData, findManifests, api, scanProject, markSeen, findNote, formatNotes, formatAdvisories, isBreaking,
};
