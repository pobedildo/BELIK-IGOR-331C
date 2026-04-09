package com.example.myapplication.data;

public class NoteImage {
    private final long id;
    private final long noteId;
    private final String imagePath;

    public NoteImage(long id, long noteId, String imagePath) {
        this.id = id;
        this.noteId = noteId;
        this.imagePath = imagePath;
    }

    public long getId() {
        return id;
    }

    public long getNoteId() {
        return noteId;
    }

    public String getImagePath() {
        return imagePath;
    }
}
