package com.barrcon.patchy.services;

import com.barrcon.patchy.models.Tech;
import com.barrcon.patchy.repositories.TechRepository;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Adds techs nobody has tracked yet: package registry -> GitHub repo -> latest
// release notes. Discovered techs join the shared tech table, so every developer
// benefits from the first person who used the package. Everything here is free
// (public registry and GitHub APIs); no AI runs server-side.
@Service
public class TechDiscoveryService {

    private static final Logger log = LoggerFactory.getLogger(TechDiscoveryService.class);
    private static final Pattern GITHUB_REPO = Pattern.compile("github\\.com[/:]([\\w.\\-]+)/([\\w.\\-]+?)(?:\\.git)?(?=[^\\w.\\-]|$)");
    private static final Set<String> SUPPORTED_ECOSYSTEMS = Set.of("npm", "pypi", "crates", "rubygems", "packagist", "go",
            "hex", "pub", "language");
    private static final Pattern GITHUB_RELEASE_LINK = Pattern.compile("github\\.com/([\\w.\\-]+/[\\w.\\-]+)/releases/tag/");
    private static final Pattern GITHUB_BLOB_LINK = Pattern.compile("https://github\\.com/([\\w.\\-]+/[\\w.\\-]+)/blob/(\\S+)");
    private static final Duration MISS_TTL = Duration.ofDays(1);

    public record PackageRef(String ecosystem, String name) {
    }

    private final TechRepository techRepository;
    private final PatchNoteService patchNoteService;
    private final LocatorDiscoveryService locatorDiscovery;
    private final GeminiClient gemini;
    private final String githubToken;
    private final String githubApiUrl;
    private final String endOfLifeUrl;
    private final int maxPerHour;
    private final int refreshSlices;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    // ponytail: in-memory miss cache and hourly budget; reset on restart. The budget
    // protects GitHub's rate limit (60/h anonymous, 5000/h with GITHUB_TOKEN).
    private final Map<PackageRef, Instant> misses = new ConcurrentHashMap<>();
    private final Set<PackageRef> inFlight = ConcurrentHashMap.newKeySet();
    private Instant budgetWindowStart = Instant.now();
    private int budgetUsed;

    public TechDiscoveryService(TechRepository techRepository,
                                PatchNoteService patchNoteService,
                                LocatorDiscoveryService locatorDiscovery,
                                GeminiClient gemini,
                                @Value("${GITHUB_TOKEN:}") String githubToken,
                                @Value("${patchy.github.api-url:https://api.github.com}") String githubApiUrl,
                                @Value("${patchy.endoflife.url:https://endoflife.date}") String endOfLifeUrl,
                                @Value("${patchy.discovery.max-per-hour:30}") int maxPerHour,
                                @Value("${patchy.refresh.slices:4}") int refreshSlices) {
        this.techRepository = techRepository;
        this.patchNoteService = patchNoteService;
        this.locatorDiscovery = locatorDiscovery;
        this.gemini = gemini;
        this.githubToken = githubToken;
        this.githubApiUrl = githubApiUrl;
        this.endOfLifeUrl = endOfLifeUrl;
        this.maxPerHour = maxPerHour;
        this.refreshSlices = refreshSlices;
    }

    public boolean canDiscover(PackageRef ref) {
        if (!SUPPORTED_ECOSYSTEMS.contains(ref.ecosystem())) {
            return false;
        }
        Instant missedAt = misses.get(ref);
        return missedAt == null || missedAt.plus(MISS_TTL).isBefore(Instant.now());
    }

    @Async
    public void discover(List<PackageRef> refs) {
        for (PackageRef ref : refs) {
            if (!canDiscover(ref) || !inFlight.add(ref)) {
                continue;
            }
            try {
                if (techRepository.findByNameIgnoreCase(ref.name()).isPresent()) {
                    continue;
                }
                if (!takeBudget()) {
                    log.info("Discovery budget spent for this hour; deferring remaining packages");
                    return;
                }
                discoverOne(ref);
            } catch (GeminiClient.QuotaExceededException e) {
                log.info("Gemini quota reached; remaining packages are retried when next seen");
                return;
            } catch (Exception e) {
                log.warn("Discovery failed for {} {}: {}", ref.ecosystem(), ref.name(), e.getMessage());
                misses.put(ref, Instant.now());
            } finally {
                inFlight.remove(ref);
            }
        }
    }

