package com.barrcon.patchy.dto;

import java.time.LocalDateTime;
import java.util.List;
//DTO for patch notes returned from Claude API
public class PatchNoteResponseDTO {
    private Long id;
    private String techName;
    private String content;
    private String originalContent;
    private String releaseVersion;
    private List<String> categories;
    private List<PatchNoteSectionDTO> sections;
    private String sourceUrl;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;


    public PatchNoteResponseDTO() {}


    public PatchNoteResponseDTO(Long id, String techName, String content, String originalContent,
                                String releaseVersion, List<String> categories,
                                List<PatchNoteSectionDTO> sections, String sourceUrl,
                                LocalDateTime createdAt, LocalDateTime lastUpdated) {
        this.id = id;
        this.techName = techName;
        this.content = content;
        this.originalContent = originalContent;
        this.releaseVersion = releaseVersion;
        this.categories = categories;
        this.sections = sections;
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
    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }
    public List<PatchNoteSectionDTO> getSections() { return sections; }
    public void setSections(List<PatchNoteSectionDTO> sections) { this.sections = sections; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
