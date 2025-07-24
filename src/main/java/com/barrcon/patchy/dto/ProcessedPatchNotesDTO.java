package com.barrcon.patchy.dto;

import java.util.List;

public class ProcessedPatchNotesDTO {

    private String version;
    private String title;
    private String content;

    public ProcessedPatchNotesDTO() {}

    public ProcessedPatchNotesDTO(String version, String title, String content) {
        this.version = version;
        this.title = title;
        this.content = content;
    }

    // Getters and setters
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
