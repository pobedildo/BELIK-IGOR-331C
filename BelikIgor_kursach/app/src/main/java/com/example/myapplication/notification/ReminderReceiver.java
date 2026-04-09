package com.example.myapplication.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.data.DatabaseHelper;
import com.example.myapplication.data.Task;
import com.example.myapplication.data.User;

public class ReminderReceiver extends BroadcastReceiver {

    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_USER_ID = "user_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        long taskId = intent.getLongExtra(EXTRA_TASK_ID, -1);
        long userId = intent.getLongExtra(EXTRA_USER_ID, -1);
        if (taskId < 0 || userId < 0) return;

        DatabaseHelper db = DatabaseHelper.getInstance(context);
        User user = db.getUser(userId);
        if (user == null || !user.isNotificationsEnabled()) return;

        Task task = db.getTask(taskId, userId);
        if (task == null || task.isCompleted()) return;

        NotificationHelper.ensureChannels(context);

        Intent open = new Intent(context, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, (int) taskId, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_TASKS)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(context.getString(R.string.notification_task_reminder))
                .setContentText(task.getTitle())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pi)
                .setAutoCancel(true);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify((int) taskId, b.build());
        }
    }
}
