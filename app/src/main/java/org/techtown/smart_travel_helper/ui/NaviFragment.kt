package org.techtown.smart_travel_helper.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.kakaomobility.knsdk.KNCarFuel
import com.kakaomobility.knsdk.KNCarType
import com.kakaomobility.knsdk.KNRoutePriority
import com.kakaomobility.knsdk.KNSDK
import com.kakaomobility.knsdk.common.objects.KNError
import com.kakaomobility.knsdk.guidance.knguidance.*
import com.kakaomobility.knsdk.guidance.knguidance.citsguide.KNGuide_Cits
import com.kakaomobility.knsdk.guidance.knguidance.locationguide.KNGuide_Location
import com.kakaomobility.knsdk.guidance.knguidance.routeguide.KNGuide_Route
import com.kakaomobility.knsdk.guidance.knguidance.routeguide.objects.KNMultiRouteInfo
import com.kakaomobility.knsdk.guidance.knguidance.safetyguide.KNGuide_Safety
import com.kakaomobility.knsdk.guidance.knguidance.safetyguide.objects.KNSafety
import com.kakaomobility.knsdk.guidance.knguidance.voiceguide.KNGuide_Voice
import com.kakaomobility.knsdk.trip.kntrip.KNTrip
import com.kakaomobility.knsdk.trip.kntrip.knroute.KNRoute
import com.kakaomobility.knsdk.ui.component.MapViewCameraMode
import com.kakaomobility.knsdk.ui.view.KNNaviView
import com.kakaomobility.knsdk.ui.view.KNNaviView_GuideStateDelegate
import com.kakaomobility.knsdk.ui.view.KNNaviView_StateDelegate
import org.techtown.smart_travel_helper.R
import org.techtown.smart_travel_helper.application.GlobalApplication
import org.techtown.smart_travel_helper.common.EyeTracker
import org.w3c.dom.Text

