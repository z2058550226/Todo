package com.practice.todo.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by suikajy on 2019.10.3
 */

@SuppressLint("SimpleDateFormat")
fun Long.formatToTime(pattern: String = "MM-dd HH:mm:ss"): String {
    return SimpleDateFormat(pattern).format(Date(this))
}