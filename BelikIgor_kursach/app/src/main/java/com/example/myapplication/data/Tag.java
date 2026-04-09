package com.example.myapplication.data;

public class Tag {
    private final long id;
    private final String name;
    private final String color;

    public Tag(long id, String name, String color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }
}
