package org.techtown.smart_travel_helper.ui

import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil

import org.techtown.smart_travel_helper.R
import org.techtown.smart_travel_helper.databinding.ActivityMainBinding
import org.techtown.smart_travel_helper.location.ClientFusedLocation
import org.techtown.smart_travel_helper.location.OnLocationUpdateListener



/**
 * https://youngest-programming.tistory.com/371 리팩토링 참고
 * */

class MainActivity : AppCompatActivity(){


    lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // binding data
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)


    }


}