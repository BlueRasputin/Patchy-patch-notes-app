package com.barrcon.patchy.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

// Personal access token for editor plugins, the MCP server, and other
// non-browser clients. Only the SHA-256 hash is stored.
@Entity
@Table(name = "api_tokens")
public class ApiToken extends AbstractEntity {

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @JsonIgnore
    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime lastUsedAt;

    public ApiToken() {}

    public ApiToken(User user, String tokenHash, String name) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
}
