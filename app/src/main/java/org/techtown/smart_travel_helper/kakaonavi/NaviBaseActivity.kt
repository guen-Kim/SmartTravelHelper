package org.techtown.smart_travel_helper.kakaonavi

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.kakaomobility.knsdk.*
import com.kakaomobility.knsdk.common.gps.KN_DEFAULT_POS_X
import com.kakaomobility.knsdk.common.gps.KN_DEFAULT_POS_Y
import com.kakaomobility.knsdk.common.objects.KNError
import com.kakaomobility.knsdk.common.objects.KNPOI
import com.kakaomobility.knsdk.common.objects.KNSearchPOI
import com.kakaomobility.knsdk.common.util.FloatPoint
import com.kakaomobility.knsdk.common.util.IntPoint
import com.kakaomobility.knsdk.trip.knrouteconfiguration.KNRouteConfiguration
import com.kakaomobility.knsdk.trip.kntrip.KNTrip
import com.kakaomobility.knsdk.trip.kntrip.knroute.KNRoute
import com.kakaomobility.knsdk.ui.utils.getAddressWithReverseGeocodeResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.techtown.smart_travel_helper.application.GlobalApplication
import org.techtown.smart_travel_helper.ui.MainActivity

// Request route coroutine
private val job = Job();
private val scope = CoroutineScope(Dispatchers.Main + job);

abstract class NaviBaseActivity : AppCompatActivity(), OnCompleteSearchforNavi {

    // 현좌표
    private lateinit var searchPos: IntPoint

    // 검색된 좌표
    private val addresses: ArrayList<KNSearchPOI> = arrayListOf()

    private var knTrip: KNTrip? = null
    private var knRoutes: MutableList<KNRoute>? = null
    private var knDestination: KNPOI? = null
    private var knAvoidOption: Int = 0
    private var knRouteOption: KNRoutePriority = KNRoutePriority.KNRoutePriority_Recommand


