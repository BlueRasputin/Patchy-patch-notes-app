// Mock end-to-end test: new languages and packages flowing into Patchy's shared
// database, then simulated new releases flowing back out as editor alerts.
//   node mock-test/run.mjs
// Uses a separate MySQL database (patchy_mock) and port 8091, so the real dev
// data is untouched. Gemini and injected releases are mocked (see mock-upstream.mjs);
// registries, GitHub, endoflife.date and OSV are real.
import { spawn } from "node:child_process";
import { createRequire } from "node:module";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { startMockUpstream, injectGithubRelease, injectLanguageRelease, log as mockLog } from "./mock-upstream.mjs";

const HERE = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.resolve(HERE, "..");
const API = "http://localhost:8091";
const MOCK = "http://localhost:8099";
const PROJECTS = ["elixir-api", "gleam-web", "scala-service", "julia-analysis", "flutter-app"];

// Fresh local dataset, as on a new user's machine
process.env.PATCHY_HOME = path.join(HERE, ".home");
fs.rmSync(process.env.PATCHY_HOME, { recursive: true, force: true });
const core = createRequire(import.meta.url)(path.join(ROOT, "integrations/mcp/core.js"));
const config = { apiUrl: API, token: null };

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const heading = (text) => console.log(`\n${"=".repeat(78)}\n${text}\n${"=".repeat(78)}`);
const getJson = async (route) => (await fetch(`${API}${route}`)).json();

async function waitFor(label, check, timeoutMs) {
  const start = Date.now();
  while (Date.now() - start < timeoutMs) {
    const result = await check().catch(() => null);
    if (result) return result;
    await sleep(3000);
  }
  throw new Error(`Timed out waiting for ${label}`);
}

function startApi() {
  const settings = {
    server: { port: 8091 },
    spring: { datasource: { url: "jdbc:mysql://localhost:3306/patchy_mock?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC" } },
    GEMINI_API_KEY: "mock-key",
    // Any token makes the server use the GitHub API (via the mock proxy) instead of the public feed
    GITHUB_TOKEN: "mock-token",
    patchy: {
      gemini: { "base-url": MOCK, spacing: "PT0S" },
      github: { "api-url": MOCK },
      endoflife: { url: MOCK },
      // Compressed schedule: every tech is checked every 30s instead of once a day
      refresh: { cron: "*/30 * * * * *", slices: 1 },
      summary: { interval: "PT5S" },
    },
  };
  const api = spawn("./mvnw", ["-q", "spring-boot:run"], {
    cwd: path.join(ROOT, "server"),
    env: { ...process.env, SPRING_APPLICATION_JSON: JSON.stringify(settings) },
    detached: true,
    stdio: ["ignore", fs.openSync(path.join(HERE, "api.log"), "w"), "pipe"],
  });
  api.stderr.on("data", () => {});
  return api;
}

function describeSource(tech) {
  if (tech.sourceRepo) return `GitHub releases (${tech.sourceRepo})`;
  if (tech.crawlSelector) return `AI-found page, selector "${tech.crawlSelector}"`;
  if (tech.crawlUrl) return `changelog file`;
  if (tech.ecosystem === "language") return "endoflife.date release link";
  return "?";
}

async function discoveredTable() {
  const [techs, notes] = await Promise.all([getJson("/tech"), getJson("/api/patch-notes")]);
  const notesByTech = Object.fromEntries(notes.map((note) => [note.techName, note]));
  return techs.filter((tech) => tech.ecosystem).map((tech) => ({ tech, note: notesByTech[tech.name] }));
}

function printTable(rows) {
  for (const { tech, note } of rows) {
    console.log(`\n- ${tech.name}  [${tech.ecosystem}]  via ${describeSource(tech)}`);
    if (!note) {
      console.log("    no note yet");
      continue;
    }
    console.log(`    version ${note.releaseVersion || "?"} | urgency ${note.urgency ?? "pending summary"} | ${note.categories.join(", ") || "no categories"}`);
    console.log(`    ${(note.headline ?? note.content.split("\n")[0]).slice(0, 110)}`);
  }
}

// Same message the VS Code extension shows for new releases
function editorAlert(newNotes) {
  const breaking = newNotes.filter(core.isBreaking);
  const names = newNotes.map((n) => `${n.techName} ${n.releaseVersion ?? ""}`.trim()).join(", ");
  return breaking.length > 0
    ? `⚠ WARNING toast: "Patchy: breaking changes in ${breaking.map((n) => `${n.techName} ${n.releaseVersion}`).join(", ")}"`
    : `ℹ info toast: "Patchy: new releases for ${names}"`;
}

const mockServer = await startMockUpstream(8099);
const api = startApi();
let exitCode = 0;

