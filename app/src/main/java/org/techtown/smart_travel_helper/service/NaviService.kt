package org.techtown.smart_travel_helper.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import org.techtown.smart_travel_helper.R
import org.techtown.smart_travel_helper.ui.DrowsinessActicity

class NaviService : Service() {
    companion object {
        const val NOTIFICATION_ID = 2000
        const val CHANNEL_ID = "STH_channel"
        const val EXTRA_BUNDLE = "bundle"
    }

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel (
                CHANNEL_ID,
                "주행 정보",
                NotificationManager.IMPORTANCE_LOW
            )

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    override fun onStartCommand(intent : Intent, flags : Int, startId : Int) : Int {
        startForeground(NOTIFICATION_ID, createNotification(intent))
        return Service.START_NOT_STICKY // 서비스를 명시적으로 다시 시작할 때 까지 만들지 않습니다.
    }

    override fun onTaskRemoved(rootIntent : Intent) {
        super.onTaskRemoved(rootIntent)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // 서비스가 종료된다면
    override fun onDestroy() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
        super.onDestroy()
    }

    override fun onBind(intent : Intent) : IBinder? {
        return null
    }

    private fun createNotification(intent: Intent?): Notification {
        val title: String?
        val content: String?
        if (intent != null) {
            // 인텐트로 UI 데이터 입력받기
            val bundle = intent.getBundleExtra("bundle")
            title = bundle?.getString("title")
            content = bundle?.getString("content")
        } else {
            title = "주행 중"
            content = "안전한 운전"
        }

        val resultIntent = Intent(this, DrowsinessActicity::class.java)
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this,
            2,
            resultIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        val icon = BitmapFactory.decodeResource(resources, R.drawable.poi_dot)

        // TargetSDK 27 대응. Channel ID 적용된 notification 생성.
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.poi_dot)
            .setLargeIcon(icon)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}