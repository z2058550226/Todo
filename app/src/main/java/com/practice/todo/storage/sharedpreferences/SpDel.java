package com.practice.todo.storage.sharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.practice.todo.App;

/**
 * 使用安卓的SharedPreference来存储用户是否第一次登陆。
 */
public class SpDel {

    private static final String APP_SP = "app_sp";
    private static final String IS_FIRST_RUN = "is_first_run";

    public static void setIsFirstRun(boolean isFirstRun) {
        SharedPreferences sp = App.instance.getSharedPreferences(APP_SP, Context.MODE_PRIVATE);
        sp.edit().putBoolean(IS_FIRST_RUN, isFirstRun).apply();
    }

    public static boolean isFirstRun() {
        SharedPreferences sp = App.instance.getSharedPreferences(APP_SP, Context.MODE_PRIVATE);
        return sp.getBoolean(IS_FIRST_RUN, true);
    }

}
