package com.example.myapplication.data;

public class TagCount {
    private final Tag tag;
    private final int taskCount;

    public TagCount(Tag tag, int taskCount) {
        this.tag = tag;
        this.taskCount = taskCount;
    }

    public Tag getTag() {
        return tag;
    }

    public int getTaskCount() {
        return taskCount;
    }
}
