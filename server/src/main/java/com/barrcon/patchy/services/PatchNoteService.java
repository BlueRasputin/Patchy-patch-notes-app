package com.barrcon.patchy.services;

import com.barrcon.patchy.models.PatchNote;
import com.barrcon.patchy.models.Tech;
import com.barrcon.patchy.repositories.PatchNoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class PatchNoteService {

    @Autowired
    private PatchNoteRepository patchNoteRepository;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private PatchNoteCategoryService patchNoteCategoryService;

    @Autowired
    private PatchNoteSectionService patchNoteSectionService;

    public ProcessResult processAndSave(Tech tech, String newContent, String sourceUrl, String releaseVersion) {
        Optional<PatchNote> existingNoteOpt = patchNoteRepository.findFirstByTechOrderByCreatedAtDesc(tech);


        PatchNote patchNote;
        if (existingNoteOpt.isPresent()) {
            patchNote = existingNoteOpt.get();
        } else {
            patchNote = new PatchNote(tech, sourceUrl);
        }


        String normalizedIncomingContent = normalize(newContent);
        String normalizedExistingContent = normalize(patchNote.getOriginalContent());
        boolean contentChanged = existingNoteOpt.isEmpty()
                || !Objects.equals(normalizedExistingContent, normalizedIncomingContent);

        String existingVersion = normalize(patchNote.getReleaseVersion());
        String incomingVersion = normalize(releaseVersion);
        boolean versionChanged = !incomingVersion.isEmpty()
                && !Objects.equals(existingVersion, incomingVersion);

        if (!contentChanged && !versionChanged) {
            return new ProcessResult(patchNote, false);
        }

        //sends new content to summary service
        SummaryService.SummaryResult summaryResult = summaryService.generateSummary(newContent);
        List<String> detectedCategories = summaryResult.sections().isEmpty()
                ? patchNoteCategoryService.detectCategories(newContent)
                : summaryResult.sections().stream()
                .map(section -> section.getCategory())
                .distinct()
                .toList();

        //returns summarized content to repository and formats for dataset
        patchNote.setContent(summaryResult.summary());
        patchNote.setOriginalContent(newContent);
        patchNote.setReleaseVersion(releaseVersion);
        patchNote.setCategories(patchNoteCategoryService.serializeCategories(detectedCategories));
        patchNote.setSummarySections(patchNoteSectionService.serialize(summaryResult.sections()));
        patchNote.setSourceUrl(sourceUrl);
        //Sets current time as last updated to track when notes are created/modified
        patchNote.setLastUpdated(LocalDateTime.now());

        PatchNote saved = patchNoteRepository.save(patchNote);

        return new ProcessResult(saved, true);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public record ProcessResult(PatchNote patchNote, boolean updated) {
    }
}
