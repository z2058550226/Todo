package com.practice.todo.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.practice.todo.MainActivity;
import com.practice.todo.R;
import com.practice.todo.storage.database.entity.TodoItem;
import com.practice.todo.util.FormatUtil;
import com.practice.todo.util.InMemoryCache;
import com.practice.todo.util.RemindUtil;

public class RemindService extends Service {
    private static final String NOTIFICATION_CHANNEL_ID = "10007";
    private static final int FOREGROUND_ID = 41441;
    private static final String DEFAULT_NTF_ID = "todo_practice";
    public static final String INTENT_EXT_TODO_ITEM = "ie_todo_item";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final TodoItem todoItem = intent.getParcelableExtra(INTENT_EXT_TODO_ITEM);

        Notification notification = getForegroundNotification(todoItem);
        startForeground(FOREGROUND_ID, notification);

        if (todoItem != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    InMemoryCache.RemindTimeCache.remindTimeMills = todoItem.getRemindTimeMillis();
                    InMemoryCache.RemindTimeCache.itemDbId = todoItem.getId();
                    try {
                        Thread.sleep(todoItem.getRemindTimeMillis() - System.currentTimeMillis());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            RemindUtil.remind(todoItem);
                            stopSelf();
                        }
                    });
                }
            }).start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private Notification getForegroundNotification(TodoItem todoItem) {
        Intent ntfIntent = new Intent(this, MainActivity.class);
        ntfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pi = PendingIntent.getActivity(this, 0, ntfIntent, 0);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        final String contentTitle;
        final String contentText;

        if (todoItem != null) {
            contentTitle = todoItem.getTitle();
            contentText = "Remind time: " + FormatUtil.formatToTime(todoItem.getRemindTimeMillis());
        } else {
            contentTitle = "Todo Reminder Service";
            contentText = "It used to keep reminder running.";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, DEFAULT_NTF_ID);
        builder.setContentTitle(contentTitle);
        builder.setContentText(contentText);
        builder.setContentIntent(pi);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "TODO_CHANNEL", importance);

            builder.setChannelId(NOTIFICATION_CHANNEL_ID);
            nm.createNotificationChannel(channel);
        }
        return builder.build();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }
}
