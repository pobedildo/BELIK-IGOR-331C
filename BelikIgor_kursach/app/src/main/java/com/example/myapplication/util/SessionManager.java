package com.example.myapplication.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREFS = "organizer_session";
    private static final String KEY_USER_ID = "user_id";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void setUserId(long id) {
        prefs.edit().putLong(KEY_USER_ID, id).apply();
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1L);
    }

    public boolean isLoggedIn() {
        return getUserId() >= 0;
    }

    public void clear() {
        prefs.edit().remove(KEY_USER_ID).apply();
    }
}
