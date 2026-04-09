package com.example.myapplication.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.example.myapplication.R;

public final class NotificationHelper {
    public static final String CHANNEL_TASKS = "organizer_tasks";

    private NotificationHelper() {
    }

    public static void ensureChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm == null) return;
        NotificationChannel ch = new NotificationChannel(
                CHANNEL_TASKS,
                context.getString(R.string.notification_channel_tasks),
                NotificationManager.IMPORTANCE_DEFAULT);
        ch.setDescription(context.getString(R.string.notification_task_reminder));
        nm.createNotificationChannel(ch);
    }
}
