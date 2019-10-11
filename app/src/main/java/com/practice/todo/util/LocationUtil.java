package com.practice.todo.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.practice.todo.App;

import java.util.List;

public class LocationUtil {

    private static Handler sHandler = new Handler(Looper.getMainLooper());

    @Nullable
    public static Location getLocation() {
        Location resultLocation = null;

        LocationManager locationManager =
                (LocationManager) App.instance.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(
                App.instance,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            sHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(App.instance, "no location permission", Toast.LENGTH_LONG).show();
                }
            });
            return null;
        }

        List<String> enabledProviders = locationManager.getProviders(true);

        for (String provider : enabledProviders) {
            Location lastKnownLocation = locationManager.getLastKnownLocation(provider);
            if (lastKnownLocation == null) continue;

            if (resultLocation == null || lastKnownLocation.getAccuracy() < resultLocation.getAccuracy()) {
                resultLocation = lastKnownLocation;
            }
        }

        if (resultLocation == null) {
            sHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(App.instance, "please check if the network or gps is turned on", Toast.LENGTH_LONG).show();
                }
            });
        }

        return resultLocation;
    }
}
