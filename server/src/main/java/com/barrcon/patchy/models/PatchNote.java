package com.barrcon.patchy.models;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "patch_notes",
    uniqueConstraints = @UniqueConstraint(columnNames = "tech_id"))
public class PatchNote extends AbstractEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tech_id", nullable = false)
    private Tech tech;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "original_content", columnDefinition = "LONGTEXT")
    private String originalContent;

    @Column(name = "release_version")
    private String releaseVersion;

    @Column(name = "categories", columnDefinition = "TEXT")
    private String categories;

    @Column(name = "summary_sections", columnDefinition = "LONGTEXT")
    private String summarySections;

    @Column(nullable = false)
    private String sourceUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public PatchNote() {
    }

    public PatchNote(Tech tech, String sourceUrl) {
        this.tech = tech;
        this.sourceUrl = sourceUrl;
    }

    public Tech getTech() {
        return tech;
    }

    public void setTech(Tech tech) {
        this.tech = tech;
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

    public String getOriginalContent() {
        return originalContent;
    }

    public void setOriginalContent(String originalContent) {
        this.originalContent = originalContent;
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public String getSummarySections() {
        return summarySections;
    }

    public void setSummarySections(String summarySections) {
        this.summarySections = summarySections;
    }
}
