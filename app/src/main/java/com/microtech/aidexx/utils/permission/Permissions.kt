package com.microtech.aidexx.utils.permission

import android.Manifest
import android.app.Activity
import android.os.Build
import com.microtech.aidexx.R

/**
 *@date 2023/2/10
 *@author Hugh
 *@desc 权限组定义
 */
object Permissions {

    val CallPhone = arrayOf(Manifest.permission.CALL_PHONE)

    val CameraAndStorage = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    val Storage = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    val Camera = arrayOf(
        Manifest.permission.CAMERA
    )

    val Bluetooth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }

    /**
     * 获取权限对应的功能描述，需要维护更新
     */
    fun getFuncToPermission(context: Activity, permissions: Array<String>): String {
        if (CallPhone.contentEquals(permissions)) {
            return context.getString(R.string.phone_call)
        } else if (Camera.contentEquals(permissions)) {
            return context.getString(R.string.camera)
        } else if (Storage.contentEquals(permissions)) {
            return context.getString(R.string.album_features)
        } else if (CameraAndStorage.contentEquals(permissions)) {
            return context.getString(R.string.camera) + context.getString(R.string.txt_protocal_and) + context.getString(
                R.string.album_features
            )
        } else if (Bluetooth.contentEquals(permissions)) {
            return context.getString(R.string.location)
        }
        return ""
    }
}