    private void discoverOne(PackageRef ref) throws Exception {
        if (ref.ecosystem().equals("language")) {
            discoverLanguage(ref);
            return;
        }
        List<String> registryUrls = registryUrls(ref);
        String repo = extractGithubRepo(String.join(" ", registryUrls));
        JSONObject release = repo == null ? null : latestRelease(repo);
        if (release != null && !release.optString("body").isBlank() && releaseBelongsTo(release.optString("tag_name"), ref.name())) {
            Tech tech = techRepository.save(new Tech(ref.ecosystem(), ref.name(), repo));
            saveRelease(tech, release);
            log.info("Discovered {} ({}) from {} releases", ref.name(), ref.ecosystem(), repo);
            return;
        }

        // No GitHub releases: changelog file, or an AI-found page + selector for the crawler
        Optional<LocatorDiscoveryService.Source> source = locatorDiscovery.find(ref.name(), repo, registryUrls);
        if (source.isEmpty()) {
            misses.put(ref, Instant.now());
            return;
        }
        Tech tech = new Tech(ref.ecosystem(), ref.name(), null);
        tech.setCrawlSource(source.get().url(), source.get().selector());
        techRepository.save(tech);
        patchNoteService.processAndSave(tech, source.get().content(), source.get().url(), null);
        log.info("Discovered {} ({}) from {}{}", ref.name(), ref.ecosystem(), source.get().url(),
                source.get().selector() == null ? "" : " using selector " + source.get().selector());
    }

    // Monorepos release several packages: "http2-v3.1.0" in dart-lang/http is not
    // the http package. Tags with a different name prefix are rejected.
    static boolean releaseBelongsTo(String tag, String packageName) {
        String fullPrefix = tag.replaceFirst("(?:^|[-_@/])v?\\d.*$", "");
        String prefix = fullPrefix.substring(fullPrefix.lastIndexOf('/') + 1);
        String name = packageName.substring(packageName.lastIndexOf('/') + 1);
        return prefix.isEmpty() || prefix.replaceAll("[^A-Za-z0-9]", "").equalsIgnoreCase(name.replaceAll("[^A-Za-z0-9]", ""));
    }

    // Languages have no package registry. endoflife.date (free) knows the latest
    // version and links its release notes for hundreds of languages and runtimes;
    // a link to GitHub releases upgrades the tech to the GitHub release poller.
    private void discoverLanguage(PackageRef ref) throws Exception {
        JSONObject product = getJson(endOfLifeUrl + "/api/v1/products/" + ref.name());
        if (product == null) {
            discoverLanguageWithAi(ref);
            return;
        }
        String label = product.getJSONObject("result").optString("label", ref.name());
        Tech tech = new Tech("language", ref.name(), null);
        tech.setName(label);
        Matcher release = GITHUB_RELEASE_LINK.matcher(latestLanguageRelease(product).optString("link"));
        if (release.find()) {
            tech.setSourceRepo(release.group(1));
            techRepository.save(tech);
            JSONObject latest = latestRelease(release.group(1));
            if (latest != null && !latest.optString("body").isBlank()) {
                saveRelease(tech, latest);
            }
        } else {
            techRepository.save(tech);
            refreshLanguage(tech);
        }
        log.info("Discovered language {} via endoflife.date{}", label,
                tech.getSourceRepo() == null ? "" : " -> " + tech.getSourceRepo() + " releases");
    }

    static final String LANGUAGE_REPO_PROMPT = """
            Give the GitHub owner/repo where the official compiler, SDK or runtime of the named programming
            language is developed and where its releases or changelog are published (for example rust-lang/rust).
            Answer with an empty repo if you are not certain; a wrong answer is worse than none.""";
    private static final JSONObject LANGUAGE_REPO_SCHEMA = new JSONObject("""
            {"type": "OBJECT", "properties": {"repo": {"type": "STRING"}}, "required": ["repo"]}""");

