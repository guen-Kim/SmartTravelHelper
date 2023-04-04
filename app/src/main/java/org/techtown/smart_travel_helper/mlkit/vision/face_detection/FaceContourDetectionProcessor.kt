package org.techtown.smart_travel_helper.mlkit.vision.face_detection

import android.graphics.Rect
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import org.techtown.smart_travel_helper.camerax.BaseImageAnalyzer
import org.techtown.smart_travel_helper.camerax.GraphicOverlay
import org.techtown.smart_travel_helper.common.EyeTracker
import org.techtown.smart_travel_helper.common.EyeTracker.isClosed
import java.io.IOException

class FaceContourDetectionProcessor(private val view: GraphicOverlay) :
    BaseImageAnalyzer<List<Face>>() {

    //옵션 내용: https://developers.google.com/ml-kit/vision/face-detection/android?hl=ko
    private val realTimeOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL) // for eyeContour
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL) // for eyeOpenProbability
        .build()

    private val detector = FaceDetection.getClient(realTimeOpts)

    override val graphicOverlay: GraphicOverlay
        get() = view

    override fun detectInImage(image: InputImage): Task<List<Face>> {
        return detector.process(image)
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: $e")
        }
    }

    /**
     * 탐지결과
     * **/
    override fun onSuccess(
        results: List<Face>, // 탐지된 faces data
        graphicOverlay: GraphicOverlay,
        rect: Rect // 탐지된  face 영역 잘려진 사각형
    ) {
        // 이미 출력된 view 삭제
        graphicOverlay.clear()

        results.forEach {
            val faceGraphic = FaceContourGraphic(graphicOverlay, it, rect, isClosed)
            graphicOverlay.add(faceGraphic)

            //TODO: face data, eyes open / close Probability
            // TODO: face data, ROI 반복 그리기
            var leOpen = it.leftEyeOpenProbability ?: 5.0
            var reOepn = it.rightEyeOpenProbability ?: 5.0
            Log.d("eye", "왼쪽: ${leOpen}, 오른쪽: ${reOepn}")

            with(EyeTracker) {
                if ((reOepn.toFloat() < 0.3f) && (leOpen.toFloat() < 0.3f)) {
                    //사용자가 눈을 감았다면
                    if (isClosed && timeAdjustmentFactor >= 20) {
                        // 눈 2회 이상 눈을 감았다면
                        val endTime = System.currentTimeMillis()
                        if (alarmTime <= endTime) {
                            Log.d("timePass","${alarmTime}, ${endTime}")
                            val faceGraphic = FaceContourGraphic(graphicOverlay, it, rect, isClosed)
                            graphicOverlay.add(faceGraphic)
                            //TODO: new Thread run 알람
                            Log.d("timePass", "눈뜨세요.")
                        }
                    } else {
                        // 눈 1회 감음
                        startTime = System.currentTimeMillis()
                        setAlarmTime(startTime, limitTime)
                        isClosed = true
                        val faceGraphic = FaceContourGraphic(graphicOverlay, it, rect, false) // 1회 깜박임은 레드 박스 x
                        graphicOverlay.add(faceGraphic)
                        timeAdjustmentFactor++ // time 조정
                    }
                } else {
                    isClosed = false
                    //사용자가 눈을 감지 않았다면
                    timeAdjustmentFactor = 0
                    val faceGraphic = FaceContourGraphic(graphicOverlay, it, rect, isClosed)
                    graphicOverlay.add(faceGraphic)

                }
            }


        }
        // draw 요청
        graphicOverlay.postInvalidate()
    }

    override fun onFailure(e: Exception) {
        Log.w(TAG, "Face Detector failed.$e")
    }

    companion object {
        private const val TAG = "FaceDetectorProcessor"
    }
}