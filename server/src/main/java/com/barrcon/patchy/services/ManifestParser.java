package com.barrcon.patchy.services;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Turns project manifest files into package names + the languages they imply.
// Lives server-side so every client (website, editors, MCP) shares one parser.
// ponytail: regex/line parsing, not full TOML/XML/Gradle parsers. Good enough for
// dependency names; swap in real parsers if exotic manifests get misread.
public final class ManifestParser {

    public record Parsed(String ecosystem, List<String> languages, Map<String, String> dependencies) {
    }

    private static final Pattern POM_DEPENDENCY = Pattern.compile(
            "<dependency>.*?<artifactId>\\s*([^<\\s]+)\\s*</artifactId>(?:.*?<version>\\s*([^<\\s]+)\\s*</version>)?.*?</dependency>",
            Pattern.DOTALL);
    private static final Pattern GRADLE_DEPENDENCY = Pattern.compile("['\"]([\\w.\\-]+):([\\w.\\-]+)(?::([\\w.\\-]+))?['\"]");
    private static final Pattern GEM = Pattern.compile("^\\s*gem\\s+['\"]([^'\"]+)['\"](?:\\s*,\\s*['\"]([^'\"]+)['\"])?", Pattern.MULTILINE);
    private static final Pattern GO_REQUIRE = Pattern.compile("^\\s*(?:require\\s+)?([\\w.\\-]+\\.[\\w]+/[^\\s]+)\\s+(v[^\\s]+)", Pattern.MULTILINE);
    private static final Pattern TOML_KEY = Pattern.compile("^\\s*([A-Za-z0-9_.\\-]+)\\s*=\\s*(.*)$");
    private static final Pattern NUGET_REFERENCE = Pattern.compile("<Package(?:Reference|Version)\\s[^>]*?Include=\"([^\"]+)\"(?:[^>]*?Version=\"([^\"]*)\")?");
    private static final Pattern MIX_DEPENDENCY = Pattern.compile("\\{:(\\w+)\\s*(?:,\\s*\"([^\"]*)\")?");
    private static final Pattern SBT_DEPENDENCY = Pattern.compile("\"([\\w.\\-]+)\"\\s*%{1,3}\\s*\"([\\w.\\-]+)\"\\s*%\\s*\"([\\w.\\-]+)\"");
    private static final Pattern YAML_ENTRY = Pattern.compile("^  ([A-Za-z0-9_]+):\\s*['\"]?([^'\"#]*)['\"]?");
    private static final Pattern QUOTED_REQUIREMENT = Pattern.compile("\"([A-Za-z0-9_.\\-]+)\\s*([^\"]*)\"");

    private ManifestParser() {
    }

