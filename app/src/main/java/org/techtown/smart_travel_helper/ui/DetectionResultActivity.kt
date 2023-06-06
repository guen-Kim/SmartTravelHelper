package org.techtown.smart_travel_helper.ui

import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import org.techtown.smart_travel_helper.R
import org.techtown.smart_travel_helper.common.EyeTracker
import org.techtown.smart_travel_helper.databinding.ActivityDetectionResultBinding
import org.techtown.smart_travel_helper.firebase.FirebaseData


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
        binding.btnBack.setOnClickListener { v -> finish() }

        // 저장 버튼
        binding.btnSave.setOnClickListener { v ->


            displayDialog()


        }

    }

    private fun displayDialog() {
        val ad = AlertDialog.Builder(this)
        ad.setIcon(R.drawable.ic_launcher_foreground)
        ad.setTitle("주행결과 저장")
        ad.setMessage("이메일을 입력해주세요.")
        ad.setIcon(R.drawable.poi_dot)

        // Dialog 에 에딧텍스트를 추가
        val et: EditText = EditText(this)
        ad.setView(et)

        // Dialog 에 확인, 취소 Button 추가
        ad.setPositiveButton("확인") { dialog, _ ->


            var email: String = et.text.toString()
            setDocument(
                FirebaseData(
                    email = email,
                    headDetection = EyeTracker.isHeadDetection,
                    eyeDetection = EyeTracker.isEyeDetection,
                    drivingTime = binding.tvDrivingTime.text.toString()
                )
            )
            dialog.dismiss()
        }



        ad.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss()
        }
        ad.show()
    }

    private fun setDocument(data: FirebaseData) {
        FirebaseFirestore.getInstance()
            .collection("Login")
            .document(data.email)
            .set(data)
            .addOnSuccessListener {
                Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "이메일을 확인해주세요!", Toast.LENGTH_LONG).show()
            }
    }


    private fun setDectectionData() {

        if (EyeTracker.isHeadDetection && EyeTracker.isEyeDetection) {
            binding.tvDrivingResult.text = "심각한 졸음운전"
            binding.tvExplain.text = "장시간 눈감음, 고개 떨굼이 탐지되었습니다."

        } else if (EyeTracker.isHeadDetection) {
            binding.tvDrivingResult.text = "졸음운전"
            binding.tvExplain.text = "고개 떨굼이 탐지되었습니다."


        } else if (EyeTracker.isEyeDetection) {
            binding.tvDrivingResult.text = "졸음운전"
            binding.tvExplain.text = "장시간 눈감음이 탐지되었습니다."
        } else {
            binding.tvDrivingResult.text = "안전운전"
            binding.tvExplain.text = " "
        }

        val drivingTime = EyeTracker.drivingEnd - EyeTracker.drivingStart
        binding.tvDrivingTime.text =
            "${drivingTime / (1000 * 60 * 60)}H ${drivingTime / (1000 * 60)} min  ${drivingTime / 1000} s"


    }


    override fun onBackPressed() {
        return;
    }
}