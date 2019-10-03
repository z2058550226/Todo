package com.practice.todo.util

import android.location.Location

/**
 * Created by suikajy on 2019.10.3
 */
object InMemoryCache {

    object RemindTimeCache {
        var remindTimeMills: Long = 0L
        var itemDbId: Int = 0
    }

    object RemindLocationCache {
        var remindLocation: Location? = null
        var itemDbId: Int = 0
    }

}