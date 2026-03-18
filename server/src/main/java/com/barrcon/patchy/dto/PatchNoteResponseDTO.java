package com.barrcon.patchy.dto;

import java.time.LocalDateTime;
//DTO for patch notes returned from Claude API
public class PatchNoteResponseDTO {
    private Long id;
    private String techName;
    private String content;
    private String originalContent;
    private String releaseVersion;
    private String sourceUrl;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;


    public PatchNoteResponseDTO() {}


    public PatchNoteResponseDTO(Long id, String techName, String content, String originalContent,
                                String releaseVersion, String sourceUrl,
                                LocalDateTime createdAt, LocalDateTime lastUpdated) {
        this.id = id;
        this.techName = techName;
        this.content = content;
        this.originalContent = originalContent;
        this.releaseVersion = releaseVersion;
        this.sourceUrl = sourceUrl;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTechName() { return techName; }
    public void setTechName(String techName) { this.techName = techName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getOriginalContent() { return originalContent; }
    public void setOriginalContent(String originalContent) { this.originalContent = originalContent; }
    public String getReleaseVersion() { return releaseVersion; }
    public void setReleaseVersion(String releaseVersion) { this.releaseVersion = releaseVersion; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
