package com.barrcon.patchy.dto;

import java.util.List;

public class ProcessedPatchNotesDTO {

    private String version;
    private String title;
    private String description;

    public ProcessedPatchNotesDTO() {}

    public ProcessedPatchNotesDTO(String version, String title, String description) {
        this.version = version;
        this.title = title;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
