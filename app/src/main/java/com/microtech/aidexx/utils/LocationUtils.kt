package com.microtech.aidexx.utils

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings


object LocationUtils {
    /**
     * 手机是否开启位置服务，如果没有开启那么所有app将不能使用定位功能
     */
    fun isLocationServiceEnable(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return gps || network
    }

    /**
     * 直接跳转至位置信息设置界面
     */
    fun enableLocationService(context: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        intent.putExtra("extra_prefs_show_button_bar", true)//是否显示button bar
        context.startActivity(intent)
    }
}