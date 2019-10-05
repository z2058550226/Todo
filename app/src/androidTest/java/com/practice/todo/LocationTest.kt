package com.practice.todo

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.practice.todo.util.LocationUtil
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocationTest {

    @Test
    fun testLocation() {
        val netWorkLocation = LocationUtil.getLocation()
        netWorkLocation?.apply {
            Log.e("ttt", netWorkLocation.toString())
        }
    }
}