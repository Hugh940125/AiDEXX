package com.microtech.aidexx.utils.mmkv

object MmkvManager {

    private const val UNIT_VERSION = "UNIT_VERSION"
    private const val UNIT_LOADED_APK_VERSION = "UNIT_LOADED_APK_VERSION"
    private const val UNIT_LATEST_UPDATE_TIME = "UNIT_LATEST_UPDATE_TIME"

    fun getUnitLatestUpdateTime() = MmkvUtil.decodeLong(UNIT_LATEST_UPDATE_TIME, 0)
    fun setUnitLatestUpdateTime(time: Long) = MmkvUtil.encodeLong(UNIT_LATEST_UPDATE_TIME, time)

    fun getUnitLoadedApkVersion() = MmkvUtil.decodeInt(UNIT_LOADED_APK_VERSION, 0)
    fun setUnitLoadedApkVersion(v: Int) = MmkvUtil.encodeInt(UNIT_LOADED_APK_VERSION, v)

    fun getUnitVersion() = MmkvUtil.decodeInt(UNIT_VERSION, 0)
    fun setUnitVersion(v: Int) = MmkvUtil.encodeInt(UNIT_VERSION, v)

}