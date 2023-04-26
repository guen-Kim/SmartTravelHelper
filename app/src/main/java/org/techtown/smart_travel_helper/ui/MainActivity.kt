package org.techtown.smart_travel_helper.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.skt.tmap.*
import com.skt.tmap.overlay.TMapMarkerItem
import kotlinx.coroutines.launch
import org.techtown.smart_travel_helper.R
import org.techtown.smart_travel_helper.TMAP_API_KEY
import org.techtown.smart_travel_helper.UTF8_URL_REST_AREA
import org.techtown.smart_travel_helper.databinding.ActivityMainBinding
import org.techtown.smart_travel_helper.model.SearchPoi
import org.techtown.smart_travel_helper.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


/**
 * https://youngest-programming.tistory.com/371 리팩토링 참고
 * */

class MainActivity : AppCompatActivity() {


    lateinit var binding: ActivityMainBinding
    lateinit var tMapView: TMapView
    lateinit var image: Bitmap
    lateinit var imageBolloon: Bitmap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // binding data
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        //tmap 초기화
        setTMapAuthAndInit()

        image = createMarkerIcon(R.drawable.poi_dot)

        // 지도 생성 하기, 비동기 처리
        tMapView.setOnMapReadyListener(TMapView.OnMapReadyListener {
            //todo 맵 로딩 완료 후 구현
            val xPoint = 37.59225739
            val yPoint = 126.7683878

            tMapView.setCenterPoint(xPoint, yPoint) // 화면 출력시, 위도,경도와 지도 위치를 설정


            searchPOI() // 검색된 마커 추가

        })


    }

    private fun setTMapAuthAndInit() {
        tMapView = TMapView(this) // t맵 동적생성
        binding.tMapContainer.addView(tMapView) // 자식 뷰 (Tmap) 추가
        // 인증키 요청
        tMapView.setSKTMapApiKey(TMAP_API_KEY)
    }

    private fun createMarkerIcon(image : Int): Bitmap {
        imageBolloon = BitmapFactory.decodeResource(resources,image)
        imageBolloon = Bitmap.createScaledBitmap(imageBolloon, 100, 100, false);
        return imageBolloon
    }


    private fun searchPOI() {
            val service = ApiClient.create()
            //Tmap api 통신, 메인스레드 비동기 호출 callback 매커니즘으로 응답처리
            service.getSearchROI(UTF8_URL_REST_AREA, "10", TMAP_API_KEY).enqueue(object: Callback<SearchPoi> {
                override fun onResponse(call: Call<SearchPoi>, response: Response<SearchPoi>) {

                    if(response.isSuccessful.not()) return
                    response.body()?.let {

                        val poiList = it.searchPoiInfo.pois.poi
                        Log.d("poi count", it.searchPoiInfo.count)

                        for (poi in poiList) {
                            val markerItem = TMapMarkerItem() //마커 생성
                            markerItem.id = poi.id // id 반드시 필요
                            val tMapPoint =

                                TMapPoint(poi.frontLat.toDouble(), poi.frontLon.toDouble())  // 생성될 마커 좌표
                            markerItem.tMapPoint = tMapPoint // 마커 좌표 설정
                            markerItem.name = poi.name // 마커의 타이틀 지정
                            markerItem.icon = image // 아이콘 지정
                            setBalloonView(markerItem, markerItem.name, poi.lowerAddrName)
                            tMapView.addTMapMarkerItem(markerItem) // 지도에 마커 추가
                        }

                    }

                }

                override fun onFailure(call: Call<SearchPoi>, t: Throwable) {
                    //TODO: 실패시

                }

            })


    }


    private fun setBalloonView(marker: TMapMarkerItem, title: String, address: String) {
        marker.canShowCallout = true
        if (marker.canShowCallout) {
            marker.calloutTitle = title
            marker.calloutSubTitle = address
//            val bitmap = createMarkerIcon(R.drawable.i_go_sel)
//            marker.calloutRightButtonImage = bitmap

        }
    }
}