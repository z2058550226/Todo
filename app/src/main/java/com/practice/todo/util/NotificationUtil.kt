package com.practice.todo.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.practice.todo.App
import com.practice.todo.MainActivity
import com.practice.todo.R
import com.practice.todo.storage.database.entity.TodoItem

/**
 * Created by suikajy on 2019.10.2
 */
object NotificationUtil {

    const val NOTIFICATION_CHANNEL_ID = "10001"
    const val default_notification_channel_id = "default"

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
        mBuilder.setContentIntent(pendingIntent)
        mBuilder.setSmallIcon(R.drawable.ic_launcher)
        mBuilder.setAutoCancel(true)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "NOTIFICATION_CHANNEL_NAME",
                importance
            )
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID)
            mNotificationManager.createNotificationChannel(notificationChannel)
        }
        mNotificationManager.notify(System.currentTimeMillis().toInt(), mBuilder.build())
    }

}