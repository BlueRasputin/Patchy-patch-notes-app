package com.barrcon.patchy.models;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;


//@Entity
//public class Feed extends AbstractEntity{
//
//    @ManyToOne
//    @JoinColumn(name = "user_id")
//    private User user;
//
//    @ManyToOne
//    @JoinColumn(name = "patch_note_id")
//    private PatchNotes patchNote;
//
//    @ManyToOne
//    @JoinColumn(name = "tech_id")
//    private Tech tech;
//
//
//
//    public Feed() {
//    }
//    public Feed(User user, PatchNotes patchNote) {
//
//        this.user = user;
//        this.patchNote = patchNote;
//        this.tech = patchNote.getTech();
//
//
//    }
//    public User getUser() {
//        return user;
//    }
//    public void setUser(User user) {
//        this.user = user;
//    }
//    public PatchNotes getPatchNote() {
//        return patchNote;
//    }
//    public void setPatchNote(PatchNotes patchNote) {
//        this.patchNote = patchNote;
//    }
//
//
//}
