package org.techtown.smart_travel_helper.location


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

//https://developers.google.com/android/reference/com/google/android/gms/location/package-summary


class ClientFusedLocation(
    private val context: Context,
    private val listener: OnLocationUpdateListener // 사용할 액티비티에서 구현
) {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    init {
        initLocationClient()
        initLocationCallback()
    }

    // 통합 위치 제공자 초기화
    private fun initLocationClient() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        val locationRequest = LocationRequest.create()?.apply {
            interval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest!!)
        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {
            Log.d(TAG, "location client setting success")
        }

        task.addOnFailureListener {
            Log.d(TAG, "location client setting failure")
        }
    }


    /**마지막으로 알려진 위치 요청**/
    fun requestLastLocation() {
        //위치 권한 체크
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location ->
                listener.onLocationUpdated(location)
            }
        fusedLocationProviderClient.lastLocation.addOnFailureListener{
            Exception -> Log.d("test","null")
        }

    }

    /**위치 업데이트 요청 **/
    // requestLocationUpdates()으로 변경사항에 대한 Callback을 요청
    private fun initLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    listener.onLocationUpdated(location)
                    break
                }
            }
        }
    }

    fun startLocationUpdates() {
        //위치 권한 체크
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val locationRequest = LocationRequest.create()?.apply {
            interval = 5000 // 시간 간격
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY // 정밀도 최상
        }

        //
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,    // 필수 옵션 설정
            locationCallback,   // 호출될 콜백 함수
            Looper.getMainLooper()
        )
    }



    /**callback 등록 해제, 더 이상 위치 정보를 받을 필요가 없다면**/
    fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        private const val TAG = "FusedLocationManager"
    }
}