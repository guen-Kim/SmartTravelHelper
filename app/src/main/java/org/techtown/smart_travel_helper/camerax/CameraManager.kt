package org.techtown.smart_travel_helper.camerax

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import org.techtown.smart_travel_helper.mlkit.vision.face_detection.FaceContourDetectionProcessor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraManager(
    private val context: Context,
    private val finderView: PreviewView,
    private val lifecycleOwner: LifecycleOwner,
    private val graphicOverlay: GraphicOverlay,
    private val activity : Activity
) {

    private var preview: Preview? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalyzer: ImageAnalysis? = null


    lateinit var cameraExecutor: ExecutorService

    var cameraSelectorOption = CameraSelector.LENS_FACING_FRONT

    init {
        createNewExecutor()
    }

    private fun createNewExecutor() {
        cameraExecutor = Executors.newSingleThreadExecutor()
    }


    private fun setFaceAnalyzer(): ImageAnalysis.Analyzer {
        return FaceContourDetectionProcessor(graphicOverlay, activity)

    }

    private fun setCameraConfig(
        cameraProvider: ProcessCameraProvider?,
        cameraSelector: CameraSelector
    ) {
        try {
            // Provider 설정; camera 에게 useCaer(PreviewView) 가 데이터 수신 준비가 되었음 알림
            preview?.setSurfaceProvider(finderView.surfaceProvider)

            // CameraX UseCase 와 lifecycle 연동 삭제하고  현재 열려 있는 모든 카메라가 닫음.
            cameraProvider?.unbindAll()

            // Binds the collection of UseCase to a LifecycleOwner.
            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer
            )

        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
        }
    }


    fun startCamera() {
        // CameraProvider singleton
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)


        //CameraProvider availability 확인
        cameraProviderFuture.addListener(
            Runnable {
                cameraProvider = cameraProviderFuture.get()
                preview = Preview.Builder().build()

                imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, setFaceAnalyzer())
                    }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(cameraSelectorOption)
                    .build()


                setCameraConfig(cameraProvider, cameraSelector) // "Use case binding failed" 바인딩 에러 발생 될 수 있으므로 함수로 따로 뺌

            }, ContextCompat.getMainExecutor(context)
        )
    }




    companion object {
        private const val TAG = "CameraXBasic"
    }

}