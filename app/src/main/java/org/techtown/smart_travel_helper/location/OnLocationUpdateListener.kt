package org.techtown.smart_travel_helper.location

import android.location.Location

interface OnLocationUpdateListener {
    fun onLocationUpdated(location: Location?)
}