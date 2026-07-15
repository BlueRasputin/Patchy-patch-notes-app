package com.barrcon.patchy.services;

import com.barrcon.patchy.dto.PackageInsightMatchDTO;
import com.barrcon.patchy.dto.PackageInsightsRequestDTO;
import com.barrcon.patchy.dto.PackageInsightsResponseDTO;
import com.barrcon.patchy.models.PatchNote;
import com.barrcon.patchy.models.Tech;
import com.barrcon.patchy.repositories.PatchNoteRepository;
import com.barrcon.patchy.repositories.TechRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class PackageInsightsService {

    private static final Logger log = LoggerFactory.getLogger(PackageInsightsService.class);

    private final TechRepository techRepository;
    private final PatchNoteRepository patchNoteRepository;
    private final PatchNoteService patchNoteService;

    // Maps well-known package names to their tracked tech; anything not here
    // falls back to a substring match against tech names.
    private static final Map<String, String> PACKAGE_TO_TECH = Map.ofEntries(
            Map.entry("react", "React"),
            Map.entry("react-dom", "React"),
            Map.entry("next", "Next.js"),
            Map.entry("spring-boot", "Spring"),
            Map.entry("spring-framework", "Spring"),
            Map.entry("rails", "Ruby on Rails"),
            Map.entry("node", "Node.js"),
            Map.entry("nodejs", "Node.js"),
            Map.entry("python", "Python"),
            Map.entry("django", "Python"),
            Map.entry("flask", "Python"),
            Map.entry("java", "Java"),
            Map.entry("golang", "Go"),
            Map.entry("go", "Go"),
            Map.entry("php", "PHP"),
            Map.entry("dotnet", ".NET"),
            Map.entry("bun", "Bun"),
            Map.entry("pg", "PostgreSQL"),
            Map.entry("postgres", "PostgreSQL"),
            Map.entry("ioredis", "Redis")
    );

    // Tech names shorter than this only match via the explicit map above;
    // substring matching on names like "Go" would hit packages like "mongodb".
    private static final int MIN_SUBSTRING_MATCH_LENGTH = 4;

    public PackageInsightsService(TechRepository techRepository,
                                  PatchNoteRepository patchNoteRepository,
                                  PatchNoteService patchNoteService) {
        this.techRepository = techRepository;
        this.patchNoteRepository = patchNoteRepository;
        this.patchNoteService = patchNoteService;
    }

    public PackageInsightsResponseDTO generateInsights(PackageInsightsRequestDTO request) {
        List<PackageInsightMatchDTO> matches = new ArrayList<>();
        List<String> unmatched = new ArrayList<>();

        if (request != null) {
            collectMatches(matches, unmatched, request.getDependencies(), "dependencies");
            collectMatches(matches, unmatched, request.getDevDependencies(), "devDependencies");
            collectMatches(matches, unmatched, request.getPeerDependencies(), "peerDependencies");
        }

        // Telemetry for improving the package-to-tech mapping over time
        if (!unmatched.isEmpty()) {
            log.info("Unmatched packages from package.json insights: {}", unmatched);
        }

        return new PackageInsightsResponseDTO(matches, unmatched);
    }

    private void collectMatches(List<PackageInsightMatchDTO> matches,
                                List<String> unmatched,
                                Map<String, String> dependencies,
                                String dependencyType) {
        if (dependencies == null || dependencies.isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> entry : dependencies.entrySet()) {
            Optional<Tech> techOpt = resolveTech(entry.getKey());
            if (techOpt.isEmpty()) {
                unmatched.add(entry.getKey());
                continue;
            }

            Optional<PatchNote> patchNoteOpt = patchNoteRepository.findFirstByTechOrderByCreatedAtDesc(techOpt.get());
            if (patchNoteOpt.isEmpty()) {
                continue;
            }

            matches.add(new PackageInsightMatchDTO(
                    entry.getKey(),
                    entry.getValue(),
                    dependencyType,
                    patchNoteService.toResponseDTO(patchNoteOpt.get())
            ));
        }
    }

    private Optional<Tech> resolveTech(String packageName) {
        if (packageName == null || packageName.isBlank()) {
            return Optional.empty();
        }

        String normalized = normalize(packageName);
        String techName = PACKAGE_TO_TECH.get(normalized);

        if (techName != null) {
            return techRepository.findByNameIgnoreCase(techName);
        }

        for (Tech tech : techRepository.findAll()) {
            String normalizedTechName = normalize(tech.getName());
            if (normalizedTechName.length() >= MIN_SUBSTRING_MATCH_LENGTH
                    && normalized.contains(normalizedTechName)) {
                return Optional.of(tech);
            }
        }

        return Optional.empty();
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
