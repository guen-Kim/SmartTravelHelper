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
            // TODO: face data, ROI 반복 그리기
            val faceGraphic = FaceContourGraphic(graphicOverlay, it, rect)
            graphicOverlay.add(faceGraphic)

            //TODO: face data, eyes open / close Probability
            var leOpen = it.leftEyeOpenProbability ?: 5.0
            var reOepn = it.rightEyeOpenProbability ?: 5.0
            Log.d("eye", "왼쪽: ${leOpen}, 오른쪽: ${reOepn}")

            with(EyeTracker) {
                if ((reOepn.toFloat() < 0.3f) && (leOpen.toFloat() < 0.3f)) {
                    //사용자가 눈을 감았다면
                    if (isClosed) {
                        // 이전에도 눈을 감았다면
                        val endTime = System.currentTimeMillis()
                        if (alarmTime <= endTime) {
                            //TODO: new Thread run 알람
                            Log.d("eye", "눈뜨세요.")
                        }
                    } else {
                        // 이전에도 눈을 감지 않았다면
                        startTime = System.currentTimeMillis()
                        setAlarmTime(startTime, limitTime)
                        isClosed = true
                    }
                } else {
                    //사용자가 눈을 감지 않았다면
                    isClosed = false
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