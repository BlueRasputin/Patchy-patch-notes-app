const API_BASE = "http://localhost:8080";

const statusEl = document.getElementById("status");
const summaryEl = document.getElementById("summary");
const techNameEl = document.getElementById("tech-name");
const summarizeBtn = document.getElementById("summarize");

const setStatus = (text) => {
  statusEl.hidden = !text;
  statusEl.textContent = text ?? "";
};

// The catalog changes rarely; cache it for an hour
async function getCatalog() {
  const cached = await chrome.storage.local.get(["catalog", "catalogAt"]);
  if (cached.catalog && Date.now() - cached.catalogAt < 60 * 60 * 1000) {
    return cached.catalog;
  }
  const response = await fetch(`${API_BASE}/tech/catalog`);
  if (!response.ok) throw new Error("Patchy backend unreachable");
  const catalog = await response.json();
  await chrome.storage.local.set({ catalog, catalogAt: Date.now() });
  return catalog;
}

function matchTech(catalog, tabUrl) {
  const tabHost = new URL(tabUrl).hostname.replace(/^www\./, "");
  return catalog.find((entry) => {
    try {
      const entryHost = new URL(entry.patchNotesUrl).hostname.replace(/^www\./, "");
      return entryHost === tabHost;
    } catch {
      return false;
    }
  });
}

async function loadLatestSummary(techName) {
  const response = await fetch(`${API_BASE}/api/patch-notes`);
  if (!response.ok) throw new Error("Couldn't fetch patch notes");
  const notes = await response.json();
  return notes.find((note) => note.techName === techName) ?? null;
}

// Runs inside the page: grab the readable text of the main content area
function extractPageText() {
  const container =
    document.querySelector("main, article, .content") ?? document.body;
  const clone = container.cloneNode(true);
  clone.querySelectorAll("script, style, nav, header, footer").forEach((el) => el.remove());
  return clone.textContent.replace(/\s+/g, " ").trim().slice(0, 100000);
}

async function submitPage(tech, tab) {
  summarizeBtn.disabled = true;
  setStatus("Extracting page content…");

  const [{ result: content }] = await chrome.scripting.executeScript({
    target: { tabId: tab.id },
    func: extractPageText,
  });

  if (!content || content.length < 100) {
    setStatus("Not enough content on this page to summarize.");
    summarizeBtn.disabled = false;
    return;
  }

  setStatus("Sending to Patchy for summarization…");
  // Uses the patchy website session cookie; summarizing requires being signed in
  const response = await fetch(`${API_BASE}/api/patch-notes/submit`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify({ techName: tech.name, url: tab.url, content }),
  });

  if (!response.ok) {
    setStatus(response.status === 401
      ? "Sign in on the Patchy website to summarize pages."
      : "Patchy couldn't process this page.");
    summarizeBtn.disabled = false;
    return;
  }

  const note = await loadLatestSummary(tech.name);
  if (note?.content) {
    setStatus(null);
    summaryEl.hidden = false;
    summaryEl.textContent = note.content;
  } else {
    setStatus("Submitted. The summary will appear on Patchy shortly.");
  }
  summarizeBtn.disabled = false;
}

async function init() {
  try {
    const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
    if (!tab?.url?.startsWith("http")) {
      setStatus("Open a website to use Patchy.");
      return;
    }

    const catalog = await getCatalog();
    const tech = matchTech(catalog, tab.url);

    if (!tech) {
      setStatus("This site isn't a tracked tech yet.");
      return;
    }

    techNameEl.textContent = tech.name;
    summarizeBtn.hidden = false;
    summarizeBtn.addEventListener("click", () => submitPage(tech, tab));

    const note = await loadLatestSummary(tech.name);
    if (note?.content) {
      setStatus(null);
      summaryEl.hidden = false;
      summaryEl.textContent = note.content;
    } else {
      setStatus(`No summary for ${tech.name} yet — summarize this page to create one.`);
    }
  } catch (error) {
    setStatus(`Error: ${error.message}`);
  }
}

init();
