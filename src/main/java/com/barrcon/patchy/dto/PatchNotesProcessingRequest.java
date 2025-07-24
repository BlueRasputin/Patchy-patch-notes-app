package com.barrcon.patchy.dto;

public class PatchNotesProcessingRequest {

    private String version;
    private String title;

    public PatchNotesProcessingRequest( String version, String title) {

        this.version = version;
        this.title = title;
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

}