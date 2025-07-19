package com.barrcon.patchy.models;

import jakarta.persistence.Entity;

import java.time.LocalDateTime;


@Entity
public class Feed extends AbstractEntity{
    private User user;
    private PatchNotes patchNote;
    private LocalDateTime addedtoFleetAt;
    private boolean isRead;


    public Feed() {
        // Default constructor
    }
    public Feed(User user, PatchNotes patchNote, LocalDateTime addedtoFleetAt, boolean isRead) {
        this.user = user;
        this.patchNote = patchNote;
        this.addedtoFleetAt = addedtoFleetAt;
        this.isRead = isRead;

    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public PatchNotes getPatchNote() {
        return patchNote;
    }
    public void setPatchNote(PatchNotes patchNote) {
        this.patchNote = patchNote;
    }
    public LocalDateTime getAddedtoFleetAt() {
        return addedtoFleetAt;
    }
    public void setAddedtoFleetAt(LocalDateTime addedtoFleetAt) {
        this.addedtoFleetAt = addedtoFleetAt;
    }
    public boolean isRead() {
        return isRead;
    }
    public void setRead(boolean read) {
        isRead = read;
    }

}
