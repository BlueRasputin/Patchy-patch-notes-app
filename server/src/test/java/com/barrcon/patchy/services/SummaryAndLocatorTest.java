package com.barrcon.patchy.services;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SummaryAndLocatorTest {

    @Test
    void groupsItemsIntoOrderedCategoriesWithMigrationAndIds() {
        SummaryService.Summary summary = SummaryService.parse(new JSONObject("""
                {"isReleaseNotes": true, "version": "5.0.0", "headline": "Drops Node 18 and fixes a prototype pollution bug.",
                 "urgency": "high",
                 "items": [
                   {"category": "Bug Fixes", "title": "merge()", "detail": "Handles arrays."},
                   {"category": "Security", "title": "set()", "detail": "Blocks __proto__ keys. Severity: high.", "identifiers": ["CVE-2026-1234"]},
                   {"category": "Breaking Changes", "title": "Node 18", "detail": "No longer supported.", "migration": "Upgrade to Node 20+."},
                   {"category": "Made Up", "title": "x", "detail": "dropped"}
                 ]}"""));

        assertTrue(summary.isReleaseNotes());
        assertEquals("high", summary.urgency());
        assertEquals(java.util.List.of("Breaking Changes", "Security", "Bug Fixes"),
                summary.sections().stream().map(section -> section.getCategory()).toList());
        assertTrue(summary.markdown().startsWith("Drops Node 18"));
        assertTrue(summary.markdown().contains("- **Node 18**: No longer supported.\n  - Migration: Upgrade to Node 20+."));
        assertTrue(summary.markdown().contains("(CVE-2026-1234)"));
        assertFalse(summary.markdown().contains("dropped"));

        assertFalse(SummaryService.parse(new JSONObject("{\"isReleaseNotes\": false}")).isReleaseNotes());
    }

    @Test
    void reportsTheFirstFixAboveTheVersionInUse() {
        JSONObject vuln = new JSONObject("""
                {"affected": [{"package": {"name": "phoenix"}, "ranges": [{"events": [
                  {"introduced": "0"}, {"fixed": "1.5.15"}, {"introduced": "1.6.0"}, {"fixed": "1.6.16"},
                  {"introduced": "1.7.0"}, {"fixed": "1.7.22"}]}]}]}""");
        assertEquals("1.7.22", AdvisoryService.fixedVersion(vuln, "phoenix", "1.7.14"));
        assertEquals("1.6.16", AdvisoryService.fixedVersion(vuln, "phoenix", "1.6.2"));
        assertNull(AdvisoryService.fixedVersion(vuln, "plug", "1.7.14"));
    }

    @Test
    void readsGeminiTextSkippingThoughtParts() {
        JSONObject response = new JSONObject("""
                {"candidates": [{"content": {"parts": [{"text": "thinking...", "thought": true}, {"text": "{\\"a\\": 1}"}]}}]}""");
        assertEquals("{\"a\": 1}", GeminiClient.textOf(response));
    }

    @Test
    void acceptsOnlySelectorsThatExtractReleaseText() {
        String release = "Version 2.4.1 released. ".repeat(20);
        Document page = Jsoup.parse("""
                <html><body><nav>%s</nav>
                <main id="content"><article class="release-notes"><h2>v2.4.1</h2><p>%s</p></article></main>
                <div class="promo">Buy our product. %s</div></body></html>""".formatted("Home Docs ".repeat(30), release, "Sale! ".repeat(60)));

        assertTrue(LocatorDiscoveryService.extract(page, "article.release-notes").isPresent());
        assertTrue(LocatorDiscoveryService.extract(page, "div.promo").isEmpty(), "no version text");
        assertTrue(LocatorDiscoveryService.extract(page, "section.missing").isEmpty());
        assertTrue(LocatorDiscoveryService.extract(page, "[[bad").isEmpty(), "invalid selector");

        String outline = LocatorDiscoveryService.outline(page);
        assertTrue(outline.contains("main#content"));
        assertTrue(outline.contains("article.release-notes"));
        assertFalse(outline.contains("nav"), "navigation is excluded from the outline");
    }
}
