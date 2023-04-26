package org.techtown.smart_travel_helper

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

/*
* https://github.com/android/permissions-samples/blob/main/RuntimePermissionsBasicKotlin/Application/src/main/java/com/example/android/basicpermissions/util/AppCompatActivityExt.kt
*
* */

// 특정 권한이 PackageManager.PERMISSION_DENIED 인지, PackageManager.PERMISSION_GRANTED 인지 반환 한다.
fun AppCompatActivity.checkSelfPermissionCompat(permission: String) =
    ActivityCompat.checkSelfPermission(this, permission)

// 사용자가 권한 요청을 명시적으로 거부한 경우 true를 반환한다.
// 사용자가 권한 요청을 처음 보거나, 다시 묻지 않음 선택한 경우, 권한을 허용한 경우 false를 반환한다
fun AppCompatActivity.shouldShowRequestPermissionRationaleCompat(permission: String) =
    ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

// 사용자에게 명시적으로 권한을 요청한다.
// 안드로이드 시스템 표준 대화상자로 사용자에게 권한을 요구하며, 변경할 수 없다.
fun AppCompatActivity.requestPermissionsCompat(permissionsArray: Array<String>, requestCode: Int) {
    ActivityCompat.requestPermissions(this, permissionsArray, requestCode)
}
