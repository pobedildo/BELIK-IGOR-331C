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
    private final Long parentTaskId;
    private final String repeatRule;

    public Task(long id, long userId, String title, String description,
                long dueDateTime, int reminderMinutes, boolean completed, boolean important,
                Long parentTaskId, String repeatRule) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.dueDateTime = dueDateTime;
        this.reminderMinutes = reminderMinutes;
        this.completed = completed;
        this.important = important;
        this.parentTaskId = parentTaskId;
        this.repeatRule = repeatRule;
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

    public Long getParentTaskId() {
        return parentTaskId;
    }

    public String getRepeatRule() {
        return repeatRule;
    }
}
