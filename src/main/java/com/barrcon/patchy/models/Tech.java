package com.barrcon.patchy.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "techs")
public class Tech extends AbstractEntity {

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(name = "official_url")
    private String officialUrl;

    private String description;

    @OneToMany(mappedBy = "tech", cascade = CascadeType.ALL)
    private List<PatchNotes> patchNotes = new ArrayList<>();

    @ManyToMany(mappedBy = "followedTechs")
    private Set<User> followers = new HashSet<>();

    @OneToMany(mappedBy = "tech", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Favorites> favorites = new ArrayList<>();

    public Tech() {
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
    public List<Favorites> getFavorites() {
        return favorites;
    }
    public void setFavorites(List<Favorites> favorites) {
        this.favorites = favorites;
    }


    public Set<User> getFollowers() {
        return followers;
    }

    public void setFollowers(Set<User> followers) {
        this.followers = followers;
    }
}
