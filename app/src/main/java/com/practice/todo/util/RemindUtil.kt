package com.practice.todo.util

import android.app.Notification.VISIBILITY_SECRET
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.practice.todo.App
import com.practice.todo.MainActivity
import com.practice.todo.R
import com.practice.todo.storage.database.entity.TodoItem

/**
 * Created by suikajy on 2019.10.2
 */
object NotificationUtil {

    private const val NOTIFICATION_CHANNEL_ID = "10051"
    private const val default_notification_channel_id = "todo_practice2"

    fun notification(todoItem: TodoItem) {
        val notificationIntent = Intent(App.instance, MainActivity::class.java)
        notificationIntent.putExtra("fromNotification", true)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(App.instance, 0, notificationIntent, 0)
        val mNotificationManager =
            App.instance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val mBuilder =
            NotificationCompat.Builder(
                App.instance,
                default_notification_channel_id
            )
        mBuilder.setContentTitle(todoItem.title)
            .setContentIntent(pendingIntent)
            .setContentText("You can mark this todo is done")
            .setSmallIcon(R.drawable.ic_launcher)
            .setAutoCancel(true)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "NOTIFICATION_CHANNEL_NAME",
                importance
            )
            channel.enableVibration(true)
            channel.canBypassDnd()
            channel.lockscreenVisibility = VISIBILITY_SECRET
            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.audioAttributes
            channel.group
            channel.setBypassDnd(true)
            channel.shouldShowLights()

            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID)
            mNotificationManager.createNotificationChannel(channel)
        }
        mNotificationManager.notify(System.currentTimeMillis().toInt(), mBuilder.build())
    }

}