package org.techtown.smart_travel_helper.firebase

data class FirebaseData(
    val email: String,
    val headDetection: Boolean,
    val eyeDetection: Boolean,
    val drivingTime: String
)