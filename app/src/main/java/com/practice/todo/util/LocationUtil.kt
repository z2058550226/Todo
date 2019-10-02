package com.practice.todo.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import com.practice.todo.App
import org.jetbrains.anko.toast

/**
 * Created by suikajy on 2019.10.2
 */
object LocationUtil {

    fun getNetWorkLocation(): Location? {
        var location: Location? = null
        val locationManager =
            App.instance.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                App.instance,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            App.instance.toast("no location permission")
            return null
        }

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }
        return location
    }
}