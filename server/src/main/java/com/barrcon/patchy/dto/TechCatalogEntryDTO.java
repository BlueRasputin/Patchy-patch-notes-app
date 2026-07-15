package com.barrcon.patchy.dto;

public class TechCatalogEntryDTO {

    private String name;
    private String patchNotesUrl;
    private String contentSelector;
    private String contentStrategy;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPatchNotesUrl() {
        return patchNotesUrl;
    }

    public void setPatchNotesUrl(String patchNotesUrl) {
        this.patchNotesUrl = patchNotesUrl;
    }

    public String getContentSelector() {
        return contentSelector;
    }

    public void setContentSelector(String contentSelector) {
        this.contentSelector = contentSelector;
    }

    public String getContentStrategy() {
        return contentStrategy;
    }

    public void setContentStrategy(String contentStrategy) {
        this.contentStrategy = contentStrategy;
    }
}
