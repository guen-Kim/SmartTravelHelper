package org.techtown.smart_travel_helper.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import org.techtown.smart_travel_helper.R
import org.techtown.smart_travel_helper.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {


    lateinit var binidng:ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // binding data
        binidng = DataBindingUtil.setContentView(
            this, R.layout.activity_main
        )
        //인증 요청
        binidng.tmap.setSKTMapApiKey("j1MKrD2Bcx2Vt1ibRJsO1akBYm4Rr9uV1UvSSja5")


        // TODO: 사용자 GPS 정보 받기
    }
}