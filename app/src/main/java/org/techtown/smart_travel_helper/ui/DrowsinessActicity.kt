package org.techtown.smart_travel_helper.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.AnimationDrawable
import android.location.Location
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import org.techtown.smart_travel_helper.PERMISSION_REQUEST_CODE
import org.techtown.smart_travel_helper.R
import org.techtown.smart_travel_helper.application.GlobalApplication
import org.techtown.smart_travel_helper.camerax.CameraManager
import org.techtown.smart_travel_helper.common.EyeTracker
import org.techtown.smart_travel_helper.databinding.ActivityDrowsinessDetectionBinding
import org.techtown.smart_travel_helper.kakaonavi.NaviBaseActivity
import org.techtown.smart_travel_helper.location.ClientFusedLocation
import org.techtown.smart_travel_helper.location.OnLocationUpdateListener
import org.techtown.smart_travel_helper.service.NaviService
import org.techtown.smart_travel_helper.showSnackbar
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class DrowsinessActicity : NaviBaseActivity(), ActivityCompat.OnRequestPermissionsResultCallback,
    OnLocationUpdateListener {

    private lateinit var binding: ActivityDrowsinessDetectionBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraManager: CameraManager
    var PATHGUIDE: Boolean = false
    var WARRINGSOUND: Boolean = false
    var time: Long = 0;
    lateinit var mediaPlayerA: MediaPlayer
    lateinit var mediaPlayerB: MediaPlayer
    lateinit var animationDrawable: AnimationDrawable


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 액티비티 애니매이션
        overridePendingTransition(R.anim.horizon_enter, R.anim.horizon_exit)

        // view binding
        binding = ActivityDrowsinessDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        createCameraManager()
        layout = binding.root
        // 카메라 실행 유지를 위한 스레드 생성
        cameraExecutor = Executors.newSingleThreadExecutor()

        startFusedLocation()

        setStatusBarColor()
    }

    private fun init() {
        startForegroundService(this, "안전한 주행중", "안전하고 즐거운 운전")

        mediaPlayerA = MediaPlayer.create(this, R.raw.alarm);
        mediaPlayerB = MediaPlayer.create(this, R.raw.alarm);


        binding.swPathGuide.setOnCheckedChangeListener { CompoundButton, onSwitch ->
            PATHGUIDE = onSwitch

        }

        binding.swWarringSound.setOnCheckedChangeListener { CompoundButton, onSwitch ->
            WARRINGSOUND = onSwitch

        }
        binding.btnStart.setOnClickListener { v ->
            binding.btnStart.isEnabled = false
            binding.btnEnd.isEnabled = true
            binding.swPathGuide.isEnabled = false
            binding.swWarringSound.isEnabled = false
            binding.ivLogo.visibility = View.GONE
            binding.ivTextLogo.visibility = View.GONE
            binding.tvEx.visibility = View.GONE
            binding.tvUserState.text = "실시간 감지중"

            // result 변수 초기화
            EyeTracker.isEyeDetection = false
            EyeTracker.isHeadDetection = false

            //주행 시작 타이머
            EyeTracker.drivingStart = System.currentTimeMillis()

            // 카메라 활성화
            cameraManager.startCamera()
            // usetState
            binding.cvUserStateDisplay.setBackgroundResource(R.drawable.gradient_list)
            animationDrawable = binding.cvUserStateDisplay.background as AnimationDrawable
            animationDrawable.setEnterFadeDuration(2000);
            animationDrawable.setExitFadeDuration(4000);
            animationDrawable.start()
        }

        binding.btnEnd.setOnClickListener { v ->

            EyeTracker.drivingEnd = System.currentTimeMillis()
            //자원해제
            stopDetection()

            // result 페이지
            startActivity(Intent(applicationContext, DetectionResultActivity::class.java))
            finish()
        }
    }


    fun startNavi() {

        val fragment = NaviFragment()
        val bundle = Bundle()
        bundle.putString("key", getKey())

        // 프래그먼트에 Bundle 설정
        fragment.arguments = bundle

        // FragmentManager를 사용하여 프래그먼트 추가 또는 교체
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.fragment_container, fragment)
        transaction.commit()

    }






    override fun onBackPressed() {
        if (System.currentTimeMillis() - time >= 2000) {
            time = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), "한번더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        } else if (System.currentTimeMillis() - time < 2000) {
            super.onBackPressed()
        }

    }


    /* 검색 완료 콜백 */
    // 검색 완료 후,
    override fun onCompleteSearch() {
        startNavi()
    }




    /**권한 설정창에서 복귀 or 다른 작업 후 액티비티 복귀**/
    override fun onRestart() {
        super.onRestart()
        var bChack = false
        if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.CAMERA)
            || PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            // 필수 권한 설정 화면에서 권한 설정하지 않음
            bChack = true
        }
        if (bChack) {
            Toast.makeText(this, "요청된 모든 권한을 설정해야 합니다.", Toast.LENGTH_SHORT).show()
            // finish()
        } else {
            Toast.makeText(this, "기능을 사용하실 수 있습니다.", Toast.LENGTH_SHORT).show()
            startFusedLocation()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopDetection()
    }

    private fun stopDetection() {
        if (clientFusedLocation != null) {
            clientFusedLocation.stopLocationUpdates() // 위치 업데이트 요청 종료
        }

        try {
            // MediaPlayer 해지
            if (mediaPlayerA != null && mediaPlayerA.isPlaying()) {
                mediaPlayerA.stop()
                mediaPlayerA.release()
            }
            if (mediaPlayerB != null && mediaPlayerB.isPlaying()) {
                mediaPlayerA.stop()
                mediaPlayerB.release()

            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
        // 포그라운드 서비스 종료
        stopForegroundService(this)
    }


    private fun createCameraManager() {
        cameraManager = CameraManager(
            this,
            binding.previewView,
            this,
            binding.graphicOverlayFinder,
            this
        )
    }





    // ---------------------Service--------------------
    private fun startForegroundService(context: Context, title: String, content: String) {
        val bundle = Bundle().apply {
            putString("title", title)
            putString("content", content)
        }

        val intent = Intent(context, NaviService::class.java).apply {
            putExtra(NaviService.EXTRA_BUNDLE, bundle)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun stopForegroundService(context: Context) {
        context.stopService(Intent(context, NaviService::class.java))
    }


}

