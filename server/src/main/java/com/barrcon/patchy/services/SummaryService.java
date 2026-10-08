package com.barrcon.patchy.services;

import com.barrcon.patchy.dto.PatchNoteSectionDTO;
import com.barrcon.patchy.models.PatchNote;
import com.barrcon.patchy.repositories.PatchNoteRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Turns raw release notes into structured, categorized summaries with Gemini's
// free tier. Notes are queued (pendingSummary) when the crawler or release
// poller detects a change; this drains the queue a few at a time, and on a
// quota error simply stops until the next run. Nothing here can incur a charge.
@Service
public class SummaryService {

    private static final Logger log = LoggerFactory.getLogger(SummaryService.class);

    // Display order; also the only categories the model may use
    static final List<String> CATEGORIES = List.of("Breaking Changes", "Security", "Deprecations", "New Requirements",
            "New Features", "Bug Fixes", "Performance", "Known Issues", "Documentation");

    static final String SYSTEM_PROMPT = """
            You extract structured release information from software release notes for working developers.

            Input: the tech name, the page URL, and the raw text of a release-notes page, changelog, or GitHub release.
            The text may include site navigation, sponsor lists, contributor credits and several releases.

            Rules:
            - Describe only the NEWEST release in the text. If an expected version is given, describe that one.
            - Skip sections marked Unreleased, upcoming, dev, nightly, beta or release-candidate unless they are
              the only release present; developers need what has actually shipped.
            - Use only facts stated in the text. Never invent versions, dates, CVE/GHSA IDs, APIs or numbers.
            - If the text is not release notes (an index page, docs, a blog listing without details, an error page),
              set isReleaseNotes=false and leave the other fields empty.
            - One item per distinct change. Merge trivial items (typo fixes, dependency bumps, internal refactors)
              into at most one item per category. Ignore contributor lists, links-only lines and marketing copy.
            - Categories (use exactly one per item):
              Breaking Changes: anything that can break existing code, configs, builds or behavior on upgrade,
                including removed APIs, changed defaults and dropped platform/runtime support.
                Put the concrete upgrade steps in `migration`.
              Security: vulnerability fixes and hardening. Put every CVE-/GHSA- ID in `identifiers` and the stated
                severity (critical/high/moderate/low) in `detail` if given.
              Deprecations: features still working but scheduled for removal. Name the replacement and the removal
                version in `detail` when stated.
              New Requirements: new minimum versions of runtimes, compilers, OSes, browsers or peer dependencies.
              New Features, Bug Fixes, Performance, Known Issues, Documentation: as named.
            - `title`: a short phrase naming the affected API, command, option or component (use code names verbatim).
            - `detail`: one or two plain sentences on what changed and who is affected.
            - `headline`: one sentence a developer can scan, leading with the most important change.
            - `urgency`:
              critical = actively exploited or critical-severity vulnerability, data loss or corruption fix;
              high = any security fix, or breaking changes most users will hit on upgrade;
              normal = features and fixes; low = docs, tooling or internal-only changes.
            - `releaseDate`: YYYY-MM-DD only if stated, else empty.""";

    private static final JSONObject SCHEMA = new JSONObject("""
            {
              "type": "OBJECT",
              "properties": {
                "isReleaseNotes": {"type": "BOOLEAN"},
                "version": {"type": "STRING"},
                "releaseDate": {"type": "STRING"},
                "headline": {"type": "STRING"},
                "urgency": {"type": "STRING", "enum": ["critical", "high", "normal", "low"]},
                "items": {
                  "type": "ARRAY",
                  "items": {
                    "type": "OBJECT",
                    "properties": {
                      "category": {"type": "STRING", "enum": %s},
                      "title": {"type": "STRING"},
                      "detail": {"type": "STRING"},
                      "migration": {"type": "STRING"},
                      "identifiers": {"type": "ARRAY", "items": {"type": "STRING"}}
                    },
                    "required": ["category", "title", "detail"]
                  }
                }
              },
              "required": ["isReleaseNotes", "version", "headline", "urgency", "items"]
            }""".formatted(new JSONArray(CATEGORIES)));

    public record Summary(boolean isReleaseNotes, String version, String headline, String urgency,
                          String markdown, List<PatchNoteSectionDTO> sections) {
    }

