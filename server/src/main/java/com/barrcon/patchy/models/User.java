
package com.barrcon.patchy.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static java.util.Base64.getEncoder;

@Entity
@Table(name = "users")
public class User extends AbstractEntity {

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

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

    public User(String username, String password, String email) {
        this.username = username;
        setPassword(password);
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public static BCryptPasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder();
    }

    public boolean isFollowingTech(Tech tech) {
        return favoriteTechs.contains(tech);
    }

    public boolean isMatchingPassword(String password) {
        return getEncoder().matches(password, this.password);
    }
}
