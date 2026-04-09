package com.example.myapplication.data;

public class Note {
    private final long id;
    private final long userId;
    private final String title;
    private final String content;
    private final long updatedAt;
    private final boolean important;

    public Note(long id, long userId, String title, String content, long updatedAt, boolean important) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.updatedAt = updatedAt;
        this.important = important;
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public boolean isImportant() {
        return important;
    }
}
