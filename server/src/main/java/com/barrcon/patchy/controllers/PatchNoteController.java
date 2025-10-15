package com.barrcon.patchy.controllers;

import com.barrcon.patchy.dto.CrawledNoteDTO;
import com.barrcon.patchy.dto.PatchNoteResponseDTO;
import com.barrcon.patchy.models.PatchNote;
import com.barrcon.patchy.models.Tech;
import com.barrcon.patchy.repositories.PatchNoteRepository;
import com.barrcon.patchy.repositories.TechRepository;
import com.barrcon.patchy.repositories.UserRepository;
import com.barrcon.patchy.services.PatchNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
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

    @PostMapping("/api/process-crawled-notes")
    public ResponseEntity<String> processCrawledNotes(@RequestBody List<CrawledNoteDTO> crawledData) {


        int updatedCount = 0;
        int skippedCount = 0;

        try {
            for (CrawledNoteDTO item : crawledData) {
                String techName = item.getTechName();
                String content = item.getContent();
                String url = item.getUrl();

                Optional<Tech> techOptional = techRepository.findByName(techName);
                if (techOptional.isEmpty()) {

                    skippedCount++;
                    continue;
                }

                Tech tech = techOptional.get();
                PatchNote result = patchNoteService.processAndSave(tech, content, url);

                if (result.getLastUpdated() != null) {
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

        List<PatchNote> patchNotes = patchNoteRepository.findAllByOrderByCreatedAtDesc();

        List<PatchNoteResponseDTO> responseDTOs = patchNotes.stream()
                .map(pn -> new PatchNoteResponseDTO(
                        pn.getId(),
                        pn.getTech().getName(),
                        pn.getContent(),
                        pn.getSourceUrl(),
                        pn.getCreatedAt(),
                        pn.getLastUpdated()
                ))
                .toList();

        return ResponseEntity.ok(responseDTOs);
    }


    @GetMapping("/api/users/{userId}/patch-notes")
    public ResponseEntity<List<PatchNoteResponseDTO>> getUserPatchNotes(@PathVariable Long userId) {


        try {

            Optional<com.barrcon.patchy.models.User> userOpt =
                    userRepository.findById(userId);

            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Set<Tech> favoriteTechs = userOpt.get().getFavoriteTechs();

            if (favoriteTechs.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }


            List<PatchNoteResponseDTO> userPatchNotes = favoriteTechs.stream()
                    .map(tech -> patchNoteRepository.findFirstByTechOrderByCreatedAtDesc(tech))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(pn -> new PatchNoteResponseDTO(
                            pn.getId(),
                            pn.getTech().getName(),
                            pn.getContent(),
                            pn.getSourceUrl(),
                            pn.getCreatedAt(),
                            pn.getLastUpdated()
                    ))
                    .toList();

            return ResponseEntity.ok(userPatchNotes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}