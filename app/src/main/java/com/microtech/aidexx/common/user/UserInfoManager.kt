package com.microtech.aidexx.common.user

import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.common.equal
import com.microtech.aidexx.common.ioScope
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.entity.UserEntity
import com.microtech.aidexx.db.entity.UserEntity_
import com.microtech.aidexx.db.repository.AccountDbRepository
import com.microtech.aidexx.ui.setting.share.ShareUserInfo
import com.microtech.aidexx.utils.LogUtil
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
    var userEntity: UserEntity? = null
        private set

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
            if (INSTANCE == null) {
                INSTANCE = UserInfoManager()
            }
            return INSTANCE!!
        }

        /**
         * 获取当前展示的用户id 自己的或者共享中的用户
         */
        fun getCurShowUserId() = shareUserInfo?.dataProviderId ?: instance().userId()
    }

    fun userId(): String {
        return userEntity?.userId ?: MmkvManager.getUserId()
    }

    fun isLogin(): Boolean {
        return MmkvManager.isLogin()
    }

    fun updateLoginFlag(isLogin: Boolean) {
        MmkvManager.setLogin(isLogin)
    }

    fun saveUserId(userId: String) {
        MmkvManager.saveUserId(userId)
    }

    suspend fun updateProfile(
        name: String? = null,
        fullName: String? = null,
        height: Int? = null,
        bodyWeight: Int? = null,
        gender: Int? = null,
        birthDate: String? = null,
    ) {
        userEntity?.let { user ->
            fullName?.let { user.fullName = it }
            name?.let { userEntity?.name = it }
            height?.let { userEntity?.height = it }
            bodyWeight?.let { userEntity?.bodyWeight = it }
            gender?.let { userEntity?.gender = it }
            birthDate?.let { userEntity?.birthDate = it }

            AccountDbRepository.saveUser(user)
        } ?: let {
            LogUtil.xLogE("updateProfile user null")
        }
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
