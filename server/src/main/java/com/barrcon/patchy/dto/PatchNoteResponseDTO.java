package com.barrcon.patchy.dto;

import java.time.LocalDateTime;
import java.util.List;
//DTO for patch notes returned from Claude API
public class PatchNoteResponseDTO {
    private Long id;
    private String techName;
    private String content;
    private String releaseVersion;
    private String headline;
    // critical | high | normal | low; null until summarized
    private String urgency;
    private List<String> categories;
    private List<PatchNoteSectionDTO> sections;
    private String sourceUrl;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;


    public PatchNoteResponseDTO() {}


    public PatchNoteResponseDTO(Long id, String techName, String content,
                                String releaseVersion, String headline, String urgency, List<String> categories,
                                List<PatchNoteSectionDTO> sections, String sourceUrl,
                                LocalDateTime createdAt, LocalDateTime lastUpdated) {
        this.id = id;
        this.techName = techName;
        this.content = content;
        this.releaseVersion = releaseVersion;
        this.headline = headline;
        this.urgency = urgency;
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
    public String getReleaseVersion() { return releaseVersion; }
    public void setReleaseVersion(String releaseVersion) { this.releaseVersion = releaseVersion; }
    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }
    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }
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
