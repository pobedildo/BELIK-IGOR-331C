package com.example.myapplication.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.myapplication.data.DatabaseHelper;
import com.example.myapplication.data.Task;
import com.example.myapplication.data.User;

public final class ReminderScheduler {

    private ReminderScheduler() {
    }

    private static PendingIntent pending(Context context, long taskId, long userId) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra(ReminderReceiver.EXTRA_TASK_ID, taskId);
        intent.putExtra(ReminderReceiver.EXTRA_USER_ID, userId);
        int req = (int) (taskId ^ (userId << 16));
        return PendingIntent.getBroadcast(context, req, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    public static void cancel(Context context, long taskId, long userId) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        am.cancel(pending(context, taskId, userId));
    }

    public static void schedule(Context context, Task task) {
        User user = DatabaseHelper.getInstance(context).getUser(task.getUserId());
        if (user == null || !user.isNotificationsEnabled()) return;
        if (task.isCompleted()) return;
        int minutes = task.getReminderMinutes();
        if (minutes < 0) return;

        long trigger = task.getDueDateTime() - minutes * 60_000L;
        if (trigger <= System.currentTimeMillis()) return;

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        PendingIntent pi = pending(context, task.getId(), task.getUserId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, trigger, pi);
        }
    }

    public static void rescheduleAllForUser(Context context, long userId) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);
        User user = db.getUser(userId);
        if (user == null || !user.isNotificationsEnabled()) return;
        for (Task t : db.getIncompleteTasksWithReminders(userId)) {
            cancel(context, t.getId(), userId);
            schedule(context, t);
        }
    }

    public static void cancelAllForUser(Context context, long userId) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);
        for (Task t : db.getTasksForUser(userId)) {
            cancel(context, t.getId(), userId);
        }
    }
}
