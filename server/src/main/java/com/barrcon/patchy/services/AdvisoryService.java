package com.barrcon.patchy.services;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Known vulnerabilities for a project's dependencies, from OSV.dev (free, no key).
// Runs on every scan, so security problems surface immediately rather than on the
// daily release-notes schedule.
@Service
public class AdvisoryService {

    public record Dependency(String ecosystem, String name, String versionRange) {
    }

    public record Advisory(String packageName, String version, String id, String summary, String fixedIn, String url) {
    }

    private static final Logger log = LoggerFactory.getLogger(AdvisoryService.class);
    private static final Map<String, String> OSV_ECOSYSTEMS = Map.of(
            "npm", "npm", "pypi", "PyPI", "crates", "crates.io", "rubygems", "RubyGems", "packagist", "Packagist", "go", "Go",
            "hex", "Hex", "pub", "Pub", "nuget", "NuGet");
    private static final Pattern VERSION = Pattern.compile("\\d+(?:\\.\\d+)+");
    private static final int MAX_DETAILS = 15;

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    // ponytail: manifests hold ranges, so we check the lowest allowed version ("^19.0.0" -> 19.0.0).
    // That can over-report when the lockfile already resolved a fixed version; send lockfiles to tighten it.
    static String baseVersion(String range) {
        if (range == null) {
            return null;
        }
        Matcher matcher = VERSION.matcher(range);
        return matcher.find() ? matcher.group() : null;
    }

    public List<Advisory> check(List<Dependency> dependencies) {
        List<Dependency> queryable = dependencies.stream()
                .filter(dep -> OSV_ECOSYSTEMS.containsKey(dep.ecosystem()) && baseVersion(dep.versionRange()) != null)
                .limit(1000)
                .toList();
        if (queryable.isEmpty()) {
            return List.of();
        }
        try {
            JSONArray queries = new JSONArray();
            for (Dependency dep : queryable) {
                queries.put(new JSONObject()
                        .put("version", baseVersion(dep.versionRange()))
                        .put("package", new JSONObject().put("name", dep.name()).put("ecosystem", OSV_ECOSYSTEMS.get(dep.ecosystem()))));
            }
            JSONArray results = post("https://api.osv.dev/v1/querybatch", new JSONObject().put("queries", queries))
                    .getJSONArray("results");

            Map<String, Dependency> hits = new LinkedHashMap<>();
            for (int i = 0; i < results.length(); i++) {
                JSONArray vulns = results.getJSONObject(i).optJSONArray("vulns");
                for (int j = 0; vulns != null && j < vulns.length(); j++) {
                    hits.putIfAbsent(vulns.getJSONObject(j).getString("id"), queryable.get(i));
                }
            }

            List<Advisory> advisories = new ArrayList<>();
            for (Map.Entry<String, Dependency> hit : hits.entrySet()) {
                Dependency dep = hit.getValue();
                String summary = "";
                String fixedIn = null;
                if (advisories.size() < MAX_DETAILS) {
                    JSONObject vuln = get("https://api.osv.dev/v1/vulns/" + hit.getKey());
                    summary = vuln.optString("summary", vuln.optString("details", "").lines().findFirst().orElse(""));
                    fixedIn = fixedVersion(vuln, dep.name(), baseVersion(dep.versionRange()));
                }
                advisories.add(new Advisory(dep.name(), baseVersion(dep.versionRange()), hit.getKey(), summary, fixedIn,
                        "https://osv.dev/vulnerability/" + hit.getKey()));
            }
            return advisories;
        } catch (Exception e) {
            log.warn("OSV lookup failed: {}", e.getMessage());
            return List.of();
        }
    }

    // Advisories list a fix per release branch; report the first fix above the version in use
    static String fixedVersion(JSONObject vuln, String packageName, String version) {
        String best = null;
        JSONArray affected = vuln.optJSONArray("affected");
        for (int i = 0; affected != null && i < affected.length(); i++) {
            JSONObject entry = affected.getJSONObject(i);
            if (!packageName.equals(entry.optJSONObject("package", new JSONObject()).optString("name"))) {
                continue;
            }
            JSONArray ranges = entry.optJSONArray("ranges");
            for (int r = 0; ranges != null && r < ranges.length(); r++) {
                JSONArray events = ranges.getJSONObject(r).optJSONArray("events");
                for (int e = 0; events != null && e < events.length(); e++) {
                    String fixed = events.getJSONObject(e).optString("fixed", null);
                    if (fixed != null && compareVersions(fixed, version) > 0 && (best == null || compareVersions(fixed, best) < 0)) {
                        best = fixed;
                    }
                }
            }
        }
        return best;
    }

    static int compareVersions(String left, String right) {
        String[] a = baseVersion(left) == null ? new String[0] : baseVersion(left).split("\\.");
        String[] b = baseVersion(right) == null ? new String[0] : baseVersion(right).split("\\.");
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            int diff = Long.compare(i < a.length ? Long.parseLong(a[i]) : 0, i < b.length ? Long.parseLong(b[i]) : 0);
            if (diff != 0) {
                return diff;
            }
        }
        return 0;
    }

    private JSONObject post(String url, JSONObject body) throws Exception {
        return send(HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString())));
    }

    private JSONObject get(String url) throws Exception {
        return send(HttpRequest.newBuilder(URI.create(url)).GET());
    }

    private JSONObject send(HttpRequest.Builder request) throws Exception {
        HttpResponse<String> response = http.send(request.timeout(Duration.ofSeconds(10)).build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("OSV returned " + response.statusCode());
        }
        return new JSONObject(response.body());
    }
}
