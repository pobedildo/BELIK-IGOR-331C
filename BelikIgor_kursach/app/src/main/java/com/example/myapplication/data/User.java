package com.example.myapplication.data;

public class User {
    private final long id;
    private final String email;
    private final String displayName;
    private final String avatarUri;
    private final int avatarPreset;
    private final boolean notificationsEnabled;
    private final boolean darkTheme;
    private final long createdAt;

    public User(long id, String email, String displayName, String avatarUri, int avatarPreset,
                boolean notificationsEnabled, boolean darkTheme, long createdAt) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.avatarUri = avatarUri;
        this.avatarPreset = avatarPreset;
        this.notificationsEnabled = notificationsEnabled;
        this.darkTheme = darkTheme;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatarUri() {
        return avatarUri;
    }

    public int getAvatarPreset() {
        return avatarPreset;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public boolean isDarkTheme() {
        return darkTheme;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
