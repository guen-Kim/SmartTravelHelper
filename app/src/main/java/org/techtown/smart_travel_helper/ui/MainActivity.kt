package org.techtown.smart_travel_helper.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.skt.tmap.TMapPoint
import com.skt.tmap.TMapView
import com.skt.tmap.TMapView.OnMapReadyListener
import com.skt.tmap.overlay.TMapMarkerItem
import org.techtown.smart_travel_helper.R
import org.techtown.smart_travel_helper.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {


    lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // binding data
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val tmapView = TMapView(this) // t맵 동적생성
        binding.tmapViewContainer.addView(tmapView) // 자식 뷰 (Tmap) 추가


        // 인증키 요청
        tmapView.setSKTMapApiKey("j1MKrD2Bcx2Vt1ibRJsO1akBYm4Rr9uV1UvSSja5")
        // 지도 생성 하기, 비동기 처리
        tmapView.setOnMapReadyListener(OnMapReadyListener {
            //todo 맵 로딩 완료 후 구현

            Log.d("마커", "마커")
            tmapView.setCenterPoint( // 화면 출력시, 위도,경도와 지도 위치를 설정
                36.90075062,
                126.7184702
            )

            //TODO: 마커추가
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.poi_dot) // 마커 이미지 로드
            val markerItem = TMapMarkerItem() //마커 생성
            markerItem.id = "marker1" // id 반드시 필요
            markerItem.icon = bitmap // 아이콘 지정
            markerItem.setPosition(0.5f, 0.5f) // 마커의 중심점을 중앙, 하단으로 설정
            val tMapPoint1 = TMapPoint(36.90075062, 126.7184702)  // 생성될 마커 좌표
            markerItem.tMapPoint = tMapPoint1 // 마커 좌표 설정
            markerItem.name = "test" // 마커의 타이틀 지정
            tmapView.addTMapMarkerItem(markerItem) // 지도에 마커 추가
        })


        // TODO: 사용자 GPS 정보 받기
    }


}