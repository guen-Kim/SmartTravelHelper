package org.techtown.smart_travel_helper.camerax

import android.graphics.Rect
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage

abstract class BaseImageAnalyzer<T> : ImageAnalysis.Analyzer {

    abstract val graphicOverlay: GraphicOverlay

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {


        val mediaImage = imageProxy.image

// https://developers.google.com/android/reference/com/google/mlkit/vision/face/FaceContour
        mediaImage?.let {
            detectInImage(InputImage.fromMediaImage(it, imageProxy.imageInfo.rotationDegrees))
                .addOnSuccessListener { results ->

                    onSuccess(
                        results,
                        graphicOverlay,
                        it.cropRect
                    )
                }
                .addOnFailureListener {
                    graphicOverlay.clear()
                    graphicOverlay.postInvalidate()
                    onFailure(it)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }


    }

    abstract fun stop()

    protected abstract fun detectInImage(image: InputImage): Task<T>

    protected abstract fun onSuccess(
        results: T,
        graphicOverlay: GraphicOverlay,
        rect: Rect
    )

    protected abstract fun onFailure(e: Exception)

}