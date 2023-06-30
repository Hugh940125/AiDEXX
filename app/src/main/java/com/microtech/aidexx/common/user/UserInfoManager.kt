package com.microtech.aidexx.common.user

import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.common.equal
import com.microtech.aidexx.common.ioScope
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.entity.UserEntity
import com.microtech.aidexx.db.entity.UserEntity_
import com.microtech.aidexx.db.repository.AccountDbRepository
import com.microtech.aidexx.ui.setting.share.ShareUserInfo
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.utils.mmkv.MmkvManager
import io.objectbox.kotlin.awaitCallInTx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *@date 2023/2/9
 *@author Hugh
 *@desc 用户信息管理
 */
class UserInfoManager {
    private var userEntity: UserEntity? = null

    init {
        AidexxApp.mainScope.launch {
            userEntity = ObjectBox.store.awaitCallInTx {
                ObjectBox.userBox!!.query().orderDesc(UserEntity_.idx)
                    .build().findFirst()
            }
        }
    }

    companion object {

        var shareUserInfo: ShareUserInfo? = null

        private var INSTANCE: UserInfoManager? = null

        @Synchronized
        fun instance(): UserInfoManager {
            if (INSTANCE == null){
                INSTANCE = UserInfoManager()
            }
            return INSTANCE!!
        }

        /**
         * 获取当前展示的用户id 自己的或者共享中的用户
         */
        fun getCurShowUserId() = shareUserInfo?.dataProviderId ?: instance().userId()
    }

    suspend fun loadUserInfo() {

    }

    fun userId(): String {
        return userEntity?.userId ?: "unknown"
    }

    fun isLogin(): Boolean {
        return MmkvManager.isLogin()
    }

    fun updateLoginFlag(isLogin: Boolean) {
        MmkvManager.setLogin(isLogin)
    }

    fun updateProfile(profile: String) {
        MmkvManager.saveProfile(profile)
    }

    fun getDisplayName() = userEntity?.getDisplayName()

    private suspend fun getUserInfoById(userId: String): UserEntity {
        return ObjectBox.store.awaitCallInTx {
            ObjectBox.userBox!!.query().equal(UserEntity_.userId, userId).orderDesc(UserEntity_.idx)
                .build().findFirst()
        } ?: UserEntity()
    }

    fun onTokenExpired() {
        AidexxApp.instance.ioScope.launch {
            updateLoginFlag(false)
            MmkvManager.saveToken("")
        }
    }

    suspend fun onUserLogin(content: UserEntity): Long = withContext(Dispatchers.IO) {
        var entity = AccountDbRepository.getUserInfoByUid(content.userId!!)
        if (entity == null) {
            entity = content
        } else {
            content.idx = entity.idx
            entity = content
        }

        this@UserInfoManager.userEntity = entity

        AccountDbRepository.saveUser(entity)?.let {
            it
        } ?: -1
    }

    /**
     * @param from 0-主动退出 1-被踢
     */
    suspend fun onUserExit(from: Int = 0) {
        this@UserInfoManager.userEntity = null
        shareUserInfo = null
        updateLoginFlag(false)
        // 用户退出登录
        EventBusManager.send(EventBusKey.EVENT_LOGOUT, 0)
    }

}
