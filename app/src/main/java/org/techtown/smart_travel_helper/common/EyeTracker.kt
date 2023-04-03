package org.techtown.smart_travel_helper.common

/*
*
**/
object EyeTracker {
    var limitTime: Long = 5000L // 5초
    var startTime: Long = 0L // 눈감기 시작한 시간
    var alarmTime: Long = 0
    var isClosed: Boolean = false
    var alarmCallState: Boolean =false
    var closedCount: Int = 0

    fun setAlarmTime(startTime:Long, limitTime: Long) {
        alarmTime = EyeTracker.startTime + EyeTracker.limitTime
    }

    fun callSound(){



    }







}