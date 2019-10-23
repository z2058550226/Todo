package com.practice.todo;

import android.app.Application;

/**
 * 安卓的Application类，添加了静态实例方便到处获取。
 */
public class App extends Application {

    public static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
