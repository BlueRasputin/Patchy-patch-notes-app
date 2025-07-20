package com.barrcon.patchy.models;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;


@Entity
public class Feed extends AbstractEntity{

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "patch_note_id")
    private PatchNotes patchNote;
    private LocalDateTime addedToFeedAt;
    private boolean isRead;


    public Feed() {
        // Default constructor
    }
    public Feed(User user, PatchNotes patchNote, LocalDateTime addedToFeedAt, boolean isRead) {

        this.user = user;
        this.patchNote = patchNote;
        this.addedToFeedAt = addedToFeedAt;
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
    public LocalDateTime getAddedToFeedAt() {
        return addedToFeedAt;
    }
    public void setAddedToFeedAt(LocalDateTime addedToFeedAt) {
        this.addedToFeedAt = addedToFeedAt;
    }
    public boolean isRead() {
        return isRead;
    }
    public void setRead(boolean read) {
        isRead = read;
    }

}
