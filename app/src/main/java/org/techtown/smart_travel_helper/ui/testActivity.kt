package org.techtown.smart_travel_helper.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import org.techtown.smart_travel_helper.PERMISSION_REQUEST_CODE
import org.techtown.smart_travel_helper.R
import org.techtown.smart_travel_helper.databinding.ActivityMainBinding
import org.techtown.smart_travel_helper.showSnackbar

class testActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // binding data
        binding = DataBindingUtil.setContentView(
            this, R.layout.activity_main
        )


        OnCheckPermission();
    }


    private fun OnCheckPermission() {
        // 권한이 있는지 없는지 확인
        // val locationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        // 앱에 해당 권한이 하나라도 없다면
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) !=  PackageManager.PERMISSION_GRANTED
        ) {

            // 교육용 UI가 필요한 유저인가?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            ) {
                // true, 이미 허용되있던 권한을 '허용하지 않음' 으로 돌린 경우,해당 권한을 명시적으로 거부한 경험이 있는 경우
                Toast.makeText(this, "앱 기능을 실행하기 위해선 카메라 액세스 권한과 위치 액세스 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE) // 권한 요청

            } else {
                // false, 설치하고 요청 메시지를 한 번 도 못 받아서 요청 대화상자 선택 자체를 못한 경우
                Toast.makeText(this, "앱 기능을 실행하기 위해선 카메라 액세스 권한과 위치 액세스 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE) // 권한 요청
            }
        } // else: 앱에 기능을 실행하기위한 모든 권한이 있는 경우

    }

    // 결과요청 처리 결과 수신
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    var isAllGranted = true
                    // 요청한 권한 허용/거부 상태 한번에 체크
                    for (grant in grantResults) {
                        if (grant != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false
                            break;
                        }
                    }

                    // 요청한 권한을 모두 허용했음.
                    if (isAllGranted) {
                        // 다음 step으로
                    } else {
                        // 허용하지 않은 권한이 있음. 필수권한/선택권한 여부에 따라서 별도 처리를 해주어야 함.
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                            || !ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.CAMERA
                            )
                        ) {
                            // 다시 묻지 않기 체크하면서 권한 거부 되었음.
                            alertAuthoritySetting()

                        } else {
                            // 접근 권한 거부하였음.
                        }
                    }
                }
            }
        }
    }

    private fun alertAuthoritySetting() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("권한 설정")
        builder.setMessage("앱에서 요청한 권한이 없으면 기능을 사용할 수 없습니다. 권한을 설정해주세요")
        builder.setCancelable(false)
        builder.setPositiveButton("예", DialogInterface.OnClickListener { dialogInterface, i ->
            // 사용자가 직접 권한 설정 하도록 유도
            try {
                val intent =
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + packageName))
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
                startActivity(intent)
            }
        })
        builder.setNegativeButton("아니오", DialogInterface.OnClickListener { dialogInterface, i ->
            //finish()
        })
        builder.show()
    }


    override fun onRestart() {
        super.onRestart()
        var bChack = false
        if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.CAMERA)
            || PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            // 권한 설정 화면에서 권한 설정하지 않고 다시 화면으로 복귀한 경우
            bChack = true
        }
        if (bChack) {
            Toast.makeText(this, "요청된 모든 권한을 설정해야 합니다.", Toast.LENGTH_SHORT).show()
            // finish()
        } else {
            Toast.makeText(this, "기능을 사용하실 수 있습니다.", Toast.LENGTH_SHORT).show()
        }
    }


    companion object {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION, // 대략적인 위치(이것만 허용해도 기능 동작함)
            Manifest.permission.ACCESS_FINE_LOCATION,  // 정확한 위치
            Manifest.permission.CAMERA
        )
    }

}