class NaviFragment : Fragment(), KNNaviView_StateDelegate, KNNaviView_GuideStateDelegate,
    KNGuidance_GuideStateDelegate, KNGuidance_LocationGuideDelegate, KNGuidance_RouteGuideDelegate,
    KNGuidance_SafetyGuideDelegate, KNGuidance_VoiceGuideDelegate, KNGuidance_CitsGuideDelegate {

    lateinit var naviView: KNNaviView
    lateinit var endText: TextView
    lateinit var imageText: ImageView
    lateinit var imageLogo: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_navi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initView(view)




        naviView.stateDelegate = this@NaviFragment
        naviView.guideStateDelegate = this@NaviFragment
        naviView.useDarkMode = true
        naviView.mapViewMode = MapViewCameraMode.Bird
        naviView.sndVolume = 1.0f
        naviView.carType = KNCarType.KNCarType_1
        naviView.fuelType = KNCarFuel.KNCarFuel_Gasoline

        //RequestActivity에서 전달한 번들 저장
        val bundle = getArguments()
        //번들 안의 텍스트 불러오기
        val tripKey = bundle?.getString("key")

        //val key = getintent?.getStringExtra("tripKey")
        // 하나의 경로를 갖는 가이드 집합체
        var trip: KNTrip? = null
        if (!TextUtils.isEmpty(tripKey)) {
            trip = GlobalApplication.instance.getDataHolder(tripKey) // 가져옴
        }



        GlobalApplication.knsdk.sharedGuidance()?.apply {
            //  guidance delegate 등록
            guideStateDelegate = this@NaviFragment
            locationGuideDelegate = this@NaviFragment
            routeGuideDelegate = this@NaviFragment
            safetyGuideDelegate = this@NaviFragment
            voiceGuideDelegate = this@NaviFragment
            citsGuideDelegate = this@NaviFragment

            //KNPOI(name=망향휴게소 부산방향, pos=point info = [x: 327156 , y: 473001], address=충남 천안시 서북구 성거읍 요방리 121, rnAddress=충남 천안시 서북구 성거읍 돌다리길 23-37, guidePoints=null)
            naviView.initWithGuidance(
                this,
                trip,
                KNRoutePriority.KNRoutePriority_Distance,
                0
            )
        }

    }

    private fun initView(view: View) {
        naviView = view.findViewById<KNNaviView>(R.id.navi_view)
        endText = view.findViewById<TextView>(R.id.tv_end)
        imageText = view.findViewById<ImageView>(R.id.iv_text_logo)
        imageLogo = view.findViewById<ImageView>(R.id.iv_logo)

        endText.visibility = View.GONE
        imageText.visibility = View.GONE
        imageLogo.visibility = View.GONE





    }


    override fun onResume() {
        super.onResume()

        GlobalApplication.knsdk.handleWillEnterForeground()
    }

    override fun onPause() {
        super.onPause()

        GlobalApplication.knsdk.handleDidEnterBackground()
    }

    override fun onDestroy() {
        super.onDestroy()

        KNSDK.sharedGuidance()?.stop()
    }



    //  ---------------------------------------------------------------------------------------------------------------------------

    //  KNNaviView_StateDelegate
    //  ===========================================================================================================================

    override fun naviViewDidUpdateStatusBarColor(aColor: Int) {
        //setStatusBarColor(aColor)
    }

    override fun naviViewDidUpdateMapCameraMode(aCameraMode: MapViewCameraMode) {}

    override fun naviViewDidUpdateUseDarkMode(aMode: Boolean) {}

    override fun naviViewDidUpdateSndVolume(aVolume: Float) {
        //GlobalApplication.prefs.setSndVolume(aVolume)
    }

    //  ---------------------------------------------------------------------------------------------------------------------------

    //  KNNaviView_GuideStateDelegate
    //  ===========================================================================================================================

    override fun naviViewGuideEnded() {
        //todo: 내비종료
        naviView.mapComponent.mapView.onPause()
        KNSDK.sharedGuidance()?.stop()

        naviView.visibility = View.GONE
        endText.visibility = View.VISIBLE
        imageText.visibility = View.VISIBLE
        imageLogo.visibility = View.VISIBLE

        // naviView.setBackgroundResource(R.drawable.poi_dot)
        EyeTracker.guideStart = true // 내비 초기화
    }

    //  ---------------------------------------------------------------------------------------------------------------------------

    //  KNGuidance_GuideStateDelegate
    //  ===========================================================================================================================

    override fun guidanceGuideStarted(aGuidance: KNGuidance) {
        naviView.guidanceGuideStarted(aGuidance)
    }

    override fun guidanceCheckingRouteChange(aGuidance: KNGuidance) {
        naviView.guidanceCheckingRouteChange(aGuidance)
    }

    override fun guidanceRouteUnchanged(aGuidance: KNGuidance) {
        naviView.guidanceRouteUnchanged(aGuidance)
    }

    override fun guidanceRouteUnchangedWithError(aGuidnace: KNGuidance, aError: KNError) {
        naviView.guidanceRouteUnchangedWithError(aGuidnace, aError)
    }

    override fun guidanceOutOfRoute(aGuidance: KNGuidance) {
        naviView.guidanceOutOfRoute(aGuidance)
    }

    override fun guidanceRouteChanged(aGuidance: KNGuidance) {
        naviView.guidanceRouteChanged(aGuidance)
    }

    override fun guidanceGuideEnded(aGuidance: KNGuidance) {
        naviView.guidanceGuideEnded(aGuidance)
    }

    override fun guidanceDidUpdateRoutes(
        aGuidance: KNGuidance,
        aRoutes: List<KNRoute>,
        aMultiRouteInfo: KNMultiRouteInfo?
    ) {
        naviView.guidanceDidUpdateRoutes(aGuidance, aRoutes, aMultiRouteInfo)
    }


    //  ---------------------------------------------------------------------------------------------------------------------------

    //  KNGuidance_LocationGuideDelegate
    //  ===========================================================================================================================

    override fun guidanceDidUpdateLocation(
        aGuidance: KNGuidance,
        aLocationGuide: KNGuide_Location
    ) {
        naviView.guidanceDidUpdateLocation(aGuidance, aLocationGuide)
    }

    //  ---------------------------------------------------------------------------------------------------------------------------

    //  KNGuidance_RouteGuideDelegate
    //  ===========================================================================================================================

    override fun guidanceDidUpdateRouteGuide(aGuidance: KNGuidance, aRouteGuide: KNGuide_Route) {
        naviView.guidanceDidUpdateRouteGuide(aGuidance, aRouteGuide)
    }

    //  ---------------------------------------------------------------------------------------------------------------------------

    //  KNGuidance_SafetyGuideDelegate
    //  ===========================================================================================================================

    override fun naviViewGuideState(state: KNGuideState) {}


    override fun guidanceDidUpdateSafetyGuide(
        aGuidance: KNGuidance,
        aSafetyGuide: KNGuide_Safety?
    ) {
        naviView.guidanceDidUpdateSafetyGuide(aGuidance, aSafetyGuide)
    }

    override fun guidanceDidUpdateAroundSafeties(
        aGuidance: KNGuidance,
        aSafeties: List<KNSafety>?
    ) {
        naviView.guidanceDidUpdateAroundSafeties(aGuidance, aSafeties)
    }

    //  ---------------------------------------------------------------------------------------------------------------------------


    //  KNGuidance_VoiceGuideDelegate
    //  ===========================================================================================================================
    override fun shouldPlayVoiceGuide(
        aGuidance: KNGuidance,
        aVoiceGuide: KNGuide_Voice,
        aNewData: MutableList<ByteArray>
    ): Boolean {
        return naviView.shouldPlayVoiceGuide(aGuidance, aVoiceGuide, aNewData)
    }

    override fun willPlayVoiceGuide(aGuidance: KNGuidance, aVoiceGuide: KNGuide_Voice) {
        naviView.willPlayVoiceGuide(aGuidance, aVoiceGuide)
    }

    override fun didFinishPlayVoiceGuide(aGuidance: KNGuidance, aVoiceGuide: KNGuide_Voice) {
        naviView.didFinishPlayVoiceGuide(aGuidance, aVoiceGuide)
    }

    //  ---------------------------------------------------------------------------------------------------------------------------

    //  KNGuidance_CitsGuideDelegate
    //  ===========================================================================================================================

    override fun didUpdateCitsGuide(aGuidance: KNGuidance, aCitsGuide: KNGuide_Cits) {
        naviView.didUpdateCitsGuide(aGuidance, aCitsGuide)
    }


}