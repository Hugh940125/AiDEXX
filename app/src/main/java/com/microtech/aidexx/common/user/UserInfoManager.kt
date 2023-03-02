package com.microtech.aidexx.common.user

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

    fun isLogin(): Boolean {
        return true
    }
}