    // Languages endoflife.date doesn't track (Dart, Zig, ...): Gemini names the repo,
    // and GitHub must confirm it exists before anything is stored
    private void discoverLanguageWithAi(PackageRef ref) throws Exception {
        String repo = gemini.enabled()
                ? gemini.generateJson(LANGUAGE_REPO_PROMPT, "Language: " + ref.name(), LANGUAGE_REPO_SCHEMA).optString("repo").strip()
                : "";
        if (!repo.matches("[\\w.\\-]+/[\\w.\\-]+") || getJson(githubApiUrl + "/repos/" + repo) == null) {
            misses.put(ref, Instant.now());
            return;
        }
        String label = Character.toUpperCase(ref.name().charAt(0)) + ref.name().substring(1);
        Tech tech = new Tech("language", ref.name(), null);
        tech.setName(label);
        JSONObject release = latestRelease(repo);
        if (release != null && !release.optString("body").isBlank()) {
            tech.setSourceRepo(repo);
            techRepository.save(tech);
            saveRelease(tech, release);
        } else {
            Optional<LocatorDiscoveryService.Source> source = locatorDiscovery.find(label, repo, List.of());
            if (source.isEmpty()) {
                misses.put(ref, Instant.now());
                return;
            }
            tech.setCrawlSource(source.get().url(), source.get().selector());
            techRepository.save(tech);
            patchNoteService.processAndSave(tech, source.get().content(), source.get().url(), null);
        }
        log.info("Discovered language {} via AI-named repo {}", label, repo);
    }

    private void refreshLanguage(Tech tech) throws Exception {
        JSONObject product = getJson(endOfLifeUrl + "/api/v1/products/" + tech.getPackageName());
        if (product == null) {
            return;
        }
        JSONObject latest = latestLanguageRelease(product);
        String link = latest.optString("link");
        String content = fetchReleaseNotes(link);
        String header = tech.getName() + " " + latest.optString("name") + " released " + latest.optString("date");
        patchNoteService.processAndSave(tech, content == null ? header + "\nRelease notes: " + link : header + "\n\n" + content,
                link.isBlank() ? endOfLifeUrl + "/" + tech.getPackageName() : link, latest.optString("name"));
    }

    private static JSONObject latestLanguageRelease(JSONObject product) {
        return product.getJSONObject("result").getJSONArray("releases").getJSONObject(0).optJSONObject("latest", new JSONObject());
    }

    // Raw text for GitHub-hosted changelogs, the main content of anything else
    private String fetchReleaseNotes(String link) {
        if (link == null || link.isBlank()) {
            return null;
        }
        try {
            Matcher blob = GITHUB_BLOB_LINK.matcher(link);
            if (blob.matches()) {
                return Jsoup.connect("https://raw.githubusercontent.com/" + blob.group(1) + "/" + blob.group(2).replaceFirst("#.*$", ""))
                        .ignoreContentType(true).timeout(10_000).execute().body();
            }
            org.jsoup.nodes.Document page = Jsoup.connect(link).userAgent("patchy-discovery").timeout(15_000).get();
            org.jsoup.nodes.Element main = page.selectFirst("main, article, #content, .content");
            return (main == null ? page.body() : main).wholeText().replaceAll("\\n{3,}", "\n\n").strip();
        } catch (Exception e) {
            log.info("Couldn't fetch release notes from {}: {}", link, e.getMessage());
            return null;
        }
    }

    @Async
    public void relocate(Tech tech) {
        if (tech.getPackageName() == null) {
            log.warn("Crawl selector for catalog tech {} matched nothing; update tech-catalog.json", tech.getName());
            return;
        }
        try {
            PackageRef ref = new PackageRef(tech.getEcosystem(), tech.getPackageName());
            List<String> registryUrls = registryUrls(ref);
            Optional<LocatorDiscoveryService.Source> source =
                    locatorDiscovery.find(tech.getName(), extractGithubRepo(String.join(" ", registryUrls)), registryUrls);
            if (source.isPresent()) {
                tech.setCrawlSource(source.get().url(), source.get().selector());
                techRepository.save(tech);
                patchNoteService.processAndSave(tech, source.get().content(), source.get().url(), null);
                log.info("Relocated release notes for {} to {}", tech.getName(), source.get().url());
            } else {
                log.warn("Couldn't relocate release notes for {}; keeping the old locator", tech.getName());
            }
        } catch (Exception e) {
            log.warn("Relocate failed for {}: {}", tech.getName(), e.getMessage());
        }
    }

