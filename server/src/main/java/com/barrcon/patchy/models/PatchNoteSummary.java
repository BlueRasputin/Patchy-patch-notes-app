package com.barrcon.patchy.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "patch_note_summary")
public class PatchNoteSummary extends AbstractEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "patch_note_id", nullable = false)
    private PatchNote patchNote;

    @Lob
    @Column(name = "tldr_bullets", columnDefinition = "TEXT", nullable = false)
    private String tldrBullets; // e.g. "- X\n- Y\n- Z"

    @Lob
    @Column(name = "highlights", columnDefinition = "TEXT")
    private String highlights;

    @Lob
    @Column(name = "breaking_changes", columnDefinition = "TEXT")
    private String breakingChanges;

    @Column(name = "model", length = 64, nullable = false)
    private String model;

    @Column(name = "prompt_version", length = 32, nullable = false)
    private String promptVersion;

    @Column(name = "hash_of_raw_content", length = 128, nullable = false)
    private String hashOfRawContent;

    @Column(name = "is_current", nullable = false)
    private boolean isCurrent = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // getters/setters...
}
