package com.practice.todo.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.practice.todo.MainActivity
import com.practice.todo.R
import com.practice.todo.base.CoroutineService
import com.practice.todo.storage.database.entity.TodoItem
import com.practice.todo.util.InMemoryCache
import com.practice.todo.util.LocationUtil
import com.practice.todo.util.RemindUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by suikajy on 2019.10.3
 */
class LocationService : CoroutineService() {

    companion object {
        private const val DISTANCE_TO_REMIND_IN_METER = 500f
        private const val NOTIFICATION_CHANNEL_ID = "10107"
        private const val FOREGROUND_ID = 41541
        private const val DEFAULT_NTF_ID = "todo_ls_ntf"
        const val INTENT_EXT_TODO_ITEM = "ie_todo_item"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val todoItem = intent?.getParcelableExtra<TodoItem>(INTENT_EXT_TODO_ITEM)
        if (todoItem != null) {
            val notification = getForegroundNotification(todoItem)
            startForeground(FOREGROUND_ID, notification)
            val targetLocation = todoItem.remindLocation
            launch {
                InMemoryCache.RemindLocationCache.itemDbId = todoItem.id
                InMemoryCache.RemindLocationCache.remindLocation = todoItem.remindLocation
                while (true) {
                    LocationUtil.getLocation()?.apply {
                        val distance = this.distanceTo(targetLocation)
                        if (distance < DISTANCE_TO_REMIND_IN_METER) {
                            if (todoItem.description.isEmpty()) {
                                todoItem.description = "You have arrived at your destination"
                            }
                            InMemoryCache.RemindLocationCache.itemDbId = 0
                            InMemoryCache.RemindLocationCache.remindLocation = null
                            RemindUtil.remind(todoItem)
                            stopSelf()
                        }
                        val newNtf = getForegroundNotification(todoItem)
                        startForeground(FOREGROUND_ID, newNtf)
                    }
                    delay(5000)
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getForegroundNotification(todoItem: TodoItem): Notification {
        val ntfIntent = Intent(this, MainActivity::class.java)
        ntfIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pi = PendingIntent.getActivity(this, 0, ntfIntent, 0)

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val contentTitle: String = todoItem.title
        val remindLocation = todoItem.remindLocation
        val contentText = if (remindLocation != null) {
            val netWorkLocation = LocationUtil.getLocation()
            if (netWorkLocation != null) {
                "It's ${netWorkLocation.distanceTo(remindLocation)} meters from the reminder site."
            } else {
                "Remind within 500 meters from: " +
                        "${remindLocation.longitude.toString().take(7)}, " +
                        remindLocation.latitude.toString().take(7)
            }
        } else {
            "target location wrong"
        }

        val builder = NotificationCompat.Builder(
            this,
            DEFAULT_NTF_ID
        )
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setContentIntent(pi)
            .setSmallIcon(R.drawable.ic_launcher)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "TODO_CHANNEL", importance)

            builder.setChannelId(NOTIFICATION_CHANNEL_ID)
            nm.createNotificationChannel(channel)
        }
        return builder.build()
    }

    override fun onDestroy() {
        stopForeground(true)
        super.onDestroy()
    }
}