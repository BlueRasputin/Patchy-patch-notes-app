package com.barrcon.patchy.dto;

import java.util.List;

// One tech in tech-catalog.json. Release notes come from exactly one of:
// githubRepo (GitHub releases), changelogUrl (raw changelog file), endoflife
// (endoflife.date product id), or patchNotesUrl + contentSelector (Playwright crawl).
public class TechCatalogEntryDTO {

    private String name;
    private String patchNotesUrl;
    private String contentSelector;
    private String contentStrategy;
    // owner/repo when the tech publishes GitHub releases; those skip the crawler
    private String githubRepo;
    private String changelogUrl;
    private String endoflife;
    // Exact manifest names that map to this tech, as "ecosystem:name" (npm:axios,
    // maven:jackson-databind, language:elixir). Entries without any are matched by name.
    private List<String> packages;
    private Integer rank;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPatchNotesUrl() {
        return patchNotesUrl;
    }

    public void setPatchNotesUrl(String patchNotesUrl) {
        this.patchNotesUrl = patchNotesUrl;
    }

    public String getContentSelector() {
        return contentSelector;
    }

    public void setContentSelector(String contentSelector) {
        this.contentSelector = contentSelector;
    }

    public String getChangelogUrl() {
        return changelogUrl;
    }

    public void setChangelogUrl(String changelogUrl) {
        this.changelogUrl = changelogUrl;
    }

    public String getEndoflife() {
        return endoflife;
    }

    public void setEndoflife(String endoflife) {
        this.endoflife = endoflife;
    }

    public List<String> getPackages() {
        return packages;
    }

    public void setPackages(List<String> packages) {
        this.packages = packages;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getGithubRepo() {
        return githubRepo;
    }

    public void setGithubRepo(String githubRepo) {
        this.githubRepo = githubRepo;
    }

    public String getContentStrategy() {
        return contentStrategy;
    }

    public void setContentStrategy(String contentStrategy) {
        this.contentStrategy = contentStrategy;
    }
}
