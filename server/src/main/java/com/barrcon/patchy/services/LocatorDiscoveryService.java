package com.barrcon.patchy.services;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Finds where a tech without GitHub releases publishes its release notes, and
// how to scrape them:
//   1. a CHANGELOG/CHANGES/HISTORY/NEWS file in its GitHub repo (no AI, fetched directly)
//   2. otherwise Gemini picks the release-notes page from registry/homepage links and
//      proposes a CSS selector, which is only accepted after a test extraction yields
//      real release text. The Playwright crawler then reads that selector daily.
@Service
public class LocatorDiscoveryService {

    public record Source(String url, String selector, String content) {
    }

    private static final Logger log = LoggerFactory.getLogger(LocatorDiscoveryService.class);
    private static final Pattern VERSION = Pattern.compile("\\bv?\\d+\\.\\d+(\\.\\d+)?\\b");
    private static final Pattern GITHUB_TREE = Pattern.compile("github\\.com/([\\w.\\-]+/[\\w.\\-]+)/tree/([^/]+)/(\\S+)");
    private static final Pattern RELEASE_LINK = Pattern.compile("(?i)change.?log|release|what.?s.?new|\\bnews\\b|history");
    private static final List<String> CHANGELOG_FILES = List.of("CHANGELOG.md", "CHANGES.md", "HISTORY.md", "NEWS.md", "CHANGELOG.rst");
    private static final Set<String> SKIP_TAGS = Set.of("script", "style", "nav", "header", "footer", "svg", "noscript", "form");
    private static final int MIN_TEXT = 200;

    static final String PAGE_PROMPT = """
            You pick the page where a software project publishes its release notes or changelog.
            Choose the single URL most likely to contain the newest release's notes (a changelog, release notes,
            "what's new" or releases page). Prefer official project sites over third-party mirrors.
            Return an empty url if none of the candidates is a release-notes page. Only return a URL from the list.""";

    static final String SELECTOR_PROMPT = """
            You write a CSS selector for a web scraper that extracts release notes from a page.
            You get an outline of the page: one line per element with a selector, its text length and how its
            text starts. Return the selector of the smallest element that contains the release notes (ideally the
            newest release and its details), excluding navigation, sidebars, footers and comment sections.
            The selector must be stable across releases: prefer ids, semantic tags (main, article) and meaningful
            class names. Never use :nth-child, generated/hashed class names, or text-based selectors.
            Set isReleaseNotesPage=false if the outline shows no release notes.""";

    private static final JSONObject PAGE_SCHEMA = new JSONObject("""
            {"type": "OBJECT", "properties": {"url": {"type": "STRING"}}, "required": ["url"]}""");
    private static final JSONObject SELECTOR_SCHEMA = new JSONObject("""
            {"type": "OBJECT",
             "properties": {"isReleaseNotesPage": {"type": "BOOLEAN"}, "contentSelector": {"type": "STRING"}},
             "required": ["isReleaseNotesPage", "contentSelector"]}""");

    private final GeminiClient gemini;

    public LocatorDiscoveryService(GeminiClient gemini) {
        this.gemini = gemini;
    }

