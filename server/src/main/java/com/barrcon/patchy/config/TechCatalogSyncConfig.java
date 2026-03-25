package com.barrcon.patchy.config;

import com.barrcon.patchy.models.Tech;
import com.barrcon.patchy.repositories.TechRepository;
import com.barrcon.patchy.services.TechCatalogService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TechCatalogSyncConfig {

    @Bean
    CommandLineRunner syncTechCatalog(TechCatalogService techCatalogService, TechRepository techRepository) {
        return args -> techCatalogService.loadCatalog().stream()
                .map(entry -> entry.getName() == null ? null : entry.getName().trim())
                .filter(name -> name != null && !name.isEmpty())
                .filter(name -> techRepository.findByNameIgnoreCase(name).isEmpty())
                .forEach(name -> techRepository.save(new Tech(name)));
    }
}
