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
import com.practice.todo.util.RemindUtil
import com.practice.todo.util.formatToTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class RemindService : CoroutineService() {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "10007"
        private const val FOREGROUND_ID = 41441
        private const val DEFAULT_NTF_ID = "todo_practice"
        const val INTENT_EXT_TODO_ITEM = "ie_todo_item"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val todoItem = intent?.getParcelableExtra<TodoItem>(INTENT_EXT_TODO_ITEM)
        val notification = getForegroundNotification(todoItem)
        startForeground(FOREGROUND_ID, notification)
        if (todoItem != null) {
            launch {
                InMemoryCache.RemindTimeCache.remindTimeMills = todoItem.remindTimeMillis
                InMemoryCache.RemindTimeCache.itemDbId = todoItem.id
                delay(todoItem.remindTimeMillis - System.currentTimeMillis())
                RemindUtil.remind(todoItem)
                stopSelf()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun getForegroundNotification(todoItem: TodoItem?): Notification {
        val ntfIntent = Intent(this, MainActivity::class.java)
        ntfIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pi = PendingIntent.getActivity(this, 0, ntfIntent, 0)

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val contentTitle: String
        val contentText: String
        if (todoItem != null) {
            contentTitle = todoItem.title
            contentText = "Remind time: " + todoItem.remindTimeMillis.formatToTime()
        } else {
            contentTitle = "Todo Reminder Service"
            contentText = "It used to keep reminder running."
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


}