    private final GeminiClient gemini;
    private final PatchNoteRepository patchNoteRepository;
    private final PatchNoteCategoryService categoryService;
    private final PatchNoteSectionService sectionService;

    public SummaryService(GeminiClient gemini, PatchNoteRepository patchNoteRepository,
                          PatchNoteCategoryService categoryService, PatchNoteSectionService sectionService) {
        this.gemini = gemini;
        this.patchNoteRepository = patchNoteRepository;
        this.categoryService = categoryService;
        this.sectionService = sectionService;
    }

    @Scheduled(fixedDelayString = "${patchy.summary.interval:PT2M}", initialDelayString = "PT30S")
    public void summarizePending() {
        if (!gemini.enabled()) {
            return;
        }
        for (PatchNote note : patchNoteRepository.findTop10ByPendingSummaryTrueOrderByCreatedAtAsc()) {
            try {
                apply(note, summarize(note.getTech().getName(), note.getSourceUrl(), note.getReleaseVersion(), note.getOriginalContent()));
            } catch (GeminiClient.QuotaExceededException e) {
                log.info("Gemini quota reached; {} will be summarized on a later run", note.getTech().getName());
                return;
            } catch (Exception e) {
                log.warn("Summary failed for {}: {}", note.getTech().getName(), e.getMessage());
            }
        }
    }

    private void apply(PatchNote note, Summary summary) {
        if (summary.isReleaseNotes()) {
            note.setContent(summary.markdown());
            note.setSummarySections(sectionService.serialize(summary.sections()));
            note.setCategories(categoryService.serializeCategories(
                    summary.sections().stream().map(PatchNoteSectionDTO::getCategory).toList()));
            note.setHeadline(summary.headline());
            note.setUrgency(summary.urgency());
            if (note.getReleaseVersion() == null || note.getReleaseVersion().isBlank()) {
                note.setReleaseVersion(summary.version());
            }
        }
        // Not release notes: keep the raw excerpt rather than retrying forever
        note.setPendingSummary(false);
        note.setOriginalContent(null);
        patchNoteRepository.save(note);
    }

    public Summary summarize(String techName, String sourceUrl, String expectedVersion, String content) throws Exception {
        String input = "Tech: " + techName + "\nURL: " + sourceUrl
                + (expectedVersion == null || expectedVersion.isBlank() ? "" : "\nExpected version: " + expectedVersion)
                + "\n\nRelease notes:\n" + content;
        return parse(gemini.generateJson(SYSTEM_PROMPT, input, SCHEMA));
    }

    static Summary parse(JSONObject json) {
        if (!json.optBoolean("isReleaseNotes")) {
            return new Summary(false, "", "", "", "", List.of());
        }
        Map<String, List<String>> bullets = new LinkedHashMap<>();
        CATEGORIES.forEach(category -> bullets.put(category, new ArrayList<>()));
        JSONArray items = json.optJSONArray("items", new JSONArray());
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            List<String> target = bullets.get(item.optString("category"));
            if (target == null || item.optString("title").isBlank()) {
                continue;
            }
            StringBuilder bullet = new StringBuilder("- **" + item.optString("title").strip() + "**: " + item.optString("detail").strip());
            JSONArray ids = item.optJSONArray("identifiers");
            if (ids != null && !ids.isEmpty()) {
                bullet.append(" (").append(String.join(", ", ids.toList().stream().map(String::valueOf).toList())).append(")");
            }
            if (!item.optString("migration").isBlank()) {
                bullet.append("\n  - Migration: ").append(item.optString("migration").strip());
            }
            target.add(bullet.toString());
        }

        List<PatchNoteSectionDTO> sections = new ArrayList<>();
        StringBuilder markdown = new StringBuilder(json.optString("headline").strip());
        bullets.forEach((category, lines) -> {
            if (!lines.isEmpty()) {
                String body = String.join("\n", lines);
                sections.add(new PatchNoteSectionDTO(category, body));
                markdown.append("\n\n## ").append(category).append("\n").append(body);
            }
        });
        return new Summary(true, json.optString("version").strip(), json.optString("headline").strip(),
                json.optString("urgency", "normal"), markdown.toString(), sections);
    }
}