try {
  heading("Starting the API on :8091 with database patchy_mock (separate from your dev data)");
  await waitFor("API startup", async () => (await fetch(`${API}/tech`)).ok, 180_000);
  console.log("API is up. Catalog techs seeded:", (await getJson("/tech")).length);

  heading("PHASE 1: an editor scans five projects in languages Patchy has never seen");
  for (const project of PROJECTS) {
    const scan = await core.scanProject(path.join(HERE, "projects", project), config);
    console.log(`\n${project}`);
    console.log(`  matched existing techs: ${scan.techNames.join(", ") || "none"}`);
    console.log(`  queued for discovery:   ${scan.discovering.join(", ") || "none"}`);
    console.log(`  security advisories:    ${scan.advisories.map((a) => `${a.packageName} ${a.version} ${a.id}${a.fixedIn ? ` (fixed in ${a.fixedIn})` : ""}`).join("; ") || "none"}`);
  }

  heading("Waiting for discovery (registries -> GitHub / changelogs / endoflife.date) and summaries");
  await waitFor("discovered techs to be summarized", async () => {
    const rows = await discoveredTable();
    const pending = rows.filter((row) => row.note && !row.note.urgency);
    process.stdout.write(`\r  ${rows.length} new techs in the shared table, ${pending.length} waiting for a summary   `);
    return rows.length >= 8 && pending.length === 0;
  }, 240_000).catch((error) => console.log(`\n  ${error.message}; showing what exists so far`));
  console.log("\n\nShared tech table, newly added rows:");
  printTable(await discoveredTable());

  heading("PHASE 2: second scan — techs are now matched, so nothing is queued again");
  const baseline = await core.scanProject(path.join(HERE, "projects", "gleam-web"), config);
  const juliaBaseline = await core.scanProject(path.join(HERE, "projects", "julia-analysis"), config);
  console.log(`gleam-web matched: ${baseline.techNames.join(", ")} | queued: ${baseline.discovering.join(", ") || "none"}`);
  console.log(`julia-analysis matched: ${juliaBaseline.techNames.join(", ")} | queued: ${juliaBaseline.discovering.join(", ") || "none"}`);
  console.log(`new-release alerts on this scan: ${baseline.newNotes.length + juliaBaseline.newNotes.length} (expected 0)`);

  const gleam = (await getJson("/tech")).find((tech) => tech.name === "Gleam");
  heading("PHASE 3: upstream ships new versions (injected into the mock)");
  if (gleam?.sourceRepo) {
    injectGithubRelease(gleam.sourceRepo, {
      tag_name: "v9.9.0",
      name: "v9.9.0",
      html_url: `https://github.com/${gleam.sourceRepo}/releases/tag/v9.9.0`,
      body: [
        "## Breaking changes",
        "- Removed the deprecated `list.at` function; use `list.drop` and `list.first` instead.",
        "- The `gleam export` command no longer writes to the project root by default.",
        "## Security",
        "- Fixed CVE-2026-31337: a path traversal in `gleam deps download` when unpacking tarballs.",
        "## Features",
        "- Added a language server code action to inline variables.",
        "## Bug fixes",
        "- Fixed a crash when formatting files containing only comments.",
      ].join("\n"),
    });
    console.log(`Gleam: new GitHub release v9.9.0 injected for ${gleam.sourceRepo} (breaking + security)`);
  }
  injectLanguageRelease("julia", { name: "1.99.0", date: "2026-10-06", link: `${MOCK}/mock/julia-1.99.html` }, `
    <html><body><nav>Docs Blog Downloads</nav><main>
      <h1>Julia 1.99.0 release notes</h1>
      <ul>
        <li>Julia now requires a minimum of LLVM 20 to build from source.</li>
        <li>The <code>@async</code> macro is deprecated in favor of <code>Threads.@spawn</code>.</li>
        <li>Broadcasting over dictionaries is about 3x faster.</li>
        <li>Fixed incorrect results from <code>sum</code> on empty ranges of BigInt.</li>
      </ul>
    </main></body></html>`);
  console.log("Julia: endoflife.date now reports 1.99.0 with a release-notes page (requirements + deprecation)");

  heading("Waiting for the release poller (every 30s here, daily 20-23 UTC in production) and summaries");
  await waitFor("injected releases to be stored and summarized", async () => {
    const notes = await getJson("/api/patch-notes");
    const gleamNote = notes.find((note) => note.techName === "Gleam");
    const juliaNote = notes.find((note) => note.techName === "Julia");
    return gleamNote?.releaseVersion === "v9.9.0" && gleamNote.urgency && juliaNote?.releaseVersion === "1.99.0" && juliaNote.urgency;
  }, 150_000);
  printTable((await discoveredTable()).filter(({ tech }) => ["Gleam", "Julia"].includes(tech.name)));

  heading("PHASE 4: the user's editor scans again — what they see");
  for (const project of ["gleam-web", "julia-analysis"]) {
    const scan = await core.scanProject(path.join(HERE, "projects", project), config);
    console.log(`\n${project}: ${scan.newNotes.length} new release(s)`);
    if (scan.newNotes.length > 0) {
      console.log(`  ${editorAlert(scan.newNotes)}`);
      console.log("  + a 'Patchy: New releases' tab opens with:\n");
      console.log(core.formatNotes(scan.newNotes).split("\n").map((line) => `    ${line}`).join("\n"));
      core.markSeen(scan.newNotes);
    }
  }
  const quiet = await core.scanProject(path.join(HERE, "projects", "gleam-web"), config);
  console.log(`\nScan after the user has seen them: ${quiet.newNotes.length} new release(s) (expected 0)`);

  heading("Mock upstream activity");
  const counts = mockLog.reduce((acc, line) => ({ ...acc, [line.split(":")[0]]: (acc[line.split(":")[0]] ?? 0) + 1 }), {});
  console.log(counts);
} catch (error) {
  exitCode = 1;
  console.error(`\nMOCK TEST FAILED: ${error.message}\nAPI log: mock-test/api.log`);
} finally {
  process.kill(-api.pid, "SIGTERM");
  mockServer.close();
  process.exit(exitCode);
}
