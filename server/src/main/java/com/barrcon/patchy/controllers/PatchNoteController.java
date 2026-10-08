package com.barrcon.patchy.controllers;

import com.barrcon.patchy.dto.CrawledNoteDTO;
import com.barrcon.patchy.dto.PatchNoteResponseDTO;
import com.barrcon.patchy.models.Tech;
import com.barrcon.patchy.repositories.PatchNoteRepository;
import com.barrcon.patchy.repositories.TechRepository;
import com.barrcon.patchy.services.PatchNoteService;
import com.barrcon.patchy.services.CurrentUserService;
import com.barrcon.patchy.services.TechCatalogService;
import com.barrcon.patchy.services.TechDiscoveryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class PatchNoteController {

    @Autowired
    private PatchNoteRepository patchNoteRepository;

    @Autowired
    private TechRepository techRepository;

    @Autowired
    private PatchNoteService patchNoteService;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private TechCatalogService techCatalogService;

    @Autowired
    private TechDiscoveryService techDiscoveryService;

    @Value("${CRAWLER_API_KEY:}")
    private String crawlerApiKey;

    // Receives crawled patch note data from the crawler service. Each new note
    // costs a Claude call and is shown to every user, so only the crawler may post.
    @PostMapping("/api/process-crawled-notes")
    public ResponseEntity<String> processCrawledNotes(@RequestHeader(value = "X-Crawler-Key", required = false) String key,
                                                      @RequestBody List<CrawledNoteDTO> crawledData) {
        if (crawlerApiKey.isBlank() || !MessageDigest.isEqual(
                crawlerApiKey.getBytes(StandardCharsets.UTF_8),
                String.valueOf(key).getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or wrong X-Crawler-Key (set CRAWLER_API_KEY)");
        }
        int updatedCount = 0;
        int skippedCount = 0;

        try {
            for (CrawledNoteDTO item : crawledData) {
                Optional<Tech> techOptional = techRepository.findByName(item.getTechName());
                if (techOptional.isEmpty()) {
                    skippedCount++;
                    continue;
                }
                // The crawler reports an empty extraction when a selector stops matching
                // (usually a site redesign); AI-found locators get rediscovered
                if (item.getContent() == null || item.getContent().isBlank()) {
                    techDiscoveryService.relocate(techOptional.get());
                    skippedCount++;
                    continue;
                }

                PatchNoteService.ProcessResult result = patchNoteService.processAndSave(
                        techOptional.get(), item.getContent(), item.getUrl(), item.getReleaseVersion());

                if (result.updated()) {
                    updatedCount++;
                } else {
                    skippedCount++;
                }
            }

            String message = String.format("Processing complete! Updated: %d, Skipped: %d",
                    updatedCount, skippedCount);
            return ResponseEntity.ok(message);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Processing failed: " + e.getMessage());
        }
    }

    @GetMapping("/api/patch-notes")
    public ResponseEntity<List<PatchNoteResponseDTO>> getAllPatchNotes() {
        List<PatchNoteResponseDTO> responseDTOs = patchNoteRepository.findLatestPerTech().stream()
                .map(patchNoteService::toResponseDTO)
                .toList();

        return ResponseEntity.ok(responseDTOs);
    }

    // Full release history for one tech, newest first
    @GetMapping("/api/patch-notes/history")
    public ResponseEntity<List<PatchNoteResponseDTO>> getPatchNoteHistory(@RequestParam Long techId) {
        Optional<Tech> techOpt = techRepository.findById(techId);
        if (techOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<PatchNoteResponseDTO> history = patchNoteRepository.findByTechOrderByCreatedAtDesc(techOpt.get()).stream()
                .map(patchNoteService::toResponseDTO)
                .toList();

        return ResponseEntity.ok(history);
    }

    // Browser extension "Summarize this page": signed-in users only, and the page
    // must be on the tech's own patch-notes host so notes can't be pointed elsewhere.
    // ponytail: the page text is still client-supplied; fetch it server-side if abuse shows up.
    @PostMapping("/api/patch-notes/submit")
    public ResponseEntity<String> submitPage(HttpServletRequest request, @RequestBody CrawledNoteDTO page) {
        if (currentUserService.resolve(request).isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sign in to Patchy to summarize pages");
        }
        Optional<String> catalogHost = techCatalogService.loadCatalog().stream()
                .filter(entry -> entry.getName().equalsIgnoreCase(String.valueOf(page.getTechName())))
                .map(entry -> host(entry.getPatchNotesUrl()))
                .findFirst();
        Optional<Tech> tech = techRepository.findByName(String.valueOf(page.getTechName()));
        if (catalogHost.isEmpty() || tech.isEmpty() || !catalogHost.get().equals(host(page.getUrl()))
                || page.getContent() == null || page.getContent().length() > 100_000) {
            return ResponseEntity.badRequest().body("Page isn't on a tracked tech's patch notes site");
        }
        boolean updated = patchNoteService.processAndSave(tech.get(), page.getContent(), page.getUrl(), null).updated();
        return ResponseEntity.ok(updated ? "Summarized" : "Unchanged");
    }

    private static String host(String url) {
        try {
            String host = URI.create(url).getHost();
            return host == null ? "" : host.replaceFirst("^www\\.", "");
        } catch (Exception e) {
            return "";
        }
    }

    @GetMapping("/api/patch-notes/compare")
    public ResponseEntity<List<PatchNoteResponseDTO>> comparePatchNotes(
            @RequestParam List<Long> techIds) {

        if (techIds == null || techIds.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<PatchNoteResponseDTO> comparedNotes = techIds.stream()
                .map(techRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(tech -> patchNoteRepository.findFirstByTechOrderByCreatedAtDesc(tech))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(patchNoteService::toResponseDTO)
                .toList();

        return ResponseEntity.ok(comparedNotes);
    }
}
