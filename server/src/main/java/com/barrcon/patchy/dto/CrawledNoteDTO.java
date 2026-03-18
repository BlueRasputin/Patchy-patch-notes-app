package com.barrcon.patchy.dto;
//DTO for patchnotes returned from web crawling
public class CrawledNoteDTO {
    private String techName;
    private String content;
    private String url;
    private String releaseVersion;

    public CrawledNoteDTO() {}

    public CrawledNoteDTO(String techName, String content, String url, String releaseVersion) {
        this.techName = techName;
        this.content = content;
        this.url = url;
        this.releaseVersion = releaseVersion;
    }

    public String getTechName() {
        return techName;
    }

    public void setTechName(String techName) {
        this.techName = techName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }
}
