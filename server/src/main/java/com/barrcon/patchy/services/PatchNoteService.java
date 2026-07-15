package com.barrcon.patchy.services;

import com.barrcon.patchy.dto.PatchNoteResponseDTO;
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

        PatchNote patchNote = existingNoteOpt.orElseGet(() -> new PatchNote(tech, sourceUrl));

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

        SummaryService.SummaryResult summaryResult = summaryService.generateSummary(newContent);
        List<String> detectedCategories = summaryResult.sections().isEmpty()
                ? patchNoteCategoryService.detectCategories(newContent)
                : summaryResult.sections().stream()
                .map(section -> section.getCategory())
                .distinct()
                .toList();

        patchNote.setContent(summaryResult.summary());
        patchNote.setOriginalContent(newContent);
        patchNote.setReleaseVersion(releaseVersion);
        patchNote.setCategories(patchNoteCategoryService.serializeCategories(detectedCategories));
        patchNote.setSummarySections(patchNoteSectionService.serialize(summaryResult.sections()));
        patchNote.setSourceUrl(sourceUrl);
        patchNote.setLastUpdated(LocalDateTime.now());

        PatchNote saved = patchNoteRepository.save(patchNote);

        return new ProcessResult(saved, true);
    }

    public PatchNoteResponseDTO toResponseDTO(PatchNote patchNote) {
        return new PatchNoteResponseDTO(
                patchNote.getId(),
                patchNote.getTech().getName(),
                patchNote.getContent(),
                patchNote.getOriginalContent(),
                patchNote.getReleaseVersion(),
                resolveCategories(patchNote),
                patchNoteSectionService.parse(patchNote.getSummarySections()),
                patchNote.getSourceUrl(),
                patchNote.getCreatedAt(),
                patchNote.getLastUpdated()
        );
    }

    // Older rows have no stored categories, so fall back to keyword detection
    private List<String> resolveCategories(PatchNote patchNote) {
        List<String> storedCategories = patchNoteCategoryService.parseStoredCategories(patchNote.getCategories());
        if (!storedCategories.isEmpty()) {
            return storedCategories;
        }

        return patchNoteCategoryService.detectCategories(patchNote.getOriginalContent());
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public record ProcessResult(PatchNote patchNote, boolean updated) {
    }
}
