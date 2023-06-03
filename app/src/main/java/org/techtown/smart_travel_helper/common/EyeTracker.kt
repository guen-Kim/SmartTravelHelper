package org.techtown.smart_travel_helper.common

/*
*
**/
object EyeTracker {
    var limitTime: Long = 3000L
    var startTime: Long = 0L // 눈감기 시작한 시간
    var alarmTime: Long = 0
    var isClosed: Boolean = false
    var timeAdjustmentFactor: Int = 0 // 시간조정변수
    var guideStart = true
    var alarmStart = true

    var headDownLimitTime : Long = 3000L
    var headDownStartTime : Long = 0L
    var headDownAlarmTime : Long = 0L
    var isHeadDown: Boolean = true
    var timeAdjustmentFactor_headDown: Int = 0

    var go : Boolean = false


    var alarmCallState: Boolean =false
    var closedCount: Int = 0

    fun setAlarmTime(startTime:Long, limitTime: Long) {
        alarmTime = EyeTracker.startTime + EyeTracker.limitTime
    }

    fun callSound(){



    }







}