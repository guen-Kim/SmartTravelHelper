package org.techtown.smart_travel_helper.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import org.techtown.smart_travel_helper.R
import org.techtown.smart_travel_helper.common.EyeTracker
import org.techtown.smart_travel_helper.databinding.ActivityDetectionResultBinding

class DetectionResultActivity : AppCompatActivity() {

    lateinit var binding: ActivityDetectionResultBinding
    lateinit var animFadeIn: Animation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // view binding
        binding = ActivityDetectionResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setDectectionData()


    }


    fun init() {
        // 애니메이션
        animFadeIn = AnimationUtils.loadAnimation(this, R.anim.splash_fadein);
        binding.llResultContainer.startAnimation(animFadeIn)
        // 돌아가기 버튼
        binding.btnBack.setOnClickListener {  v -> finish() }

        // 저장 버튼
        binding.btnSave.setOnClickListener { v ->


        }

    }

    fun setDectectionData() {

        if(EyeTracker.isHeadDetection&& EyeTracker.isEyeDetection ){
            binding.tvDrivingResult.text = "심각한 졸음운전"
            binding.tvExplain.text = "장시간 눈감음, 고개 떨굼이 탐지되었습니다."

        }else if(EyeTracker.isHeadDetection){
            binding.tvDrivingResult.text = "졸음운전"
            binding.tvExplain.text = "고개 떨굼이 탐지되었습니다."


        }else if(EyeTracker.isEyeDetection){
            binding.tvDrivingResult.text = "졸음운전"
            binding.tvExplain.text = "장시간 눈감음이 탐지되었습니다."
        }else{
            binding.tvDrivingResult.text = "안전운전"
            binding.tvExplain.text = " "
        }

        val drivingTime = EyeTracker.drivingEnd - EyeTracker.drivingStart
        binding.tvDrivingTime.text = "${drivingTime /(1000*60*60)}H ${drivingTime/ (1000 * 60) } min  ${drivingTime/1000} s"


    }



    override fun onBackPressed() {
        return;
    }
}