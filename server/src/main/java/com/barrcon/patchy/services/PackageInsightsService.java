package com.barrcon.patchy.services;

import com.barrcon.patchy.dto.PackageInsightMatchDTO;
import com.barrcon.patchy.dto.PackageInsightsRequestDTO;
import com.barrcon.patchy.models.PatchNote;
import com.barrcon.patchy.models.Tech;
import com.barrcon.patchy.repositories.PatchNoteRepository;
import com.barrcon.patchy.repositories.TechRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class PackageInsightsService {

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
            Map.entry("java", "Java")
    );

    public PackageInsightsService(TechRepository techRepository,
                                  PatchNoteRepository patchNoteRepository,
                                  PatchNoteService patchNoteService) {
        this.techRepository = techRepository;
        this.patchNoteRepository = patchNoteRepository;
        this.patchNoteService = patchNoteService;
    }

    public List<PackageInsightMatchDTO> generateMatches(PackageInsightsRequestDTO request) {
        List<PackageInsightMatchDTO> matches = new ArrayList<>();

        if (request == null) {
            return matches;
        }

        collectMatches(matches, request.getDependencies(), "dependencies");
        collectMatches(matches, request.getDevDependencies(), "devDependencies");
        collectMatches(matches, request.getPeerDependencies(), "peerDependencies");

        return matches;
    }

    private void collectMatches(List<PackageInsightMatchDTO> matches,
                                Map<String, String> dependencies,
                                String dependencyType) {
        if (dependencies == null || dependencies.isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> entry : dependencies.entrySet()) {
            Optional<Tech> techOpt = resolveTech(entry.getKey());
            if (techOpt.isEmpty()) {
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
            if (normalized.contains(normalize(tech.getName()))) {
                return Optional.of(tech);
            }
        }

        return Optional.empty();
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
