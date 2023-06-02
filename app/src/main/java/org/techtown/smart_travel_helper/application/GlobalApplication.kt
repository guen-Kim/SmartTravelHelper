package org.techtown.smart_travel_helper.application

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.kakaomobility.knsdk.KNRoutePriority
import com.kakaomobility.knsdk.KNSDK
import com.kakaomobility.knsdk.KNSDKDelegate
import com.kakaomobility.knsdk.trip.kntrip.KNTrip
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * LifecycleObserver, ProcessLifecycleOwner
 * 앱이 Foreground 상태, Background 상태 감지 및 콜백
 * **/


class GlobalApplication : Application(), LifecycleEventObserver {

    /* 전역 객체 */
    companion object {
        lateinit var instance: GlobalApplication
            private set // 변수이지만 set 불가 설정

        lateinit var knsdk: KNSDK

        // ConcurrentHashMap : HashMap을 thread-safe 하도록 만든 클래스, key, value에 null을 허용하지 않음
        lateinit var dataHolder: Map<String, Any> // 전역 데이터 저장
        lateinit var handler : Handler // 전역 데이터 넣을때 사용.

    }

    override fun onCreate() {
        super.onCreate()

        dataHolder = ConcurrentHashMap()
        instance = this


        /** 1. kakaoNaviSDK 초기화: 가장 먼저 어플리케이션 컨텍스트와 앱 데이터 경로를 셋팅 **/
        // 2. 는 퍼미션, 포그라운드 서비스는 퍼미션 필요없징?
        knsdk = KNSDK.apply {
            //  파일 경로 : data/data/com.kakaomobility.knsample/files/KNSample
            install(instance, "$filesDir/KNSample")

            /** 2. 비정상 종료 주행기록이 있는 경우 알려주는 델리게이트 등록 **/

            delegate = object : KNSDKDelegate {
                override fun knsdkFoundUnfinishedTrip(
                    aTrip: KNTrip,
                    aPriority: KNRoutePriority,
                    aAvoidOptions: Int
                ) {
                    //TODO: 경로 남아 있음


                }

                override fun knsdkNeedsLocationAuthorization() {

                    //TODO: 퍼미션 관련 이상

                }
            }
        }
        handler = Handler(Looper.getMainLooper())
    }


    // UUID: 고유한 키를 간단하게 생성해서 사용할 수 있기 떄문에 사용
    fun putDataHolder(data: KNTrip): String? {
        (dataHolder as ConcurrentHashMap).clear() // 데이터 내용 삭제
        val dataHolderId = UUID.randomUUID().toString()
        (dataHolder as ConcurrentHashMap).put(dataHolderId, data)
        return dataHolderId
    }

    fun getDataHolder(key: String?): KNTrip? {
        val KNTrip = (dataHolder.get(key) as KNTrip) // String to Object
        (dataHolder as ConcurrentHashMap).clear() // 데이터 내용 삭제
        return KNTrip
    }



//================================================================================
    /** override Lifecycle for Application **/

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) { // CREATED -> DESTROYED
            //애플리케이션이 종료되면 해당 정보를 KNSDK에 전달합니다. 애플리케이션이 종료되는 시점에 주행 중인 경로가 있으면 해당 상태 값과 경로를 저장
            knsdk.handleWillTerminate()
        } else if (event == Lifecycle.Event.ON_STOP) { // STARTED -> CREAETED
            //애플리케이션이 백그라운드(Background) 상태가 되면 해당 정보를 KNSDK에 전달
            knsdk.handleDidEnterBackground()
        } else if (event == Lifecycle.Event.ON_START) { // CREATE -> STARTED
            //애플리케이션이 포그라운드(Foreground) 상태가 되면 해당 정보를 KNSDK에 전달
            knsdk.handleWillEnterForeground()
        }
    }

}