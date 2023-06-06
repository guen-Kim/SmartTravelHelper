package org.techtown.smart_travel_helper.common

/*
*
**/
object EyeTracker {
    //주행시간
    var drivingStart: Long = 0L
    var drivingEnd: Long = 0L


    var limitTime: Long = 3000L
    //눈
    var startTime: Long = 0L // 눈감기 시작한 시간
    var alarmTime: Long = 0
    var isClosed: Boolean = false
    var timeAdjustmentFactor: Int = 0 // 시간조정변수
    var guideStart = true
    var alarmStart = true
    var isEyeDetection: Boolean = false

    //고개
    var headDownLimitTime : Long = 3000L
    var headDownStartTime : Long = 0L
    var headDownAlarmTime : Long = 0L
    var timeAdjustmentFactor_headDown: Int = 0
    var isHeadDetection: Boolean = false


    fun setAlarmTime(startTime:Long, limitTime: Long) {
        alarmTime = EyeTracker.startTime + EyeTracker.limitTime
    }

}