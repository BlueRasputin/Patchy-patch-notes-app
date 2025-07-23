package com.barrcon.patchy.DTO;

import java.util.List;

public class ProcessedPatchNotesDTO {

    private String version;
    private String title;
    private List<String> bulletPoints;

    public ProcessedPatchNotesDTO() {}

    public ProcessedPatchNotesDTO(String version, String title, List<String> bulletPoints) {
        this.version = version;
        this.title = title;
        this.bulletPoints = bulletPoints;
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

    public List<String> getBulletPoints() {
        return bulletPoints;
    }

    public void setBulletPoints(List<String> bulletPoints) { this.bulletPoints = bulletPoints; }
}
