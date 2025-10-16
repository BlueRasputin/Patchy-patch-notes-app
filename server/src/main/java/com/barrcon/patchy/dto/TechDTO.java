package com.barrcon.patchy.dto;
import java.util.Set;
//converts tech ids to a set, so that each field is unique
public class TechDTO {
    private Set<Long> techIds;

    public TechDTO() {
    }

    public Set<Long> getTechIds() {
        return techIds;
    }

    public void setTechIds(Set<Long> techIds) {
        this.techIds = techIds;
    }
}