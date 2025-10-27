package com.barrcon.patchy.services;

import com.barrcon.patchy.models.PatchNote;
import com.barrcon.patchy.models.Tech;
import com.barrcon.patchy.repositories.PatchNoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PatchNoteService {



    @Autowired
    private PatchNoteRepository patchNoteRepository;

    @Autowired
    private SummaryService summaryService;

    public PatchNote processAndSave(Tech tech, String newContent, String sourceUrl) {
        Optional<PatchNote> existingNoteOpt = patchNoteRepository.findFirstByTechOrderByCreatedAtDesc(tech);


        PatchNote patchNote;
        if (existingNoteOpt.isPresent()) {
            patchNote = existingNoteOpt.get();
        } else {
            patchNote = new PatchNote(tech, sourceUrl);
        }


        boolean contentChanged = existingNoteOpt.isEmpty() ||
                !existingNoteOpt.get().getContent().equals(newContent);

        if (!contentChanged) {
            return patchNote;
        }

        //sends new content to summary service
        String summary = summaryService.generateSummary(newContent);

        //returns summarized content to repository and formats for dataset
        patchNote.setContent(summary);
        patchNote.setSourceUrl(sourceUrl);
        //Sets current time as last updated to track when notes are created/modified
        patchNote.setLastUpdated(LocalDateTime.now());

        PatchNote saved = patchNoteRepository.save(patchNote);

        return saved;
    }
}


