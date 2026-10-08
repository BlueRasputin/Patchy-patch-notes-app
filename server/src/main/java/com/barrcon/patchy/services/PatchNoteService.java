package com.barrcon.patchy.services;

import com.barrcon.patchy.dto.PatchNoteResponseDTO;
import com.barrcon.patchy.models.PatchNote;
import com.barrcon.patchy.models.Tech;
import com.barrcon.patchy.repositories.PatchNoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

// Stores a new note whenever a source's content or version changes. The raw
// release text is shown immediately and queued for SummaryService, which
// replaces it with a categorized summary (Gemini free tier) shortly after.
@Service
public class PatchNoteService {

    // Some projects publish 100k+ char notes (Kotlin); keep the head, link the rest
    static final int MAX_NOTE_CHARS = 8_000;

    @Autowired
    private PatchNoteRepository patchNoteRepository;

    @Autowired
    private PatchNoteCategoryService patchNoteCategoryService;

    @Autowired
    private PatchNoteSectionService patchNoteSectionService;

    public ProcessResult processAndSave(Tech tech, String newContent, String sourceUrl, String releaseVersion) {
        Optional<PatchNote> latestNoteOpt = patchNoteRepository.findFirstByTechOrderByCreatedAtDesc(tech);

        String incomingHash = sha256(normalize(newContent));
        boolean contentChanged = latestNoteOpt.isEmpty() || !Objects.equals(storedHash(latestNoteOpt.get()), incomingHash);

        String existingVersion = latestNoteOpt.map(note -> normalize(note.getReleaseVersion())).orElse("");
        String incomingVersion = normalize(releaseVersion);
        boolean versionChanged = !incomingVersion.isEmpty()
                && !Objects.equals(existingVersion, incomingVersion);

        if (!contentChanged && !versionChanged) {
            return new ProcessResult(latestNoteOpt.get(), false);
        }

        // Append a new note so per-tech release history is preserved
        PatchNote patchNote = new PatchNote(tech, sourceUrl);

        List<String> detectedCategories = patchNoteCategoryService.detectCategories(newContent);

        patchNote.setContent(excerpt(newContent));
        patchNote.setOriginalContent(excerpt(newContent));
        patchNote.setPendingSummary(true);
        patchNote.setContentHash(incomingHash);
        patchNote.setReleaseVersion(releaseVersion);
        patchNote.setCategories(patchNoteCategoryService.serializeCategories(detectedCategories));
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
                patchNote.getReleaseVersion(),
                patchNote.getHeadline(),
                patchNote.getUrgency(),
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

    static String excerpt(String content) {
        String trimmed = normalize(content);
        if (trimmed.length() <= MAX_NOTE_CHARS) {
            return trimmed;
        }
        int cut = trimmed.lastIndexOf('\n', MAX_NOTE_CHARS);
        return trimmed.substring(0, cut > MAX_NOTE_CHARS / 2 ? cut : MAX_NOTE_CHARS).strip()
                + "\n\n…*Truncated. See the full release notes via the source link.*";
    }

    private String storedHash(PatchNote note) {
        return note.getContentHash() != null ? note.getContentHash() : sha256(normalize(note.getOriginalContent()));
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public record ProcessResult(PatchNote patchNote, boolean updated) {
    }
}
