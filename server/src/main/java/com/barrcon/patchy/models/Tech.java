package com.barrcon.patchy.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

//instantiate tech table
@Entity
@Table(name = "techs")
public class Tech extends AbstractEntity {

    @Column(unique = true, nullable = false)
    private String name;

    // Set only for techs discovered from a project manifest (catalog techs are crawled)
    private String ecosystem;
    private String packageName;
    private String sourceRepo;
    // Page + CSS selector the Playwright crawler reads, found by AI for sites
    // without GitHub releases; crawlUrl alone = raw changelog file fetched directly
    private String crawlUrl;
    private String crawlSelector;
    private LocalDateTime lastCheckedAt;

    public Tech() {
    }

    public Tech(String name) {
        this.name = name;
    }

    public Tech(String ecosystem, String packageName, String sourceRepo) {
        this.name = packageName;
        this.ecosystem = ecosystem;
        this.packageName = packageName;
        this.sourceRepo = sourceRepo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEcosystem() {
        return ecosystem;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getSourceRepo() {
        return sourceRepo;
    }

    public void setPackage(String ecosystem, String packageName) {
        this.ecosystem = ecosystem;
        this.packageName = packageName;
    }

    public void setSourceRepo(String sourceRepo) {
        this.sourceRepo = sourceRepo;
    }

    public String getCrawlUrl() {
        return crawlUrl;
    }

    public String getCrawlSelector() {
        return crawlSelector;
    }

    public void setCrawlSource(String crawlUrl, String crawlSelector) {
        this.crawlUrl = crawlUrl;
        this.crawlSelector = crawlSelector;
    }

    public LocalDateTime getLastCheckedAt() {
        return lastCheckedAt;
    }

    public void setLastCheckedAt(LocalDateTime lastCheckedAt) {
        this.lastCheckedAt = lastCheckedAt;
    }
}
