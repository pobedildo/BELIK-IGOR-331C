package com.example.myapplication.data;

public class Task {
    private final long id;
    private final long userId;
    private final String title;
    private final String description;
    private final long dueDateTime;
    private final int reminderMinutes;
    private final boolean completed;
    private final boolean important;

    public Task(long id, long userId, String title, String description,
                long dueDateTime, int reminderMinutes, boolean completed, boolean important) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.dueDateTime = dueDateTime;
        this.reminderMinutes = reminderMinutes;
        this.completed = completed;
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

    public String getDescription() {
        return description;
    }

    public long getDueDateTime() {
        return dueDateTime;
    }

    public int getReminderMinutes() {
        return reminderMinutes;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isImportant() {
        return important;
    }
}
