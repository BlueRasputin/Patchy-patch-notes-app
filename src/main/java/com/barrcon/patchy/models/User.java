
package com.barrcon.patchy.models;

import jakarta.persistence.*;
import java.util.HashSet;
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
        name = "user_favorite_techs",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "tech_id")
    )
    private Set<Tech> favoriteTechs = new HashSet<>();

    public User() {}

    public User(String username, String passwordHash, String email) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
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

    public Set<Tech> getFavoriteTechs() {
        return favoriteTechs;
    }

    public void setFavoriteTechs(Set<Tech> favoriteTechs) {
        this.favoriteTechs = favoriteTechs;
    }

    public void addFavoriteTech(Tech tech) {
        favoriteTechs.add(tech);
    }

    public void removeFavoriteTech(Tech tech) {
        favoriteTechs.remove(tech);
    }

    public boolean isFollowingTech(Tech tech) {
        return favoriteTechs.contains(tech);
    }
}
