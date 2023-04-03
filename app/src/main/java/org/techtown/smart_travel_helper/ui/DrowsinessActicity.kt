package org.techtown.smart_travel_helper.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import org.techtown.smart_travel_helper.*
import org.techtown.smart_travel_helper.camerax.CameraManager
import org.techtown.smart_travel_helper.databinding.ActivityDrowsinessDetectionBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class DrowsinessActicity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var binding: ActivityDrowsinessDetectionBinding
    private lateinit var layout: View
    lateinit var cameraExecutor : ExecutorService
    private lateinit var cameraManager: CameraManager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // binding data
        binding = DataBindingUtil.setContentView(
            this, R.layout.activity_drowsiness_detection
        )
        createCameraManager()
        layout = binding.root
        cameraExecutor = Executors.newSingleThreadExecutor()

        // 권한 요청
        checkPermission()


    }

    private fun checkPermission() {
        // Camera permission 권한이 있는지 확인
        if (checkSelfPermissionCompat(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Camera permission 이 이미 수락되어 있다면, start camera preview
            layout.showSnackbar(R.string.camera_permission_granted, Snackbar.LENGTH_SHORT)
            cameraManager.startCamera()


        } else {
            // Camera permission 수락되어 있지 않다면, request
            requestCameraPermission()
        }
    }

    // 카메라 권한 요청
    private fun requestCameraPermission() {
        if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.CAMERA)) {
            // 사용자가 명시적으로 권한을 거부한 경우, Camera Permission 이 필요한 근거 설명
            // 버튼이 있는 스낵바를 표시, 누락된 권한을 요청한다.
            layout.showSnackbar(
                R.string.camera_access_required,
                Snackbar.LENGTH_INDEFINITE,
                R.string.ok
            ) {
                // Camera Permission request.  The result will be received in onRequestPermissionResult().
                requestPermissionsCompat(
                    arrayOf(Manifest.permission.CAMERA),
                    PERMISSION_REQUEST_CAMERA
                )
            }
        } else { // 사용자가 권한 요청을 처음 보거나, 다시 묻지 않음 선택한 경우
            layout.showSnackbar(R.string.camera_permission_not_available, Snackbar.LENGTH_SHORT)
            // Camera Permission request. The result will be received in onRequestPermissionResult().
            requestPermissionsCompat(arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
        }
    }


    // 권한 요청 결과 응답
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            // Request for camera permission.
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
                //startCamerePreview()
                cameraManager.startCamera()


            } else {
                // Permission request was denied.
                layout.showSnackbar(R.string.camera_permission_denied, Snackbar.LENGTH_SHORT) // * 실행 안됨.
            }
        }
    }

    private fun createCameraManager() {
        cameraManager = CameraManager(
            this,
            binding.previewView,
            this,
            binding.graphicOverlayFinder
        )
    }
}