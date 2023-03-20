package com.microtech.aidexx.common.user

import com.microtech.aidexx.common.net.entity.LoginInfo
import com.microtech.aidexx.db.entity.ShareUserEntity
import com.microtech.aidexx.utils.mmkv.MmkvManager

/**
 *@date 2023/2/9
 *@author Hugh
 *@desc 用户信息管理
 */
class UserInfoManager {
    companion object {

        var shareUserInfo: ShareUserEntity? = null

        private val INSTANCE = UserInfoManager()

        fun instance(): UserInfoManager {
            return INSTANCE
        }
    }

    fun userId(): String {
        return MmkvManager.getUserId()
    }

    fun updateUserId(id: String) {
        MmkvManager.saveUserId(id)
    }

    fun updatePhone(phone: String) {
        MmkvManager.savePhone(phone)
    }

    fun phone(): String {
        return MmkvManager.getPhone()
    }

    fun isLogin(): Boolean {
        return MmkvManager.isLogin()
    }

    fun updateProfile(profile: String) {
        MmkvManager.saveProfile(profile)
    }

    fun onUserLogin(content: LoginInfo) {
        content.id?.let { id -> instance().updateUserId(id) }
        content.phoneNumber?.let { number -> instance().updatePhone(number) }
        content.profile?.let { profile -> instance().updateProfile(profile) }
        content.profile?.let { profile -> instance().updateProfile(profile) }
    }
}