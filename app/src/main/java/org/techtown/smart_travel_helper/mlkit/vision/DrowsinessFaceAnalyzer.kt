package org.techtown.smart_travel_helper.mlkit.vision


import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.constraintlayout.core.widgets.Rectangle
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*

/**
 *  https://github.com/googlesamples/mlkit/blob/master/android/vision-quickstart/app/src/main/java/com/google/mlkit/vision/demo/kotlin/VisionProcessorBase.kt
 * */


class DrowsinessFaceAnalyzer(private val view: PreviewView) : ImageAnalysis.Analyzer {

    // dadetection 인스턴스
    private val detector: FaceDetector by lazy {
        //옵션 내용: https://developers.google.com/ml-kit/vision/face-detection/android?hl=ko
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)// 렌드마크인식
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL) // 분류, 얼굴을 '웃고 있음' 및 '눈을 뜸'과 같은 카테고리로 분류할 것인지 여부입니다.
            .build()
        FaceDetection.getClient(options)
    }


    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image ?: return

        if (mediaImage != null) {
            val imageRotationDegrees = image.imageInfo.rotationDegrees
            val imageWidth = mediaImage.width
            val imageHeight = mediaImage.height

            val inputImage = InputImage.fromMediaImage(mediaImage, imageRotationDegrees)

            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    // Process detected faces here
                    for (face in faces) {
                        val bounds = face.boundingBox
                        val left = bounds.left.toFloat() / imageWidth.toFloat()
                        val top = bounds.top.toFloat() / imageHeight.toFloat()
                        val right = bounds.right.toFloat() / imageWidth.toFloat()
                        val bottom = bounds.bottom.toFloat() / imageHeight.toFloat()
                        Log.d("faceData_left: ", left.toString())
                        Log.d("faceData_top: ", top.toString())
                        Log.d("faceData_right: ", right.toString())
                        Log.d("faceData_bottom: ", bottom.toString())

                        //TODO: 얼굴 감지 후, 결과 시각화
                        // In the success listener of detector.process():
                        // https://medium.com/hongbeomi-dev/mlkit-%EB%9D%BC%EC%9D%B4%EB%B8%8C%EB%9F%AC%EB%A6%AC%EC%99%80-camerax%EB%A5%BC-%EC%82%AC%EC%9A%A9%ED%95%98%EC%97%AC-vision-%EC%95%B1-%EB%A7%8C%EB%93%A4%EA%B8%B0-ad657e2e81e1



                    }
                }
                .addOnFailureListener { ex ->
                    Log.e("analyzer", "Error detecting faces: $ex")
                }
                .addOnCompleteListener {
                    image.close()
                }
        } else {
            image.close()
        }
    }
}



