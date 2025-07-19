package com.barrcon.patchy.models;

import jakarta.persistence.Entity;

import java.util.Set;


@Entity
public class User extends AbstractEntity {
    private String username;
    private String email;
    private Set<Tech> followedTechs;
    private Set<Favorites> favorites;
    private Set<Feed> feed;

    public User() {
        // Default constructor
    }

    public User(String username, String email, Set<Tech> followedTechs, Set<Favorites> favorites, Set<Feed> feed) {
        this.username = username;
        this.email = email;
        this.followedTechs = followedTechs;
        this.favorites = favorites;
        this.feed = feed;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
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
    public Set<Favorites> getFavorites() {
        return favorites;
    }
    public void setFavorites(Set<Favorites> favorites) {
        this.favorites = favorites;
    }
    public Set<Feed> getFleet() {
        return feed;
    }
    public void setFleet(Set<Feed> feed) {
        this.feed = feed;
    }

}
