package com.microtech.aidexx.utils.mmkv

import com.microtech.aidexx.common.user.UserInfoManager

object MmkvManager {

    private const val GET_USER_ID = "GET_USER_ID"
    private const val THEME = "THEME"

    fun getUserId() = MmkvUtil.decodeString(GET_USER_ID, "")

    fun getOnlineServiceMsgCount() = MmkvUtil.decodeInt(UserInfoManager.instance().userId(), 0)

    fun setOnlineServiceMsgCount(count: Int) =
        MmkvUtil.encodeInt(UserInfoManager.instance().userId(), count)

    fun getEnquireFlag(flag: String): Boolean = MmkvUtil.decodeBoolean(flag, false)

    fun saveEnquireFlag(flag: String, value: Boolean) = MmkvUtil.encodeBoolean(flag, value)

    fun getTheme(): Int = MmkvUtil.decodeInt(THEME, 1)

    fun saveTheme(index: Int) = MmkvUtil.encodeInt(THEME, index)
}