package com.barrcon.patchy.dto;
//DTO for patchnotes returned from web crawling
public class CrawledNoteDTO {
    private String techName;
    private String content;
    private String url;

    public CrawledNoteDTO() {}

    public CrawledNoteDTO(String techName, String content, String url) {
        this.techName = techName;
        this.content = content;
        this.url = url;
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
}

