package com.practice.todo.base

import android.app.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * Created by suikajy on 2019.10.3
 */
abstract class CoroutineService : Service(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    private val job by lazy { Job() }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}