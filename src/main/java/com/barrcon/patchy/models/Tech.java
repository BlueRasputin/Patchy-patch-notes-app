package com.barrcon.patchy.models;


import jakarta.persistence.Entity;

import java.util.List;

@Entity
public class Tech extends AbstractEntity {

    private String name;
    private String category;
    private String officialUrl;
    private List<PatchNotes> patchNotes;

    public Tech() {
        // Default constructor
    }
    public Tech(String name, String category, String officialUrl, List<PatchNotes> patchNotes) {
        this.name = name;
        this.category = category;
        this.officialUrl = officialUrl;
        this.patchNotes = patchNotes;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public String getOfficialUrl() {
        return officialUrl;
    }
    public void setOfficialUrl(String officialUrl) {
        this.officialUrl = officialUrl;
    }
    public List<PatchNotes> getPatchNotes() {
        return patchNotes;
    }
    public void setPatchNotes(List<PatchNotes> patchNotes) {
        this.patchNotes = patchNotes;
    }

}
