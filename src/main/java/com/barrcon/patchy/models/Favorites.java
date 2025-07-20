package com.barrcon.patchy.models;


import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;


@Entity
public class Favorites extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "patch_note_id")
    private PatchNotes patchNote;
    private boolean isRead;

    public Favorites() {
        // Default constructor
    }

    public Favorites(User user, PatchNotes patchNote, boolean isRead) {
        this.user = user;
        this.patchNote = patchNote;
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

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
