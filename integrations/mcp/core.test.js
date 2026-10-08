const test = require("node:test");
const assert = require("node:assert");
const fs = require("node:fs");
const os = require("node:os");
const path = require("node:path");

process.env.PATCHY_HOME = fs.mkdtempSync(path.join(os.tmpdir(), "patchy-home-"));
const core = require("./core");

test("first scan is silent, later releases alert, offline falls back to the local cache", async () => {
  const project = fs.mkdtempSync(path.join(os.tmpdir(), "patchy-project-"));
  fs.writeFileSync(path.join(project, "package.json"), '{"dependencies":{"react":"^19"}}');
  fs.mkdirSync(path.join(project, "node_modules", "x"), { recursive: true });
  fs.writeFileSync(path.join(project, "node_modules", "x", "package.json"), "{}");

  let noteId = 1;
  const sent = [];
  global.fetch = async (url, options) => {
    sent.push({ url, body: options.body && JSON.parse(options.body) });
    return {
      ok: true, status: 200,
      json: async () => ({
        matches: [{ patchNote: { id: noteId, techName: "React", content: "c", categories: noteId > 1 ? ["Breaking Changes"] : [] } }],
        techNames: ["React"], discovering: ["left-pad"],
        advisories: [{ packageName: "react", version: "19.0.0", id: "GHSA-test", summary: "s", url: "u" }],
      }),
    };
  };
  const config = { apiUrl: "http://patchy.test", token: null };

  const first = await core.scanProject(project, config);
  assert.equal(first.firstScan, true);
  assert.deepEqual(first.newNotes, []);
  assert.deepEqual(first.newAdvisories.map((a) => a.id), ["GHSA-test"], "advisories alert on first scan");
  assert.deepEqual(Object.keys(sent[0].body.files), ["package.json"], "node_modules is skipped");

  noteId = 2;
  const second = await core.scanProject(project, config);
  assert.equal(second.firstScan, false);
  assert.deepEqual(second.newAdvisories, [], "an advisory alerts once");
  assert.deepEqual(second.newNotes.map((n) => n.id), [2]);
  assert.ok(core.isBreaking(second.newNotes[0]));

  core.markSeen(second.newNotes);
  assert.deepEqual((await core.scanProject(project, config)).newNotes, []);

  global.fetch = async () => { throw new Error("down"); };
  const offline = await core.scanProject(project, config);
  assert.equal(offline.offline, true);
  assert.deepEqual(offline.notes.map((n) => n.id), [2]);
  assert.equal(fs.statSync(core.DATA_FILE).mode & 0o777, 0o600);
});
