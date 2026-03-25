package com.barrcon.patchy.services;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class PatchNoteCategoryService {

    public List<String> detectCategories(String content) {
        if (content == null || content.isBlank()) {
            return List.of();
        }

        String normalized = content.toLowerCase(Locale.ROOT);
        Set<String> categories = new LinkedHashSet<>();

        if (containsAny(normalized, "new feature", "new features", "what's new", "whats new", "\nnew\n", " new ")) {
            categories.add("New Features");
        }
        if (containsAny(normalized, "bug fix", "bug fixes", "fixed ", "fixes ", "issue resolved", "issues resolved")) {
            categories.add("Bug Fixes");
        }
        if (containsAny(normalized, "breaking change", "breaking changes")) {
            categories.add("Breaking Changes");
        }
        if (containsAny(normalized, "security", "vulnerability", "cve-", "cve ")) {
            categories.add("Security");
        }
        if (containsAny(normalized, "deprecated", "deprecation", "sunset", "removed")) {
            categories.add("Deprecations");
        }
        if (containsAny(normalized, "performance", "faster", "startup time", "improved speed", "optimization")) {
            categories.add("Performance");
        }
        if (containsAny(normalized, "known issue", "known issues")) {
            categories.add("Known Issues");
        }
        if (containsAny(normalized, "documentation", "docs", "reference documentation")) {
            categories.add("Documentation");
        }

        return new ArrayList<>(categories);
    }

    public List<String> parseStoredCategories(String categories) {
        if (categories == null || categories.isBlank()) {
            return List.of();
        }

        return java.util.Arrays.stream(categories.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }

    public String serializeCategories(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return "";
        }

        return String.join(",", categories);
    }

    private boolean containsAny(String content, String... keywords) {
        for (String keyword : keywords) {
            if (content.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