    // Returns null for files that aren't a recognized manifest
    public static Parsed parse(String path, String content) {
        String file = path.substring(path.replace('\\', '/').lastIndexOf('/') + 1);
        String lower = file.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".csproj") || lower.endsWith(".fsproj") || lower.equals("directory.packages.props")) {
            return new Parsed("nuget", List.of("dotnet"), group(NUGET_REFERENCE, content, 1, 2));
        }
        return switch (lower) {
            case "package.json" -> packageJson(content);
            case "requirements.txt" -> new Parsed("pypi", List.of("python"), requirements(content));
            case "pyproject.toml", "pipfile" -> new Parsed("pypi", List.of("python"), toml(content, true));
            case "cargo.toml" -> new Parsed("crates", List.of("rust"), toml(content, false));
            case "go.mod" -> new Parsed("go", List.of("go"), group(GO_REQUIRE, content, 1, 2));
            case "gemfile" -> new Parsed("rubygems", List.of("ruby"), group(GEM, content, 1, 2));
            case "composer.json" -> composerJson(content);
            case "pom.xml" -> new Parsed("maven", List.of("java"), group(POM_DEPENDENCY, content, 1, 2));
            case "build.gradle", "build.gradle.kts" -> new Parsed("maven",
                    content.contains("kotlin") ? List.of("java", "kotlin") : List.of("java"),
                    group(GRADLE_DEPENDENCY, content, 2, 3));
            case "dockerfile" -> new Parsed("docker", List.of("docker"), Map.of());
            case "mix.exs" -> new Parsed("hex", List.of("elixir"), group(MIX_DEPENDENCY, content, 1, 2));
            case "gleam.toml" -> new Parsed("hex", List.of("gleam"), toml(content, false));
            case "pubspec.yaml" -> new Parsed("pub", List.of("dart"), pubspec(content));
            case "build.sbt" -> new Parsed("maven", List.of("scala"), group(SBT_DEPENDENCY, content, 2, 3));
            // Julia's [deps] values are UUIDs, not versions
            case "project.toml" -> new Parsed("julia", List.of("julia"), blankVersions(toml(content, false)));
            default -> null;
        };
    }

    private static Parsed packageJson(String content) {
        JSONObject json = new JSONObject(content);
        Map<String, String> deps = new LinkedHashMap<>();
        for (String section : List.of("dependencies", "devDependencies", "peerDependencies")) {
            JSONObject entries = json.optJSONObject(section);
            if (entries != null) {
                entries.keySet().forEach(name -> deps.putIfAbsent(name, entries.optString(name)));
            }
        }
        return new Parsed("npm", List.of("node"), deps);
    }

    private static Parsed composerJson(String content) {
        JSONObject json = new JSONObject(content);
        Map<String, String> deps = new LinkedHashMap<>();
        for (String section : List.of("require", "require-dev")) {
            JSONObject entries = json.optJSONObject(section);
            if (entries != null) {
                entries.keySet().stream()
                        .filter(name -> name.contains("/"))
                        .forEach(name -> deps.putIfAbsent(name, entries.optString(name)));
            }
        }
        return new Parsed("packagist", List.of("php"), deps);
    }

    private static Map<String, String> requirements(String content) {
        Map<String, String> deps = new LinkedHashMap<>();
        for (String line : content.split("\\R")) {
            String trimmed = line.replaceAll("#.*", "").trim();
            if (trimmed.isEmpty() || trimmed.startsWith("-")) {
                continue;
            }
            String name = trimmed.split("[\\s<>=!~;\\[]", 2)[0];
            deps.putIfAbsent(name, trimmed.substring(name.length()).trim());
        }
        return deps;
    }

    // Keys under [*dependencies*] / [packages] tables, plus PEP 621 `dependencies = ["x>=1"]` arrays
    private static Map<String, String> toml(String content, boolean pep621) {
        Map<String, String> deps = new LinkedHashMap<>();
        boolean inDependencyTable = false;
        boolean inArray = false;
        for (String line : content.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("[")) {
                String table = trimmed.toLowerCase(Locale.ROOT);
                inDependencyTable = table.contains("dependencies") || table.equals("[packages]")
                        || table.equals("[dev-packages]") || table.equals("[deps]");
                inArray = false;
                continue;
            }
            if (pep621 && (inArray || trimmed.matches("(optional-)?dependencies\\s*=\\s*\\[.*"))) {
                Matcher matcher = QUOTED_REQUIREMENT.matcher(trimmed);
                while (matcher.find()) {
                    deps.putIfAbsent(matcher.group(1), matcher.group(2).trim());
                }
                inArray = !trimmed.contains("]");
                continue;
            }
            Matcher key = TOML_KEY.matcher(trimmed);
            if (inDependencyTable && key.matches() && !key.group(1).equalsIgnoreCase("python")) {
                deps.putIfAbsent(key.group(1), key.group(2).replace("\"", "").trim());
            }
        }
        return deps;
    }

    // Two-space-indented keys under the dependencies/dev_dependencies blocks
    private static Map<String, String> pubspec(String content) {
        Map<String, String> deps = new LinkedHashMap<>();
        boolean inDependencies = false;
        for (String line : content.split("\\R")) {
            if (!line.startsWith(" ") && !line.isBlank()) {
                inDependencies = line.matches("(dev_)?dependencies:\\s*(#.*)?");
                continue;
            }
            Matcher entry = YAML_ENTRY.matcher(line);
            if (inDependencies && entry.find()) {
                deps.putIfAbsent(entry.group(1), entry.group(2).trim());
            }
        }
        return deps;
    }

    private static Map<String, String> blankVersions(Map<String, String> deps) {
        deps.replaceAll((name, version) -> "");
        return deps;
    }

    private static Map<String, String> group(Pattern pattern, String content, int nameGroup, int versionGroup) {
        Map<String, String> deps = new LinkedHashMap<>();
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String version = matcher.group(versionGroup);
            deps.putIfAbsent(matcher.group(nameGroup), version == null ? "" : version);
        }
        return deps;
    }

}
