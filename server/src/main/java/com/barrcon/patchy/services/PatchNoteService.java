package com.barrcon.patchy.services;

import com.barrcon.patchy.models.PatchNote;
import com.barrcon.patchy.models.Tech;
import com.barrcon.patchy.repositories.PatchNoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PatchNoteService {

    private static final Logger log = LoggerFactory.getLogger(PatchNoteService.class);

    @Autowired
    private PatchNoteRepository patchNoteRepository;

    @Autowired
    private SummaryService summaryService;

    public PatchNote processAndSave(Tech tech, String newContent, String sourceUrl) {
        Optional<PatchNote> existingNoteOpt = patchNoteRepository.findFirstByTechOrderByCreatedAtDesc(tech);


        PatchNote patchNote;
        if (existingNoteOpt.isPresent()) {
            patchNote = existingNoteOpt.get();
            log.info("Found existing patch note for tech: {}", tech.getName());
        } else {
            patchNote = new PatchNote(tech, sourceUrl);
            log.info("Creating new patch note for tech: {}", tech.getName());
        }


        boolean contentChanged = existingNoteOpt.isEmpty() ||
                !existingNoteOpt.get().getContent().equals(newContent);

        if (!contentChanged) {
            log.info("No changes detected for {} - skipping", tech.getName());
            return patchNote;
        }

        log.info("New content detected for {} - generating summary", tech.getName());

        String summary = summaryService.generateSummary(newContent);


        patchNote.setContent(summary);
        patchNote.setSourceUrl(sourceUrl);
        patchNote.setLastUpdated(LocalDateTime.now());

        PatchNote saved = patchNoteRepository.save(patchNote);
        log.info("Saved new patch note for {} with ID: {}", tech.getName(), saved.getId());

        return saved;
    }
}


