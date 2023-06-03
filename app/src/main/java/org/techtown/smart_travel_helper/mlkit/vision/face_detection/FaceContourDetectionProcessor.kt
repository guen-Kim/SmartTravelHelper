package org.techtown.smart_travel_helper.mlkit.vision.face_detection

import android.app.Activity
import android.graphics.Rect
import android.media.MediaPlayer
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.*
import org.techtown.smart_travel_helper.camerax.BaseImageAnalyzer
import org.techtown.smart_travel_helper.camerax.GraphicOverlay
import org.techtown.smart_travel_helper.common.EyeTracker
import org.techtown.smart_travel_helper.common.EyeTracker.isClosed
import org.techtown.smart_travel_helper.ui.DrowsinessActicity
import java.io.IOException


private val job = Job();
private val scope = CoroutineScope(Dispatchers.Default + job);


class FaceContourDetectionProcessor(
    private val view: GraphicOverlay,
    private val activity: Activity
) :
    BaseImageAnalyzer<List<Face>>() {

    private val drowsinessActicity: DrowsinessActicity = (activity as DrowsinessActicity)

    //옵션 내용: https://developers.google.com/ml-kit/vision/face-detection/android?hl=ko
    private val realTimeOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE) // for eyeContour
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL) // for eyeOpenProbability
        .build()

    private val detector = FaceDetection.getClient(realTimeOpts)
    var leOpen: Float = 5.0f
    var reOepn: Float = 5.0f

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


        //졸음감지 패턴1 default - 눈 (길안내 or 경고음)
        startDrowsinessPattern_eye(
            results,
            graphicOverlay,
            rect,
            drowsinessActicity.PATHGUIDE,
            drowsinessActicity.WARRINGSOUND
        )




        if (drowsinessActicity.WARRINGSOUND) {
            // 졸음감지 패턴2 - 고개(경고음)
            startDrowsinessPattern_headDown(results)
        }


        // draw 요청
        graphicOverlay.postInvalidate()
    }


    private fun startDrowsinessPattern_headDown(results: List<Face>) {
        with(EyeTracker) {
            if (results.isEmpty()) {
                // 고개를 떨굼
                if (timeAdjustmentFactor_headDown > 0) {
                    var endTime = System.currentTimeMillis()
                    if (endTime <= headDownAlarmTime) {

                    } else {
                        //Todo:알람
                        drowsinessActicity.mediaPlayerB.start()
                    }
                } else {
                    timeAdjustmentFactor_headDown = 1
                    headDownStartTime = System.currentTimeMillis()
                    headDownAlarmTime = headDownLimitTime + headDownStartTime
                }

            } else {
                //고개 안떨굼
                if (drowsinessActicity.mediaPlayerB.isPlaying) {
                    drowsinessActicity.mediaPlayerB.pause()
                }
                timeAdjustmentFactor_headDown = 0
            }
        }

    }


    private fun startDrowsinessPattern_eye(
        results: List<Face>,
        graphicOverlay: GraphicOverlay,
        rect: Rect,
        guide: Boolean,
        alarm: Boolean
    ) {
        if (results.isNotEmpty()) {

            val faceGraphic = FaceContourGraphic(graphicOverlay, results[0], rect, isClosed)
            graphicOverlay.add(faceGraphic)

            //TODO: face data, eyes open / close Probability
            // TODO: face data, ROI 반복 그리기
            leOpen = results[0].leftEyeOpenProbability ?: 0.5f
            reOepn = results[0].rightEyeOpenProbability ?: 0.5f

            with(EyeTracker) {
                if ((reOepn < 0.3f) || (leOpen < 0.3f)) {
                    //사용자가 눈을 감았다면
                    if (isClosed && timeAdjustmentFactor <= 20) {
                        // 눈 2회 이상 눈을 감았다면
                        val endTime = System.currentTimeMillis()
                        if (alarmTime <= endTime) {
                            val faceGraphic =
                                FaceContourGraphic(graphicOverlay, results[0], rect, isClosed)
                            graphicOverlay.add(faceGraphic)


                            //TODO: 내비 or 알람
                            scope.launch(Dispatchers.Default) {
                                if (alarm) {
                                    //EyeTracker.alarmStart = false
                                    drowsinessActicity.mediaPlayerA.start()
                                }

                                if (EyeTracker.guideStart && guide) {
                                    EyeTracker.guideStart = false
                                    // guideStart 내비 종료시 true
                                    drowsinessActicity.reqSearchWithType()
                                }
                                if (EyeTracker.guideStart && guide && alarm) {
                                    EyeTracker.guideStart = false
                                    drowsinessActicity.reqSearchWithType()
                                    drowsinessActicity.mediaPlayerA.start()
                                }

                            }
                        } else {

                        }
                    } else {
                        // 눈 1회 감음
                        startTime = System.currentTimeMillis()
                        setAlarmTime(startTime, limitTime)
                        isClosed = true
                        val faceGraphic =
                            FaceContourGraphic(
                                graphicOverlay,
                                results.get(0),
                                rect,
                                false
                            ) // 1회 깜박임은 레드 박스 x
                        graphicOverlay.add(faceGraphic)
                        timeAdjustmentFactor++ // time 조정
                    }
                } else {
                    //사용자가 눈을 감지 않았다면
                    if (drowsinessActicity.mediaPlayerA.isPlaying) {
                        drowsinessActicity.mediaPlayerA.pause()
                    }
                    isClosed = false
                    alarmStart = true
                    timeAdjustmentFactor = 0
                    val faceGraphic =
                        FaceContourGraphic(graphicOverlay, results.get(0), rect, isClosed)
                    graphicOverlay.add(faceGraphic)

                }
            }


        }

    }


    override fun onFailure(e: Exception) {
        Log.w(TAG, "Face Detector failed.$e")
    }

    companion object {
        private const val TAG = "FaceDetectorProcessor"
    }
}