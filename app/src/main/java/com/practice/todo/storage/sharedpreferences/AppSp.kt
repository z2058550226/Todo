package com.practice.todo.storage.sharedpreferences

object AppSp {

    var isFirstRun by AppSpDel("isFirstRun", true)

    class AppSpDel<T>(key: String, defaultValue: T) :
        SpDel<T>("app_sp", key, defaultValue)
}

