package com.practice.todo

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import com.practice.todo.util.LocationUtil
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class LocationService : Service(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val job by lazy { Job() }

    companion object {
        private const val DISTANCE_TO_REMIND_IN_METER = 500f
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return super.onStartCommand(intent, flags, startId)
    }

    fun startLocation(
        targetLongitude: Double,
        targetLatitude: Double,
        block: (Location, Boolean) -> Unit
    ) {
        val targetLocation = Location("target")
        targetLocation.latitude = targetLatitude
        targetLocation.longitude = targetLongitude
        launch {
            while (true) {
                LocationUtil.getNetWorkLocation()?.apply {
                    val distance = this.distanceTo(targetLocation)
                    block(this, distance < DISTANCE_TO_REMIND_IN_METER)
                }
                delay(5000)
            }
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}