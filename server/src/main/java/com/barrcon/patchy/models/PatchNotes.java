package com.barrcon.patchy.models;

import com.barrcon.patchy.models.AbstractEntity;
import jakarta.persistence.*;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "PatchNotes")
public class PatchNotes extends AbstractEntity {

    @Column( unique = true, nullable = false)
    private String techName;

    @Column (unique = true)
    private String condensedPatchNote;



    @ManyToOne
    @JoinTable(
            name = "tech_patch_notes",
            joinColumns = @JoinColumn(name = "tech_name_update"),
            inverseJoinColumns = @JoinColumn(name = "tech_id")
    )

    @Column(unique = true)
    private String techNameUpdate;

    @Column
    private String description;





}
