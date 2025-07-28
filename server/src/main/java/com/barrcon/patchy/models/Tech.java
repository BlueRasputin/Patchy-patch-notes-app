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


    public Tech() {
    }

    public Tech(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
