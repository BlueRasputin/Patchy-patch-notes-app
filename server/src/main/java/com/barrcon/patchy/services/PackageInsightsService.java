package com.barrcon.patchy.services;

import com.barrcon.patchy.dto.PackageInsightMatchDTO;
import com.barrcon.patchy.dto.PackageInsightsRequestDTO;
import com.barrcon.patchy.dto.PackageInsightsResponseDTO;
import com.barrcon.patchy.models.Tech;
import com.barrcon.patchy.repositories.PatchNoteRepository;
import com.barrcon.patchy.repositories.TechRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.barrcon.patchy.dto.TechCatalogEntryDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class PackageInsightsService {

    private static final Logger log = LoggerFactory.getLogger(PackageInsightsService.class);

    private final TechRepository techRepository;
    private final PatchNoteRepository patchNoteRepository;
    private final PatchNoteService patchNoteService;
    private final TechDiscoveryService techDiscoveryService;
    private final AdvisoryService advisoryService;
    // "ecosystem:name" -> catalog tech name, from each catalog entry's packages
    private final Map<String, String> catalogAliases = new HashMap<>();


    public PackageInsightsService(TechRepository techRepository,
                                  PatchNoteRepository patchNoteRepository,
                                  PatchNoteService patchNoteService,
                                  TechDiscoveryService techDiscoveryService,
                                  AdvisoryService advisoryService,
                                  TechCatalogService techCatalogService) {
        this.techRepository = techRepository;
        this.patchNoteRepository = patchNoteRepository;
        this.patchNoteService = patchNoteService;
        this.techDiscoveryService = techDiscoveryService;
        this.advisoryService = advisoryService;
        for (TechCatalogEntryDTO entry : techCatalogService.loadCatalog()) {
            if (entry.getPackages() != null) {
                entry.getPackages().forEach(alias -> catalogAliases.put(alias.toLowerCase(Locale.ROOT), entry.getName()));
            }
        }
    }

    public PackageInsightsResponseDTO generateInsights(PackageInsightsRequestDTO request) {
        Map<String, Map<String, String>> sections = new LinkedHashMap<>();
        if (request != null) {
            putIfPresent(sections, "dependencies", request.getDependencies());
            putIfPresent(sections, "devDependencies", request.getDevDependencies());
            putIfPresent(sections, "peerDependencies", request.getPeerDependencies());
        }
        return buildInsights(sections.entrySet().stream()
                .map(entry -> new Section(entry.getKey(), "npm", List.of(), entry.getValue()))
                .toList());
    }

    // files: relative path -> manifest content (package.json, pom.xml, go.mod, ...)
    public PackageInsightsResponseDTO generateProjectInsights(Map<String, String> files) {
        List<Section> sections = new ArrayList<>();
        files.forEach((path, content) -> {
            try {
                ManifestParser.Parsed parsed = ManifestParser.parse(path, content);
                if (parsed != null) {
                    sections.add(new Section(path, parsed.ecosystem(), parsed.languages(), parsed.dependencies()));
                }
            } catch (Exception e) {
                log.info("Skipping unparseable manifest {}: {}", path, e.getMessage());
            }
        });
        return buildInsights(sections);
    }

    private record Section(String source, String ecosystem, List<String> languages, Map<String, String> dependencies) {
    }

    private PackageInsightsResponseDTO buildInsights(List<Section> sections) {
        List<Tech> techs = new ArrayList<>();
        techRepository.findAll().forEach(techs::add);

        List<PackageInsightMatchDTO> matches = new ArrayList<>();
        List<String> unmatched = new ArrayList<>();
        Set<String> matchedTechNames = new LinkedHashSet<>();
        List<TechDiscoveryService.PackageRef> toDiscover = new ArrayList<>();
        List<AdvisoryService.Dependency> dependencies = new ArrayList<>();

        for (Section section : sections) {
            section.dependencies().forEach((name, version) ->
                    dependencies.add(new AdvisoryService.Dependency(section.ecosystem(), name, version)));

            Map<String, String> entries = new LinkedHashMap<>();
            section.languages().forEach(language -> entries.put(language, ""));
            entries.putAll(section.dependencies());

            entries.forEach((packageName, version) -> {
                boolean isLanguage = section.languages().contains(packageName);
                Optional<Tech> techOpt = resolveTech(packageName, isLanguage ? "language" : section.ecosystem(), techs);
                if (techOpt.isEmpty()) {
                    unmatched.add(packageName);
                    TechDiscoveryService.PackageRef ref = new TechDiscoveryService.PackageRef(section.ecosystem(), packageName);
                    if (isLanguage) {
                        ref = new TechDiscoveryService.PackageRef("language", packageName);
                    }
                    if (techDiscoveryService.canDiscover(ref)) {
                        toDiscover.add(ref);
                    }
                    return;
                }
                // One entry per tech: react + react-dom shouldn't list React twice
                if (!matchedTechNames.add(techOpt.get().getName())) {
                    return;
                }
                patchNoteRepository.findFirstByTechOrderByCreatedAtDesc(techOpt.get())
                        .ifPresent(note -> matches.add(new PackageInsightMatchDTO(
                                packageName, version, section.source(), patchNoteService.toResponseDTO(note))));
            });
        }

        // Telemetry for improving the package-to-tech mapping over time
        if (!unmatched.isEmpty()) {
            log.info("Unmatched packages from insights: {}", unmatched);
        }
        if (!toDiscover.isEmpty()) {
            techDiscoveryService.discover(toDiscover);
        }

        return new PackageInsightsResponseDTO(matches, unmatched, List.copyOf(matchedTechNames),
                toDiscover.stream().map(TechDiscoveryService.PackageRef::name).toList(),
                advisoryService.check(dependencies));
    }

    private static void putIfPresent(Map<String, Map<String, String>> sections, String key, Map<String, String> value) {
        if (value != null && !value.isEmpty()) {
            sections.put(key, value);
        }
    }

    // Exact matches only: catalog aliases ("npm:axios", "language:python"), then techs
    // discovered earlier. Anything else is unknown and goes to discovery, which is
    // better than guessing by name ("vitest" is not Vite, "preact" is not React).
    private Optional<Tech> resolveTech(String packageName, String ecosystem, List<Tech> techs) {
        if (packageName == null || packageName.isBlank()) {
            return Optional.empty();
        }
        String techName = catalogAliases.get((ecosystem + ":" + packageName).toLowerCase(Locale.ROOT));
        if (techName != null) {
            return techs.stream().filter(tech -> tech.getName().equalsIgnoreCase(techName)).findFirst();
        }
        return techs.stream()
                .filter(tech -> packageName.equalsIgnoreCase(tech.getPackageName()) && ecosystem.equals(tech.getEcosystem()))
                .findFirst();
    }
}
