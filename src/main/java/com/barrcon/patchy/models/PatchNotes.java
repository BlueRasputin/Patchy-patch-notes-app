package com.barrcon.patchy.models;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class PatchNotes extends AbstractEntity {

    private String version;
    private String title;
    private String Summary;
    private String originalUrl;
    private LocalDateTime releaseDate;

    @ManyToOne
    @JoinColumn(name = "technology_id")
    private Tech technology;

    public PatchNotes() {
        // Default constructor
    }
    public PatchNotes(String version, String title, String summary, String originalUrl, LocalDateTime releaseDate, Tech technology) {
        this.version = version;
        this.title = title;
        this.Summary = summary;
        this.originalUrl = originalUrl;
        this.releaseDate = releaseDate;
        this.technology = technology;
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

    public String getSummary() {
        return Summary;
    }

    public void setSummary(String summary) {
        this.Summary = summary;
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

    public Tech getTechnology() {
        return technology;
    }

    public void setTechnology(Tech technology) {
        this.technology = technology;
    }


}
