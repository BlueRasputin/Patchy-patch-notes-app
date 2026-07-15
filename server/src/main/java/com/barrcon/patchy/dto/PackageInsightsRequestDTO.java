package com.barrcon.patchy.dto;

import java.util.Map;

public class PackageInsightsRequestDTO {

    private Map<String, String> dependencies;
    private Map<String, String> devDependencies;
    private Map<String, String> peerDependencies;

    public Map<String, String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Map<String, String> dependencies) {
        this.dependencies = dependencies;
    }

    public Map<String, String> getDevDependencies() {
        return devDependencies;
    }

    public void setDevDependencies(Map<String, String> devDependencies) {
        this.devDependencies = devDependencies;
    }

    public Map<String, String> getPeerDependencies() {
        return peerDependencies;
    }

    public void setPeerDependencies(Map<String, String> peerDependencies) {
        this.peerDependencies = peerDependencies;
    }
}
