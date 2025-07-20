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

    @Column(unique = true, nullable = false)
    private String email;

    @ManyToMany
    @JoinTable(
        name = "user_followed_techs",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "tech_id")
    )
    private Set<Tech> followedTechs = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Favorites> favorites = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Feed> feeds = new ArrayList<>();


    public User() {
        // Default constructor
    }

    public User(String username, String email, Set<Tech> followedTechs, List<Favorites> favorites, List<Feed> feeds) {
        this.username = username;
        this.email = email;
        this.followedTechs = followedTechs;
        this.favorites = favorites;
        this.feeds = feeds;
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
    public List<Favorites> getFavorites() {
        return favorites;
    }
    public void setFavorites(List<Favorites> favorites) {
        this.favorites = favorites;
    }
    public List<Feed> getFeeds() {
        return feeds;
    }
    public void setFeeds(List<Feed> feeds) {
        this.feeds = feeds;
    }
}