    public Optional<Source> find(String techName, String githubRepo, List<String> registryUrls) {
        // Monorepos (flutter/packages, dart-lang/http) link the package's own folder;
        // its changelog beats the repo root's
        List<String> changelogDirs = new ArrayList<>();
        for (String url : registryUrls) {
            Matcher tree = GITHUB_TREE.matcher(url);
            if (tree.find()) {
                changelogDirs.add(tree.group(1) + "/" + tree.group(2) + "/" + tree.group(3).replaceFirst("/$", ""));
            }
        }
        if (githubRepo != null) {
            changelogDirs.add(githubRepo + "/HEAD");
        }
        for (String dir : changelogDirs) {
            for (String file : CHANGELOG_FILES) {
                String url = "https://raw.githubusercontent.com/" + dir + "/" + file;
                Optional<String> text = fetchText(url);
                if (text.isPresent() && looksLikeReleaseNotes(text.get())) {
                    return Optional.of(new Source(url, null, text.get()));
                }
            }
        }
        if (!gemini.enabled()) {
            return Optional.empty();
        }
        try {
            Map<String, String> candidates = candidates(registryUrls);
            if (candidates.isEmpty()) {
                return Optional.empty();
            }
            StringBuilder list = new StringBuilder("Tech: " + techName + "\nCandidates:\n");
            candidates.forEach((url, label) -> list.append("- ").append(url).append("  (").append(label).append(")\n"));
            String pageUrl = gemini.generateJson(PAGE_PROMPT, list.toString(), PAGE_SCHEMA).optString("url");
            if (!candidates.containsKey(pageUrl)) {
                return Optional.empty();
            }

            Document page = Jsoup.connect(pageUrl).userAgent("patchy-discovery").timeout(15_000).get();
            JSONObject answer = gemini.generateJson(SELECTOR_PROMPT,
                    "Tech: " + techName + "\nURL: " + pageUrl + "\n\nOutline:\n" + outline(page), SELECTOR_SCHEMA);
            if (!answer.optBoolean("isReleaseNotesPage")) {
                return Optional.empty();
            }
            String selector = answer.optString("contentSelector");
            return extract(page, selector).map(text -> new Source(pageUrl, selector, text));
        } catch (GeminiClient.QuotaExceededException e) {
            throw e;
        } catch (Exception e) {
            log.info("Locator discovery failed for {}: {}", techName, e.getMessage());
            return Optional.empty();
        }
    }

    // Registry links plus release-looking links found on those pages (homepages, docs)
    private Map<String, String> candidates(List<String> registryUrls) {
        Map<String, String> candidates = new LinkedHashMap<>();
        for (String url : registryUrls) {
            if (!url.startsWith("http") || candidates.size() >= 25) {
                continue;
            }
            candidates.putIfAbsent(url, "from package registry");
            try {
                for (Element link : Jsoup.connect(url).userAgent("patchy-discovery").timeout(10_000).get().select("a[href]")) {
                    String href = link.absUrl("href").replaceFirst("#.*$", "");
                    if (href.startsWith("http") && RELEASE_LINK.matcher(link.text() + " " + href).find() && candidates.size() < 25) {
                        candidates.putIfAbsent(href, "link \"" + link.text().strip() + "\" on " + url);
                    }
                }
            } catch (Exception e) {
                // Unreachable or non-HTML candidate; keep the URL itself
            }
        }
        return candidates;
    }

    static String outline(Document page) {
        List<String> lines = new ArrayList<>();
        walk(page.body(), 0, lines);
        return String.join("\n", lines.subList(0, Math.min(lines.size(), 150)));
    }

    private static void walk(Element element, int depth, List<String> lines) {
        for (Element child : element.children()) {
            if (SKIP_TAGS.contains(child.tagName()) || depth > 10) {
                continue;
            }
            int textLength = child.text().length();
            if (textLength < MIN_TEXT && !child.tagName().matches("h[1-3]")) {
                continue;
            }
            String start = child.text().substring(0, Math.min(90, textLength)).replaceAll("\\s+", " ");
            lines.add("  ".repeat(depth) + describe(child) + " | " + textLength + " chars | " + start);
            walk(child, depth + 1, lines);
        }
    }

    static String describe(Element element) {
        StringBuilder selector = new StringBuilder(element.tagName());
        if (!element.id().isBlank()) {
            selector.append('#').append(element.id());
        }
        element.classNames().stream().limit(3).forEach(name -> selector.append('.').append(name));
        return selector.toString();
    }

    // The acceptance test for an AI-proposed selector
    static Optional<String> extract(Document page, String selector) {
        try {
            Elements matched = page.select(selector);
            String text = matched.isEmpty() ? "" : matched.first().wholeText().replaceAll("[ \\t]+", " ").replaceAll("\\n{3,}", "\n\n").strip();
            return looksLikeReleaseNotes(text) ? Optional.of(text) : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    static boolean looksLikeReleaseNotes(String text) {
        return text.length() >= MIN_TEXT && VERSION.matcher(text).find();
    }

    private static Optional<String> fetchText(String url) {
        try {
            return Optional.of(Jsoup.connect(url).userAgent("patchy-discovery").ignoreContentType(true)
                    .timeout(10_000).execute().body());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
