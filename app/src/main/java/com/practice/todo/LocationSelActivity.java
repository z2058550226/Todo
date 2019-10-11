package com.practice.todo;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.practice.todo.util.LocationUtil;

public class LocationSelActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String IE_LOCATION = "ie_loc";
    public static final int REQ_CODE = 0x42;
    public static final int RES_CODE = 0x43;

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, LocationSelActivity.class);
        activity.startActivityForResult(intent, REQ_CODE);
    }

    private GoogleMap mMap;
    private LatLng mSelectedLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_sel);
        Toolbar toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeButtonEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Location curLocation = LocationUtil.getLocation();
        if (curLocation != null) {
            LatLng curLatLng = new LatLng(curLocation.getLatitude(), curLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(curLatLng));
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.clear();
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Selected location");
                mMap.addMarker(markerOptions);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                mSelectedLocation = latLng;
            }
        });
    }

    public void onConfirm(View view) {
        if (mSelectedLocation == null) {
            Toast.makeText(this, "please select a target location", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(IE_LOCATION, mSelectedLocation);
        setResult(RES_CODE, intent);
        finish();
    }
}
