package org.techtown.smart_travel_helper.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import org.techtown.smart_travel_helper.R
import org.techtown.smart_travel_helper.databinding.ActivityDetectionResultBinding

class DetectionResultActivity : AppCompatActivity() {

    lateinit var binding: ActivityDetectionResultBinding
    lateinit var animFadeIn: Animation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // view binding
        binding = ActivityDetectionResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        animFadeIn = AnimationUtils.loadAnimation(this, R.anim.splash_fadein);
        binding.tvResult.startAnimation(animFadeIn)


    }
}