    open fun initialize() {
        // 상태바 색상 설정
        // setStatusBarColor(getColor(applicationContext, ))


        // 화면을 켜진 상태로 유지
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    protected fun setStatusBarColor(aIsFullScreen: Boolean = true) {
        if (aIsFullScreen) {
            @Suppress("DEPRECATION")
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                window.insetsController?.let{
//                    it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//                    it.hide(WindowInsets.Type.systemBars())
//                }
//            } else {
            // lean back 모드: 사용자가 동영상을 시청할 때와 같이 화면과 거의 상호작용하지 않을 때 사용할 수 있는 전체 화면 환경 설정
            //lean back 모드를 사용 설정
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//            }
        }

        //상태바  색상
        //window.statusBarColor = aId

        supportActionBar?.hide()
    }

    protected fun getKey(): String?{
        return GlobalApplication.instance.putDataHolder(knTrip!!)
    }

    /* 내비게이션 경로 요청*/
    private fun reqRoute(
        aDestination: KNPOI?,
        aWayPoints: MutableList<KNPOI>?,
        aRouteOption: KNRoutePriority
    ) {
        //
        route(aDestination, aWayPoints, knAvoidOption, aRouteOption) { aError, aTrip, aRoutes ->
            if (aError != null) {
                finish()
            } else {
                knTrip = aTrip // makeTripWithStart()에서 생성된 안내 객체 초기화 (1)
                knRoutes = aRoutes
                knAvoidOption = knRoutes?.get(0)!!.avoidOptions
                //TODO: 정상완료 콜백
                onCompleteSearch()
            }
        }
    }


    /*  KNtrip 생성   */
    protected fun route(
        aDestination: KNPOI?,
        aWayPoints: MutableList<KNPOI>?,
        aAvoidOption: Int,
        aRouteOption: KNRoutePriority,
        aCompletion: ((KNError?, KNTrip?, MutableList<KNRoute>?) -> Unit)?
    ) {
        scope.launch {

            // 목적지가 있다면
            if (aDestination != null) {

                // 사용자 GPS 가져와 pos에 저장
                val pos = KNSDK.sharedGpsManager()?.recentGpsData?.pos ?: FloatPoint(
                    KN_DEFAULT_POS_X.toFloat(), KN_DEFAULT_POS_Y.toFloat()
                )

                // KATEC 좌표에 해당하는 법정동 주소를 반환
                KNSDK.reverseGeocodeWithPos(IntPoint(pos)) { aReverseGeocodeError, _, aDoName, aSiGunGuName, aDongName ->

                    // 에러 없다면 가져온 GPS 값 네이밍 "현위치"로 하여 address 변수에 저장
                    val address = if (aReverseGeocodeError != null) {
                        "현위치"
                    } else {
                        // 함수 없어 네이밍 읽어보면 대충 추론가능
                        getAddressWithReverseGeocodeResult(aDoName, aSiGunGuName, aDongName)
                            ?: "현위치"
                    }

                    // 출발지. 사용자 위치 POI 생성 naming "현위치"
                    val start = KNPOI(address, pos.x.toInt(), pos.y.toInt(), address)
                    // 목적지.
                    val goal = KNPOI(
                        aDestination.name,
                        aDestination.pos.x,
                        aDestination.pos.y,
                        aDestination.address
                    )

                    // 하나의 경로를 구성하는 정보의 집합체로 Guidance 생성
                    // 출발지, 목적지, 경유지 목록을 넘겨준다.
                    GlobalApplication.knsdk.makeTripWithStart(
                        start, // 출발지
                        goal,   // 목적지
                        if (aWayPoints != null && aWayPoints.size > 0) aWayPoints else null // 경유지
                    ) { aError, aTrip ->
                        // 에러 발생한 경우
                        if (aError != null) {
                            aCompletion?.invoke(aError, null, null)

                            // 에러 없는 경우
                        } else {
                            // 경로 설정시 환경 설정
                            val routeConfig = KNRouteConfiguration(
                                aCarType = KNCarType.KNCarType_1,
                                aFuel = KNCarFuel.KNCarFuel_Gasoline,
                                aUseHipass = false,
                                aCarWidth = -1,
                                aCarHeight = -1,
                                aCarLength = -1,
                                aCarWeight = -1)
                            // 경로 설정시 환경 설정 초기화
                            aTrip?.setRouteConfig(routeConfig)

                            // 경로옵션 및 제외옵션에 따른 경로 정보인 KNRoute의 List 반환
                            aTrip?.routeWithPriority(
                                aRouteOption,
                                aAvoidOption
                            ) { aError2, aRoutes ->
                                if (aError2 != null) {
                                    // 에러 출력시
                                    Log.d("NaviBaseActity", " 경로 요청 실패")
                                } else {
                                    // route 호출시 구현된 aCompletion 메서드 호출
                                    aCompletion?.invoke(null, aTrip, aRoutes)
                                }
                            }
                        }
                    }
                }// end : reverseGeocodeWithPos
            } else {
                // 목적지가 없다면
                // 안전운행 모드로 실행(기본적인 도로상황 정보 준다. 속도, 어린이 구역등..)
                aCompletion?.invoke(null, null, null)
            }
        }
    }


    /*현재 위치에서 키워드 장소 검색*/
    public fun reqSearchWithType() {

        // 현재 거리 좌표값 요청하기
        GlobalApplication.knsdk.sharedGpsManager()!!.recentGpsData.apply {
            searchPos = IntPoint(pos.x.toInt(), pos.y.toInt())
        }


        // 검색 요청
        // KNSearchReqType_1 검색 유형: address + place
        GlobalApplication.knsdk.reqSearch(
            "휴게소", 1, 1,
            KNSearchReqType.KNSearchReqType_1, searchPos.x, searchPos.y
        ) { aError, aResult ->
            if (aError != null) {
                Log.d("${aError.code}", " ${aError.msg}")
            } else {
                // 요청 성공시
                Handler(Looper.getMainLooper()).post {
                    // FragmentSearchAdapter 라면
                    aResult?.let {
                        //결과 널이 아니면
                        it.addressResult?.let { result ->
                            Log.d("리스너", "검색결과 size" + result.poiList?.size.toString());

                            //setAddressesData(result)
                        }

                        it.placeResult?.let { result ->
                            // 여기에 반환됨, 검색 타입 떄문에그런가?
                            Log.d("리스너", "검색결과 size" + result.poiList?.size.toString());
                            //setPlacesData(result)
                            with(result) {

                                poiList?.let {
                                    if (!it.isNullOrEmpty()) {
                                        addresses.addAll(it.toTypedArray())
                                        val poi = addresses[0]
                                        // 목표지점
                                        knDestination =
                                            KNPOI(poi.name, poi.pos.x, poi.pos.y, poi.address)
                                        Log.d(
                                            "리스너",
                                            addresses[0].address + ", " + addresses[0].name + ", " + addresses[0].rnAddress
                                        );

                                        // 경로 요청(경유지 없음)
                                        reqRoute(
                                            knDestination,
                                            null,
                                            KNRoutePriority.KNRoutePriority_Recommand
                                        )


                                    }
                                }


                            }


                        }
                    }
                }
            }
        }
    }



}