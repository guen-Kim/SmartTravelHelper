package org.techtown.smart_travel_helper.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import org.techtown.smart_travel_helper.databinding.ActivityMainBinding


/**
 * https://youngest-programming.tistory.com/371 리팩토링 참고
 * */

class MainActivity : AppCompatActivity(){


    lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }


}