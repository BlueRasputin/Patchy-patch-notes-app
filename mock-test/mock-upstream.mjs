// Local stand-in for the outside world during the mock test:
//  - GitHub API and endoflife.date: proxied to the real services, except releases
//    the test injects with POST /__mock/release (to simulate a new version shipping)
//  - Gemini: a deterministic fake that categorizes lines by keyword, so the whole
//    summary pipeline runs without an API key or quota. Its output is marked "[mock]".
import http from "node:http";
import { execFileSync } from "node:child_process";

const githubOverrides = new Map(); // "owner/repo" -> release JSON
const endOfLifeOverrides = new Map(); // product -> { name, date, link }
const pages = new Map(); // path -> html served at /mock/<path>
export const log = [];

let githubToken = process.env.GITHUB_TOKEN ?? null;
try {
  githubToken ??= execFileSync("gh", ["auth", "token"], { encoding: "utf8" }).trim() || null;
} catch {
  // Anonymous GitHub API (60 requests/hour) is enough for a small run
}

const CATEGORY_RULES = [
  ["Security", /CVE-\d{4}-\d+|GHSA-|security|vulnerab/i],
  ["Breaking Changes", /breaking|removed|no longer|dropped support/i],
  ["Deprecations", /deprecat/i],
  ["New Requirements", /requires|minimum|now needs/i],
  ["Bug Fixes", /\bfix/i],
  ["Performance", /faster|perf|speed/i],
];

function mockSummary(prompt) {
  const notes = prompt.split("Release notes:\n")[1] ?? "";
  const expected = prompt.match(/Expected version: (.+)/)?.[1];
  const version = expected ?? notes.match(/v?\d+\.\d+(\.\d+)?/)?.[0] ?? "";
  const lines = notes.split("\n").map((line) => line.replace(/^[\s*#-]+/, "").trim()).filter((line) => line.length > 12);
  const items = lines.slice(0, 12).map((line) => {
    const category = CATEGORY_RULES.find(([, rule]) => rule.test(line))?.[0] ?? "New Features";
    return {
      category,
      title: line.split(/[.:(]/)[0].slice(0, 60),
      detail: line.slice(0, 200),
      migration: category === "Breaking Changes" ? "Follow the upgrade guide in the release notes." : "",
      identifiers: line.match(/(CVE-\d{4}-\d+|GHSA-[\w-]+)/g) ?? [],
    };
  });
  const has = (category) => items.some((item) => item.category === category);
  return {
    isReleaseNotes: lines.length > 0,
    version,
    releaseDate: "",
    headline: `[mock] ${lines[0]?.slice(0, 100) ?? "No notes"}`,
    urgency: /critical/i.test(notes) ? "critical" : has("Security") || has("Breaking Changes") ? "high" : "normal",
    items,
  };
}

function mockGemini(body) {
  const properties = body.generationConfig.responseSchema.properties;
  const prompt = body.contents[0].parts[0].text;
  let result;
  if (properties.repo) {
    // Stand-in for the model's knowledge of where a language is developed
    const language = prompt.match(/Language: (\S+)/)?.[1];
    result = { repo: { dart: "dart-lang/sdk", zig: "ziglang/zig" }[language] ?? "" };
    log.push(`gemini(mock): named repo for ${language}: ${result.repo || "unsure"}`);
  } else if (properties.items) {
    result = mockSummary(prompt);
    log.push(`gemini(mock): summarized ${prompt.match(/Tech: (.+)/)?.[1]}`);
  } else if (properties.contentSelector) {
    result = { isReleaseNotesPage: true, contentSelector: "main" };
    log.push("gemini(mock): proposed selector 'main'");
  } else {
    result = { url: prompt.match(/- (https?:\/\/\S+)/)?.[1] ?? "" };
    log.push(`gemini(mock): picked page ${result.url}`);
  }
  return { candidates: [{ content: { parts: [{ text: JSON.stringify(result) }] } }] };
}

async function proxy(target, response, headers = {}) {
  const upstream = await fetch(target, { headers: { "User-Agent": "patchy-mock-test", ...headers } });
  response.writeHead(upstream.status, { "Content-Type": "application/json" });
  response.end(await upstream.text());
}

export function injectGithubRelease(repo, release) {
  githubOverrides.set(repo, release);
}

export function injectLanguageRelease(product, latest, pageHtml) {
  endOfLifeOverrides.set(product, latest);
  if (pageHtml) pages.set(new URL(latest.link).pathname, pageHtml);
}

export function startMockUpstream(port) {
  const server = http.createServer(async (request, response) => {
    const url = new URL(request.url, `http://localhost:${port}`);
    try {
      if (request.method === "POST" && url.pathname.includes(":generateContent")) {
        let raw = "";
        for await (const chunk of request) raw += chunk;
        response.writeHead(200, { "Content-Type": "application/json" });
        response.end(JSON.stringify(mockGemini(JSON.parse(raw))));
        return;
      }
      const release = url.pathname.match(/^\/repos\/([^/]+\/[^/]+)\/releases\/latest$/);
      if (release && githubOverrides.has(release[1])) {
        log.push(`github(mock): served injected release for ${release[1]}`);
        response.writeHead(200, { "Content-Type": "application/json" });
        response.end(JSON.stringify(githubOverrides.get(release[1])));
        return;
      }
      if (url.pathname.startsWith("/repos/")) {
        await proxy(`https://api.github.com${url.pathname}`, response, githubToken ? { Authorization: `Bearer ${githubToken}` } : {});
        return;
      }
      const product = url.pathname.match(/^\/api\/v1\/products\/([^/]+)$/)?.[1];
      if (product && endOfLifeOverrides.has(product)) {
        log.push(`endoflife(mock): served injected release for ${product}`);
        response.writeHead(200, { "Content-Type": "application/json" });
        response.end(JSON.stringify({ result: { label: product[0].toUpperCase() + product.slice(1), releases: [{ name: "next", latest: endOfLifeOverrides.get(product) }] } }));
        return;
      }
      if (product) {
        await proxy(`https://endoflife.date${url.pathname}`, response);
        return;
      }
      if (pages.has(url.pathname)) {
        response.writeHead(200, { "Content-Type": "text/html" });
        response.end(pages.get(url.pathname));
        return;
      }
      response.writeHead(404).end();
    } catch (error) {
      response.writeHead(502).end(String(error));
    }
  });
  return new Promise((resolve) => server.listen(port, () => resolve(server)));
}
