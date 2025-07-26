package com.barrcon.patchy.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class PatchNotes extends AbstractEntity {
//    @NotNull
//    @Column(nullable = false)

    private String version;
//    @NotNull
//    @Column(nullable = false)

    private String title;
//    @NotNull
//    @Column(nullable = false)

    @Column(name ="description", columnDefinition = "TEXT")
    private String description;
//    @NotNull
//    @Column(nullable = false)

    private String originalUrl;
//    @NotNull
//    @Column(nullable = false)

    private LocalDateTime releaseDate;

    @ManyToOne
    @JoinColumn(name = "tech_id")
    private Tech tech;

    public PatchNotes(String description, String version, String title) {
        this.description = description;
        this.version = version;
        this.title = title;
    }
    public PatchNotes(String version, String title, String description, String originalUrl, LocalDateTime releaseDate, Tech tech) {
        this.version = version;
        this.title = title;
        this.description = description;
        this.originalUrl = originalUrl;
        this.releaseDate = releaseDate;
        this.tech = tech;
    }
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public LocalDateTime getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDateTime releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Tech getTech() {
        return tech;
    }

    public void setTech(Tech tech) {
        this.tech = tech;
    }


}
