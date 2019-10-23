package com.practice.todo.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;

import com.practice.todo.App;
import com.practice.todo.MainActivity;
import com.practice.todo.R;
import com.practice.todo.storage.database.entity.TodoItem;

/**
 * 提醒工具类
 *
 * 这个app中的提醒是一个通知加上震动，适配Android8.0需要加上Channel。
 */
public class RemindUtil {

    private static final String NOTIFICATION_CHANNEL_ID = "10051";
    private static final String default_notification_channel_id = "todo_practice2";

    public static void remind(TodoItem todoItem) {
        Intent ntfIntent = new Intent(App.instance, MainActivity.class);
        ntfIntent.putExtra("fromNotification", true);
        ntfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(App.instance, 0, ntfIntent, 0);
        NotificationManager nm = (NotificationManager) App.instance.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(App.instance, default_notification_channel_id);

        final String contentText = TextUtils.isEmpty(todoItem.getDescription()) ?
                "You can mark this todo is done" :
                todoItem.getDescription();

        builder.setContentTitle(todoItem.getTitle());
        builder.setContentIntent(pendingIntent);
        builder.setContentText(contentText);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setAutoCancel(true);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "NOTIFICATION_CHANNEL_NAME", importance);

            channel.enableVibration(true);
            channel.canBypassDnd();
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.setBypassDnd(true);
            channel.shouldShowLights();

            builder.setChannelId(NOTIFICATION_CHANNEL_ID);
            nm.createNotificationChannel(channel);
        }
        nm.notify((int) System.currentTimeMillis(), builder.build());
    }
}
