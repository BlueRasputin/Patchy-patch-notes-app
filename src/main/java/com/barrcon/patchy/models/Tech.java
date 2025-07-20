package com.barrcon.patchy.models;

import java.util.ArrayList;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import org.hibernate.annotations.Cascade;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Tech extends AbstractEntity {

    private String name;
    private String category;
    private String officialUrl;

    @OneToMany(mappedBy = "technology", cascade = CascadeType.ALL)
    private List<PatchNotes> patchNotes;

    @ManyToMany(mappedBy = "followedTechs")
    private Set<User> followers = new HashSet<>();

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

    public Set<User> getFollowers() {
        return followers;
    }

    public void setFollowers(Set<User> followers) {
        this.followers = followers;
    }
}
