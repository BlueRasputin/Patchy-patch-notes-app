package com.barrcon.patchy.dto;

public class PatchNotesProcessingRequest {
    private String content;
    private String version;
    private String title;

    public PatchNotesProcessingRequest(String content, String version, String title) {
        this.content = content;
        this.version = version;
        this.title = title;
    }

    // Getters and setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

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