package com.practice.todo.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.practice.todo.MainActivity;
import com.practice.todo.R;
import com.practice.todo.storage.database.entity.TodoItem;
import com.practice.todo.util.InMemoryCache;
import com.practice.todo.util.LocationUtil;
import com.practice.todo.util.RemindUtil;

public class LocationService extends Service {

    private static final float DISTANCE_TO_REMIND_IN_METER = 500f;
    private static final String NOTIFICATION_CHANNEL_ID = "10107";
    private static final int FOREGROUND_ID = 41541;
    private static final String DEFAULT_NTF_ID = "todo_ls_ntf";
    public static final String INTENT_EXT_TODO_ITEM = "ie_todo_item";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final TodoItem todoItem = intent.getParcelableExtra(INTENT_EXT_TODO_ITEM);
        if (todoItem != null) {
            Notification notification = getForegroundNotification(todoItem);
            startForeground(FOREGROUND_ID, notification);
            final Location targetLocation = todoItem.getRemindLocation();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    InMemoryCache.RemindLocationCache.itemDbId = todoItem.getId();
                    InMemoryCache.RemindLocationCache.remindLocation = todoItem.getRemindLocation();
                    while (true) {
                        Location location = LocationUtil.getLocation();
                        if (location != null) {
                            float distance = location.distanceTo(targetLocation);
                            if (distance < DISTANCE_TO_REMIND_IN_METER) {
                                if (TextUtils.isEmpty(todoItem.getDescription())) {
                                    todoItem.setDescription("You have arrived at your destination");
                                }
                                InMemoryCache.RemindLocationCache.itemDbId = 0;
                                InMemoryCache.RemindLocationCache.remindLocation = null;
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        RemindUtil.remind(todoItem);
                                        stopSelf();
                                    }
                                });
                            }
                        }
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
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

        String contentTitle = todoItem.getTitle();
        Location remindLocation = todoItem.getRemindLocation();
        final String contentText;
        if (remindLocation != null) {
            Location netWorkLocation = LocationUtil.getLocation();
            if (netWorkLocation != null) {
                contentText = "It's " + netWorkLocation.distanceTo(remindLocation) + " meters from the reminder site.";
            } else {
                contentText = "Remind within 500 meters from: " +
                        String.valueOf(remindLocation.getLongitude()).substring(0, 7) +
                        String.valueOf(remindLocation.getLatitude()).substring(0, 7);
            }
        } else {
            contentText = "target location wrong";
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }
}
