package org.techtown.smart_travel_helper.kakaonavi

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.kakaomobility.knsdk.KNLanguageType
import com.kakaomobility.knsdk.common.objects.KNError_Code_C302
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.techtown.smart_travel_helper.BuildConfig
import org.techtown.smart_travel_helper.PERMISSION_REQUEST_CODE
import org.techtown.smart_travel_helper.R
import org.techtown.smart_travel_helper.application.GlobalApplication.Companion.knsdk
import org.techtown.smart_travel_helper.databinding.ActivityIntroBinding
import org.techtown.smart_travel_helper.showSnackbar
import org.techtown.smart_travel_helper.ui.DrowsinessActicity


class IntroActivity : AppCompatActivity() {
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private val KAKAO_KEY: String = BuildConfig.kakaoNaviKey; // API KEY
    lateinit var animFadeIn: Animation
    lateinit var animTransrate: Animation
    lateinit var layout: View


    private lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // view binding
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        layout = binding.root

        initSplashAnim()

        // 안드로이드 앱 홈 화면 위젯(HomeScreenWidgets)을 클릭한 후, 홈 버튼을 누르고 앱 아이콘을 누를 때 액티비티(Activity)가 중첩
        if (!isTaskRoot()) {
            var intent = getIntent()
            var intentAction = intent.action
            // 메인 액티비티가 아니면 종료 시킨다.
            // CATEGORY_LAUNCHER 이고 ACTION_MAIN 이라면 앱 이 실행 중
            // 현재 활동을 완료하고 반환하여 사용자가 런처에서 동일한 활동을 다시 시작하는 것을 효과적으로 방지
            // 이것은 성능 및 메모리 문제를 일으킬 수 있는 동일한 활동의 여러 인스턴스가 생성되는 것을 방지하기 위한 Android 개발의 일반적인 관행
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intentAction != null && intentAction.equals(
                    Intent.ACTION_MAIN
                )
            ) {
                //실행될 때 Root가 아니면 그냥 액티비티를 종료
                finish()
                return
            }
        }
        sdkInit()
    }

    private fun initSplashAnim() {
        animFadeIn = AnimationUtils.loadAnimation(this, R.anim.splash_fadein);
        animTransrate = AnimationUtils.loadAnimation(this, R.anim.splash_translate);

        animFadeIn.setAnimationListener(object :
            Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                //startActivity(Intent(applicationContext, DrowsinessActicity::class.java))
                startActivity(Intent(applicationContext, DrowsinessActicity::class.java))

                finish()
            }
        });
    }

    private fun OnCheckPermission(layout: View) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 설치된 앱내 안드로이드 os버전 (VERSION.SDK_INT)가 마시멜로 버전 이상이라면

            // 권한이 있는지 없는지 확인
            // val locationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            // 앱에 해당 권한이 하나라도 없다면
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                // 교육용 UI가 필요한 유저인가?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    // true, 이미 허용되있던 권한을 '허용하지 않음' 으로 돌린 경우 or 해당 권한을 명시적으로 거부한 경험이 있는 경우
                    layout.showSnackbar(
                        R.string.permission_request,
                        Snackbar.LENGTH_INDEFINITE,
                        R.string.ok
                    ) {
                        ActivityCompat.requestPermissions(this,
                            permissions, PERMISSION_REQUEST_CODE
                        ) // 권한 요청
                    }

                } else {
                    // false, 설치하고 요청 메시지를 한번도 못 받아서 요청 대화상자 선택 자체를 못한 경우
                    layout.showSnackbar(R.string.permission_request, Snackbar.LENGTH_INDEFINITE, R.string.ok) {
                        ActivityCompat.requestPermissions(this,
                            permissions, PERMISSION_REQUEST_CODE
                        ) // 권한 요청
                    }
                }
            }
        }
        else {
            sdkInit()
        }

    }


    // 권한 결과요청 처리 결과 수신
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
                        sdkInit()

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

    /**권한 설정창**/
    protected fun alertAuthoritySetting() {
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



    override fun onDestroy() {
        super.onDestroy()
        // life cycle, 코루틴 종료
        job.cancel()
    }

    private fun sdkInit() {
        scope.launch {
            /** 3. kakaoNaviSDK의 초기화 및 앱 정보 등록을 시작 이 과정에서 권한 체크 및 초기화 과정의 문제점을 체크하여 이상이 있을 경우 문제에 대한 에러 코드 넘겨줌**/
            knsdk.apply {
                initializeWithAppKey(KAKAO_KEY, "1.6.8", "ss",
                    KNLanguageType.KNLanguageType_KOREAN, aCompletion = {
                        if (it != null) {
                            android.util.Log.e("ABASDBASDB", "failed ${it.code}")
                            when (it.code) {
                                KNError_Code_C302 -> {
                                    OnCheckPermission(layout)
                                    Toast.makeText(this@IntroActivity, "위치 권한 설정후 다시 시작해주세요.", Toast.LENGTH_LONG).show()

                                }
                                else -> {
                                }
                            }
                        } else {
                            binding.view.startAnimation(animTransrate)
                            binding.tvSubTitle.startAnimation(animFadeIn)
                            binding.tvSth.startAnimation(animFadeIn)

                        }
                    })
            }

        }
    }


    companion object {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION, // 대략적인 위치(이것만 허용해도 기능 동작함)
            Manifest.permission.ACCESS_FINE_LOCATION,  // 정확한 위치
            Manifest.permission.CAMERA
        )
    }
}