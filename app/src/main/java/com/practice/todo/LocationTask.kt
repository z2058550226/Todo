package com.practice.todo

import android.location.Location
import com.practice.todo.util.LocationUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object LocationTask {

    private const val DISTANCE_TO_REMIND_IN_METER = 500f

    fun startLocation(
        targetLongitude: Double,
        targetLatitude: Double,
        scope: CoroutineScope,
        block: (Location, Boolean) -> Unit
    ) {
        val targetLocation = Location("target")
        targetLocation.latitude = targetLatitude
        targetLocation.longitude = targetLongitude
        scope.launch {
            while (true) {
                LocationUtil.getNetWorkLocation()?.apply {
                    val distance = this.distanceTo(targetLocation)
                    block(this, distance < DISTANCE_TO_REMIND_IN_METER)
                }
                delay(5000)
            }
        }
    }

}