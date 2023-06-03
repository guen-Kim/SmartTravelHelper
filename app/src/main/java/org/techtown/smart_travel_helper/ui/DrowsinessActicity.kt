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
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import org.techtown.smart_travel_helper.PERMISSION_REQUEST_CODE
import org.techtown.smart_travel_helper.R
import org.techtown.smart_travel_helper.camerax.CameraManager
import org.techtown.smart_travel_helper.databinding.ActivityDrowsinessDetectionBinding
import org.techtown.smart_travel_helper.kakaonavi.NaviBaseActivity
import org.techtown.smart_travel_helper.location.ClientFusedLocation
import org.techtown.smart_travel_helper.location.OnLocationUpdateListener
import org.techtown.smart_travel_helper.service.NaviService
import org.techtown.smart_travel_helper.showSnackbar
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class DrowsinessActicity : NaviBaseActivity(), ActivityCompat.OnRequestPermissionsResultCallback,
    OnLocationUpdateListener
{

    private lateinit var binding: ActivityDrowsinessDetectionBinding
    private lateinit var layout: View
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraManager: CameraManager
    private lateinit var clientFusedLocation: ClientFusedLocation
    var PATHGUIDE: Boolean = false
    var WARRINGSOUND: Boolean = false
    lateinit var mediaPlayerA: MediaPlayer
    lateinit var mediaPlayerB: MediaPlayer
    lateinit var animationDrawable: AnimationDrawable



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // binding data
        binding = DataBindingUtil.setContentView(
            this, R.layout.activity_drowsiness_detection
        )

        init()

        createCameraManager()
        layout = binding.root
        // 카메라 실행 유지를 위한 스레드 생성
        cameraExecutor = Executors.newSingleThreadExecutor()
        // 퍼미션 체크
        OnCheckPermission()

        setStatusBarColor()
    }

    private fun init(){
        startForegroundService(this, "안전한 주행중", "안전하고 즐거운 운전, STH")

        mediaPlayerA = MediaPlayer.create(this, R.raw.alarm);
        mediaPlayerB = MediaPlayer.create(this, R.raw.alarm);


        binding.swPathGuide.setOnCheckedChangeListener{CompoundButton, onSwitch ->
            PATHGUIDE = onSwitch
        }

        binding.swWarringSound.setOnCheckedChangeListener{CompoundButton, onSwitch ->
            WARRINGSOUND = onSwitch
        }
        binding.btnStart.setOnClickListener { v->
            binding.btnStart.isEnabled = false
            binding.btnEnd.isEnabled = true
            binding.swPathGuide.isEnabled = false
            binding.swWarringSound.isEnabled = false
            binding.ivLogo.visibility = View.GONE
            binding.ivTextLogo.visibility = View.GONE
            binding.tvEx.visibility = View.GONE
            binding.tvUserState.text = "실시간 감지중"

            // 카메라 활성화
            cameraManager.startCamera()
            // usetState
            binding.cvUserStateDisplay.setBackgroundResource(R.drawable.gradient_list)
            animationDrawable = binding.cvUserStateDisplay.background as AnimationDrawable
            animationDrawable.setEnterFadeDuration(2000);
            animationDrawable.setExitFadeDuration(4000);
            animationDrawable.start()
        }
    }





    fun startNavi(){

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


    private fun OnCheckPermission() {
        // 권한이 있는지 없는지 확인
        // val locationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        // 앱에 해당 권한이 하나라도 없다면
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            // 교육용 UI가 필요한 유저인가?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CAMERA
                )
                || ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            ) {
                // true, 이미 허용되있던 권한을 '허용하지 않음' 으로 돌린 경우 or 해당 권한을 명시적으로 거부한 경험이 있는 경우
                layout.showSnackbar(
                    R.string.permission_request,
                    Snackbar.LENGTH_INDEFINITE,
                    R.string.ok
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        permissions,
                        PERMISSION_REQUEST_CODE
                    ) // 권한 요청
                }

            } else {
                // false, 설치하고 요청 메시지를 한 번 도 못 받아서 요청 대화상자 선택 자체를 못한 경우
                layout.showSnackbar(
                    R.string.permission_request,
                    Snackbar.LENGTH_INDEFINITE,
                    R.string.ok
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        permissions,
                        PERMISSION_REQUEST_CODE
                    ) // 권한 요청
                }
            }
        } else {
            startFusedLocation()
        }

    }


    // 결과요청 처리 결과 수신
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {

                if (grantResults.isNotEmpty()) {
                    var isAllGranted = true
                    // 요청한 권한 허용/거부 상태 한번에 체크
                    for (grant in grantResults) { // 권한 3개에 대해 (거부 -1, 허용 0)
                        if (grant != PackageManager.PERMISSION_GRANTED) {
                            // 거부한 권한이 있다면
                            isAllGranted = false
                            break
                        }
                    }

                    // 요청한 권한을 모두 허용했음.
                    if (isAllGranted) {
                        startFusedLocation()

                    } else {
                        // 허용하지 않은 권한이 있음. 필수권한/선택권한 여부에 따라서 별도 처리를 해주어야 함.
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                            || !ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.CAMERA
                            )
                        ) {
                            // 필수권한 "허용안함" 명시적으로 권한 거부 됨.
                            alertAuthoritySetting() // 권한 설정 유도 알람
                        } else {
                            // 접근 권한 거부하였음. ->

                        }
                    }
                }
            }
        }
    }


    /* 검색 완료 콜백 */
    // 검색 완료 후,
    override fun onCompleteSearch() {
        startNavi()
    }


    /**권한 설정창**/
    private fun alertAuthoritySetting() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("권한 설정")
        builder.setMessage("앱에서 요청한 권한이 없으면 정상적으로 기능을 사용할 수 없습니다. 권한을 설정해주세요")
        builder.setCancelable(false)
        builder.setPositiveButton("예", DialogInterface.OnClickListener { dialogInterface, i ->
            // 사용자가 직접 권한 설정 하도록 유도
            try {
                val intent =
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + packageName))
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
                startActivity(intent)
            }
        })
        builder.setNegativeButton("아니오", DialogInterface.OnClickListener { dialogInterface, i ->
            //finish()
        })
        builder.show()
    }

    /**권한 설정창에서 복 or 다른 작업 후 액티비티 복귀**/
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
        if (clientFusedLocation != null) {
            clientFusedLocation.stopLocationUpdates() // 위치 업데이트 요청 종료
        }

        // MediaPlayer 해지
        if (mediaPlayerA != null) {
            mediaPlayerA.release()
        }
        if (mediaPlayerB != null) {
            mediaPlayerB.release()
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

    private fun startFusedLocation() {
        clientFusedLocation = ClientFusedLocation(this, this)
        clientFusedLocation.requestLastLocation()
        clientFusedLocation.startLocationUpdates()
    }

    // user location
    override fun onLocationUpdated(location: Location?) {
        if (location != null) {
            val latitude = location.latitude
            val longitude = location.longitude
            Log.d(
                "Test", "GPS Location Latitude: $latitude" +
                        ", Longitude: $longitude"
            )
        } else {
            layout.showSnackbar(
                R.string.inactive_gps,
                Snackbar.LENGTH_INDEFINITE,
                R.string.ok
            ) {

            }
        }
    }
    // ---------------------Service--------------------
    private fun startForegroundService(context : Context, title : String, content : String) {
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

    private fun stopForegroundService(context : Context) {
        context.stopService(Intent(context, NaviService::class.java))
    }

    companion object {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION, // 대략적인 위치(이것만 허용해도 기능 동작함)
            Manifest.permission.ACCESS_FINE_LOCATION,  // 정확한 위치
            Manifest.permission.CAMERA
        )
    }
}

