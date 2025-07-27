package com.barrcon.patchy.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "techs")
public class Tech extends AbstractEntity {

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String version;

    private String description;

    @OneToMany(mappedBy = "tech", cascade = CascadeType.ALL)
    private List<PatchNotes> patchNotes = new ArrayList<>();



    public Tech() {
    }

    public Tech(String name, String version, List<PatchNotes> patchNotes) {
        this.name = name;
        this.version = version;
        this.patchNotes = patchNotes;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public List<PatchNotes> getPatchNotes() {
        return patchNotes;
    }
    public void setPatchNotes(List<PatchNotes> patchNotes) {
        this.patchNotes = patchNotes;
    }

}
