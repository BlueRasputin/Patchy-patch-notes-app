package com.barrcon.patchy.config;

import com.barrcon.patchy.dto.TechCatalogEntryDTO;
import com.barrcon.patchy.models.Tech;
import com.barrcon.patchy.repositories.TechRepository;
import com.barrcon.patchy.services.TechCatalogService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class TechCatalogSyncConfig {

    // Inserts missing catalog techs and keeps their release-notes source in sync.
    // Techs dropped from the catalog stop being polled (no daily fetch, no summary
    // quota) but keep their notes and followers; discovered techs are left alone.
    @Bean
    CommandLineRunner syncTechCatalog(TechCatalogService techCatalogService, TechRepository techRepository) {
        return args -> {
            List<TechCatalogEntryDTO> catalog = techCatalogService.loadCatalog();
            Set<String> catalogNames = catalog.stream()
                    .map(entry -> entry.getName().trim().toLowerCase(Locale.ROOT))
                    .collect(Collectors.toSet());
            for (Tech tech : techRepository.findAll()) {
                boolean discovered = tech.getPackageName() != null && !"endoflife".equals(tech.getEcosystem());
                boolean polled = tech.getSourceRepo() != null || tech.getCrawlUrl() != null || "endoflife".equals(tech.getEcosystem());
                if (!discovered && polled && !catalogNames.contains(tech.getName().toLowerCase(Locale.ROOT))) {
                    tech.setSourceRepo(null);
                    tech.setCrawlSource(null, null);
                    tech.setPackage(null, null);
                    techRepository.save(tech);
                }
            }
            syncEntries(catalog, techRepository);
        };
    }

    private static void syncEntries(List<TechCatalogEntryDTO> catalog, TechRepository techRepository) {
        catalog.stream()
                .filter(entry -> entry.getName() != null && !entry.getName().isBlank())
                .forEach(entry -> {
                    String name = entry.getName().trim();
                    Tech tech = techRepository.findByNameIgnoreCase(name).orElseGet(() -> new Tech(name));
                    if (tech.getId() == null || !matches(tech, entry)) {
                        tech.setSourceRepo(entry.getGithubRepo());
                        tech.setCrawlSource(entry.getChangelogUrl(), null);
                        if (entry.getEndoflife() != null) {
                            tech.setPackage("endoflife", entry.getEndoflife());
                        }
                        techRepository.save(tech);
                    }
                });
    }

    private static boolean matches(Tech tech, TechCatalogEntryDTO entry) {
        return Objects.equals(tech.getSourceRepo(), entry.getGithubRepo())
                && Objects.equals(tech.getCrawlUrl(), entry.getChangelogUrl())
                && (entry.getEndoflife() == null || entry.getEndoflife().equals(tech.getPackageName()));
    }
}