    // Every GitHub-sourced or changelog-file tech is checked once a day, rotating through slices so
    // requests spread over several hours. Default: 20:00-23:00 UTC, measured from
    // 1,624 releases of tracked repos: 80% publish by 20:00 UTC, 96% by 23:00 UTC
    // (mostly Tue-Thu), so same-day releases land before EU/Asia mornings.
    // Slice for a run = UTC hour % slices, so the cron hours must cover every slice.
    @Scheduled(cron = "${patchy.refresh.cron:0 0 20-23 * * *}", zone = "UTC")
    public void refreshSlice() {
        int slice = ZonedDateTime.now(ZoneOffset.UTC).getHour() % refreshSlices;
        for (Tech tech : techRepository.findAll()) {
            boolean changelogFile = tech.getCrawlUrl() != null && tech.getCrawlSelector() == null;
            boolean endOfLifeLanguage = ("language".equals(tech.getEcosystem()) || "endoflife".equals(tech.getEcosystem()))
                    && tech.getSourceRepo() == null && tech.getCrawlUrl() == null;
            if ((tech.getSourceRepo() == null && !changelogFile && !endOfLifeLanguage) || tech.getId() % refreshSlices != slice) {
                continue;
            }
            try {
                if (endOfLifeLanguage) {
                    refreshLanguage(tech);
                } else if (changelogFile) {
                    String text = Jsoup.connect(tech.getCrawlUrl()).ignoreContentType(true).timeout(10_000).execute().body();
                    patchNoteService.processAndSave(tech, text, tech.getCrawlUrl(), null);
                } else {
                    JSONObject release = latestRelease(tech.getSourceRepo());
                    if (release != null && !release.optString("body").isBlank()) {
                        saveRelease(tech, release);
                    }
                }
                tech.setLastCheckedAt(LocalDateTime.now());
                techRepository.save(tech);
            } catch (Exception e) {
                log.warn("Refresh failed for {}: {}", tech.getName(), e.getMessage());
            }
        }
    }

    private void saveRelease(Tech tech, JSONObject release) {
        String body = release.optString("name") + "\n\n" + release.optString("body");
        // Some projects' release body is only a link to their changelog (Gleam); follow it
        Matcher changelogLink = GITHUB_BLOB_LINK.matcher(release.optString("body").strip());
        if (release.optString("body").length() < 400 && changelogLink.find()) {
            String linked = fetchReleaseNotes(changelogLink.group().replaceFirst("[)>\\].,]+$", ""));
            if (linked != null) {
                body = release.optString("name") + "\n\n" + linked;
            }
        }
        patchNoteService.processAndSave(tech, body, release.optString("html_url"), release.optString("tag_name"));
    }

    private synchronized boolean takeBudget() {
        if (budgetWindowStart.plus(Duration.ofHours(1)).isBefore(Instant.now())) {
            budgetWindowStart = Instant.now();
            budgetUsed = 0;
        }
        return budgetUsed++ < maxPerHour;
    }

