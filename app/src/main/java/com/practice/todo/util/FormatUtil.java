package com.practice.todo.util;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

//日期格式化
public final class FormatUtil {
    @SuppressLint("SimpleDateFormat")
    public static String formatToTime(long timeMills) {
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm:ss");
        return format.format(new Date(timeMills));
    }
}
