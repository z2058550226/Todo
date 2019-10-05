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

    fun getLocation(): Location? {
        var resultLocation: Location? = null

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

        val enabledProviders = locationManager.getProviders(true)
        for (provider in enabledProviders) {
            val lastKnownLocation = locationManager.getLastKnownLocation(provider) ?: continue

            if (resultLocation == null || lastKnownLocation.accuracy < resultLocation.accuracy) {
                resultLocation = lastKnownLocation
            }
        }
        resultLocation ?: App.instance.toast("please check if the network or gps is turned on")

        return resultLocation
    }

}