    // Source, homepage and changelog links the registry publishes for a package,
    // most specific first. Used to find its GitHub repo and, failing that, its
    // release-notes page.
    List<String> registryUrls(PackageRef ref) throws Exception {
        String name = URLEncoder.encode(ref.name(), StandardCharsets.UTF_8).replace("%2F", "/").replace("%40", "@");
        List<String> urls = new ArrayList<>();
        switch (ref.ecosystem()) {
            case "go" -> urls.add("https://" + ref.name());
            case "hex" -> {
                JSONObject json = getJson("https://hex.pm/api/packages/" + name);
                JSONObject links = json == null ? null : json.optJSONObject("meta", new JSONObject()).optJSONObject("links");
                if (links != null) {
                    links.keySet().forEach(key -> urls.add(links.optString(key)));
                }
            }
            case "pub" -> {
                JSONObject json = getJson("https://pub.dev/api/packages/" + name);
                JSONObject pubspec = json == null ? null : json.getJSONObject("latest").getJSONObject("pubspec");
                if (pubspec != null) {
                    urls.addAll(List.of(pubspec.optString("repository"), pubspec.optString("homepage"), pubspec.optString("issue_tracker")));
                }
            }
            case "npm" -> {
                JSONObject json = getJson("https://registry.npmjs.org/" + name + "/latest");
                if (json != null) {
                    Object repository = json.opt("repository");
                    urls.add(repository instanceof JSONObject obj ? obj.optString("url") : String.valueOf(repository));
                    urls.add(json.optString("homepage"));
                }
            }
            case "pypi" -> {
                JSONObject json = getJson("https://pypi.org/pypi/" + name + "/json");
                JSONObject info = json == null ? null : json.optJSONObject("info");
                if (info != null) {
                    JSONObject projectUrls = info.optJSONObject("project_urls", new JSONObject());
                    projectUrls.keySet().forEach(key -> urls.add(projectUrls.optString(key)));
                    urls.add(info.optString("home_page"));
                }
            }
            case "crates" -> {
                JSONObject json = getJson("https://crates.io/api/v1/crates/" + name);
                if (json != null) {
                    JSONObject crate = json.getJSONObject("crate");
                    urls.addAll(List.of(crate.optString("repository"), crate.optString("homepage"), crate.optString("documentation")));
                }
            }
            case "rubygems" -> {
                JSONObject json = getJson("https://rubygems.org/api/v1/gems/" + name + ".json");
                if (json != null) {
                    urls.addAll(List.of(json.optString("source_code_uri"), json.optString("changelog_uri"), json.optString("homepage_uri")));
                }
            }
            case "packagist" -> {
                JSONObject json = getJson("https://repo.packagist.org/p2/" + name + ".json");
                if (json != null) {
                    JSONObject latest = json.getJSONObject("packages").getJSONArray(ref.name()).getJSONObject(0);
                    urls.add(latest.optJSONObject("source", new JSONObject()).optString("url"));
                    urls.add(latest.optString("homepage"));
                }
            }
            default -> {
            }
        }
        return urls.stream().filter(url -> url != null && !url.isBlank() && !url.equals("null")).distinct().toList();
    }

    static String extractGithubRepo(String text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = GITHUB_REPO.matcher(text);
        return matcher.find() ? matcher.group(1) + "/" + matcher.group(2) : null;
    }

    private JSONObject latestRelease(String repo) throws Exception {
        return githubToken.isBlank() ? latestReleaseFromFeed(repo) : getJson(githubApiUrl + "/repos/" + repo + "/releases/latest");
    }

    // Without a token the REST API allows 60 requests/hour, too few for a large
    // catalog. The public releases Atom feed isn't rate-limited that way and carries
    // the newest release's notes (as HTML), so it stands in for the API.
    private JSONObject latestReleaseFromFeed(String repo) throws Exception {
        return parseReleaseFeed(Jsoup.connect("https://github.com/" + repo + "/releases.atom")
                .userAgent("patchy-discovery").timeout(10_000).parser(org.jsoup.parser.Parser.xmlParser()).get());
    }

    static JSONObject parseReleaseFeed(org.jsoup.nodes.Document feed) {
        org.jsoup.nodes.Element entry = feed.selectFirst("entry");
        if (entry == null) {
            return null;
        }
        String link = entry.selectFirst("link") == null ? "" : entry.selectFirst("link").attr("href");
        return new JSONObject()
                .put("tag_name", link.substring(link.lastIndexOf('/') + 1))
                .put("name", entry.select("title").text())
                .put("html_url", link)
                .put("body", Jsoup.parse(entry.select("content").text()).wholeText().strip());
    }

    private JSONObject getJson(String url) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "patchy-discovery")
                .header("Accept", "application/json");
        if (url.startsWith(githubApiUrl) && !githubToken.isBlank()) {
            request.header("Authorization", "Bearer " + githubToken);
        }
        HttpResponse<String> response = http.send(request.build(), HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200 ? new JSONObject(response.body()) : null;
    }
}
