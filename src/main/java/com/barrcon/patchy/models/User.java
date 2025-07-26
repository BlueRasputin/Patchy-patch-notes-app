package com.barrcon.patchy.models;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "users")
public class User extends AbstractEntity {

   @Column(unique = true, nullable = false)
    private String username;

   @Column(nullable = false)
    private String passwordHash;

    @Column(unique = true, nullable = false)
    private String email;

    @ManyToMany
    @JoinTable(
        name = "user_followed_techs",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "tech_id")
    )
    private Set<Tech> followedTechs = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "user_tech_ids", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "tech_id")
    private List<Long> favoriteTechIds = new ArrayList<>();


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Feed> feeds = new ArrayList<>();


    public User() {
    }

    public User(String username, String passwordHash, String email, List<Feed> feeds) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.feeds = feeds;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public Set<Tech> getFollowedTechs() {
        return followedTechs;
    }
    public void setFollowedTechs(Set<Tech> followedTechs) {
        this.followedTechs = followedTechs;
    }
    public List<Feed> getFeeds() {
        return feeds;
    }
    public void setFeeds(List<Feed> feeds) {
        this.feeds = feeds;
    }

    public List<Long> getFavoriteTechIds() { return favoriteTechIds; }
    public void setFavoriteTechIds(List<Long> favoriteTechIds) { this.favoriteTechIds = favoriteTechIds; }

    public void addFavoriteTech(Long techId) {
        if (!favoriteTechIds.contains(techId)) {
            favoriteTechIds.add(techId);
        }
    }

    public void removeFavoriteTech(Long techId) {
        favoriteTechIds.remove(techId);
    }

    public boolean hasFavoriteTech(Long techId) {
        return favoriteTechIds.contains(techId);
    }
}
