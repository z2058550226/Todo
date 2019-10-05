package com.practice.todo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.practice.todo.base.CoroutineActivity
import com.practice.todo.util.LocationUtil
import kotlinx.android.synthetic.main.activity_todo_edit.*
import org.jetbrains.anko.toast

class LocationSelActivity : CoroutineActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mSelectedLocation: LatLng

    companion object {

        const val REQ_CODE = 0x42
        const val RES_CODE = 0x43
        const val IE_LOCATION = "ie_loc"
        fun start(activity: Activity) {
            val i = Intent(activity, LocationSelActivity::class.java)
            activity.startActivityForResult(i, REQ_CODE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_sel)

        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        mToolbar.setNavigationOnClickListener { finish() }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        LocationUtil.getLocation()?.apply {
            val currentLocation = LatLng(latitude, longitude)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))
        }

        mMap.setOnMapClickListener {
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(it).title("Selected location"))
            mMap.animateCamera(CameraUpdateFactory.newLatLng(it))
            mSelectedLocation = it
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onConfirm(view: View) {
        if (this::mSelectedLocation.isInitialized.not()) {
            toast("please select a target location")
            return
        }
        val intent = Intent()
        intent.putExtra(IE_LOCATION, mSelectedLocation)
        setResult(RES_CODE, intent)
        finish()
    }
}
