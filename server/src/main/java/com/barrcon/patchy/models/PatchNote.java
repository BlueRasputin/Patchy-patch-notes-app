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
    private String content;

    @Column(nullable = false)
    private String sourceUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }
}
