package com.barrcon.patchy.models;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class PatchNotes extends AbstractEntity {

    private String version;
    private String title;
    private String content;
    private String originalUrl;
    private LocalDateTime releaseDate;

    @ManyToOne
    @JoinColumn(name = "tech_id")
    private Tech tech;

    public PatchNotes(String content, String version, String title) {
        // Default constructor
    }
    public PatchNotes(String version, String title, String content, String originalUrl, LocalDateTime releaseDate, Tech tech) {
        this.version = version;
        this.title = title;
        this.content = content;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
