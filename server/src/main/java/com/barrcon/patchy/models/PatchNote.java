package com.barrcon.patchy.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "patch_notes")
public class PatchNote extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "tech_id", nullable = false)
    private Tech tech;

    @Column(nullable = false)
    private String version;

    @Column(columnDefinition = "TEXT")
    private String processedContent;

    @Column(nullable = false)
    private String sourceUrl;

    public PatchNote() {
    }

    public PatchNote(Tech tech, String version, String sourceUrl) {
        this.tech = tech;
        this.version = version;
        this.sourceUrl = sourceUrl;
    }

    public Tech getTech() {
        return tech;
    }

    public void setTech(Tech tech) {
        this.tech = tech;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProcessedContent() {
        return processedContent;
    }

    public void setProcessedContent(String processedContent) {
        this.processedContent = processedContent;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }
}
