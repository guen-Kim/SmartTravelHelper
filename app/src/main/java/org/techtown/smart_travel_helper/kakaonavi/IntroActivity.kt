package org.techtown.smart_travel_helper.kakaonavi

import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.kakaomobility.knsdk.KNLanguageType
import com.kakaomobility.knsdk.common.objects.KNError_Code_C302
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.techtown.smart_travel_helper.BuildConfig
import org.techtown.smart_travel_helper.R
import org.techtown.smart_travel_helper.application.GlobalApplication.Companion.knsdk
import org.techtown.smart_travel_helper.databinding.ActivityIntroBinding
import org.techtown.smart_travel_helper.ui.DrowsinessActicity


class IntroActivity : AppCompatActivity() {
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private val KAKAO_KEY: String = BuildConfig.kakaoNaviKey; // API KEY
    lateinit var animFadeIn: Animation
    lateinit var animTransrate: Animation

    private lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // view binding
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                startActivity(Intent(applicationContext, DrowsinessActicity:class.java))

                finish()
            }
        });



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


}