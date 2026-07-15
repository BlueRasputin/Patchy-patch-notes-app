package com.barrcon.patchy.controllers;

import com.barrcon.patchy.dto.CrawledNoteDTO;
import com.barrcon.patchy.dto.PatchNoteResponseDTO;
import com.barrcon.patchy.models.Tech;
import com.barrcon.patchy.repositories.PatchNoteRepository;
import com.barrcon.patchy.repositories.TechRepository;
import com.barrcon.patchy.repositories.UserRepository;
import com.barrcon.patchy.services.PatchNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class PatchNoteController {

    @Autowired
    private PatchNoteRepository patchNoteRepository;

    @Autowired
    private TechRepository techRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatchNoteService patchNoteService;

    // Receives crawled patch note data from the crawler service
    @PostMapping("/api/process-crawled-notes")
    public ResponseEntity<String> processCrawledNotes(@RequestBody List<CrawledNoteDTO> crawledData) {
        int updatedCount = 0;
        int skippedCount = 0;

        try {
            for (CrawledNoteDTO item : crawledData) {
                Optional<Tech> techOptional = techRepository.findByName(item.getTechName());
                if (techOptional.isEmpty()) {
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
        List<PatchNoteResponseDTO> responseDTOs = patchNoteRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(patchNoteService::toResponseDTO)
                .toList();

        return ResponseEntity.ok(responseDTOs);
    }

    // Latest patch note for each of the user's favorite techs
    @GetMapping("/api/users/{userId}/patch-notes")
    public ResponseEntity<List<PatchNoteResponseDTO>> getUserPatchNotes(@PathVariable Long userId) {
        try {
            Optional<com.barrcon.patchy.models.User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Set<Tech> favoriteTechs = userOpt.get().getFavoriteTechs();

            List<PatchNoteResponseDTO> userPatchNotes = favoriteTechs.stream()
                    .map(tech -> patchNoteRepository.findFirstByTechOrderByCreatedAtDesc(tech))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(patchNoteService::toResponseDTO)
                    .toList();

            return ResponseEntity.ok(userPatchNotes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
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
