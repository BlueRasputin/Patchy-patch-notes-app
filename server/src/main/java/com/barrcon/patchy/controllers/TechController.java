package com.barrcon.patchy.controllers;


import com.barrcon.patchy.dto.TechCatalogEntryDTO;
import com.barrcon.patchy.models.Tech;
import com.barrcon.patchy.repositories.TechRepository;
import com.barrcon.patchy.services.TechCatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tech")
@CrossOrigin(origins = "http://localhost:5173")
public class TechController {

    @Autowired
    private TechRepository techRepository;

    @Autowired
    private TechCatalogService techCatalogService;

    //get full list of tech
    @GetMapping
    public ResponseEntity<List<Tech>> getAllTech() {
        return ResponseEntity.ok((List<Tech>) techRepository.findAll());
    }

    // Tech names + patch note URLs, used by the browser extension to
    // recognize which sites belong to tracked techs
    @GetMapping("/catalog")
    public ResponseEntity<List<TechCatalogEntryDTO>> getCatalog() {
        return ResponseEntity.ok(techCatalogService.loadCatalog());
    }


    // What the daily Playwright crawler should scrape: catalog techs without
    // GitHub releases, plus discovered techs whose locator was found by AI
    @GetMapping("/crawl-targets")
    public ResponseEntity<List<TechCatalogEntryDTO>> getCrawlTargets() {
        List<TechCatalogEntryDTO> targets = new ArrayList<>(techCatalogService.loadCatalog().stream()
                // Only techs with no other source; scraping a GitHub-release tech too would
                // create duplicate notes from two sources and burn summary quota
                .filter(entry -> entry.getPatchNotesUrl() != null && entry.getContentSelector() != null
                        && entry.getGithubRepo() == null && entry.getChangelogUrl() == null && entry.getEndoflife() == null)
                .toList());
        for (Tech tech : techRepository.findByCrawlSelectorIsNotNull()) {
            TechCatalogEntryDTO target = new TechCatalogEntryDTO();
            target.setName(tech.getName());
            target.setPatchNotesUrl(tech.getCrawlUrl());
            target.setContentSelector(tech.getCrawlSelector());
            target.setContentStrategy("default");
            targets.add(target);
        }
        return ResponseEntity.ok(targets);
    }

    //get tech by ID
    @GetMapping("/{id}")
    public ResponseEntity<Tech> getTechById(@PathVariable Long id) {
         Optional<Tech> tech = techRepository.findById(id);
         return tech.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


}
