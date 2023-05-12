package com.microtech.aidexx.common.user

import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.common.equal
import com.microtech.aidexx.common.net.entity.ResUserInfo
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.entity.ShareUserEntity
import com.microtech.aidexx.db.entity.UserEntity
import com.microtech.aidexx.db.entity.UserEntity_
import com.microtech.aidexx.db.repository.AccountDbRepository
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

        var shareUserInfo: ShareUserEntity? = null

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
        fun getCurShowUserId() = shareUserInfo?.id ?: instance().userId()
    }

    suspend fun loadUserInfo() {

    }

    fun userId(): String {
        return userEntity?.id ?: ""
    }

    fun updateUserId(id: String) {
        MmkvManager.saveUserId(id)
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

    fun getDisplayName() = getNickName().ifEmpty { null }
        ?: "${getSurName()}${getGivenName()}".ifEmpty { null }
        ?: getPhone()

    fun getNickName(): String = MmkvManager.getNickName()

    fun getSurName(): String = MmkvManager.getSurName()

    fun getGivenName(): String = MmkvManager.getGivenName()

    private fun setPhone(phone: String) {
        MmkvManager.savePhone(phone)
    }

    fun getPhone(): String = MmkvManager.getPhone()

    private suspend fun getUserInfoById(userId: String): UserEntity {
        return ObjectBox.store.awaitCallInTx {
            ObjectBox.userBox!!.query().equal(UserEntity_.id, userId).orderDesc(UserEntity_.idx)
                .build().findFirst()
        } ?: UserEntity()
    }

    suspend fun onUserLogin(content: ResUserInfo): Long = withContext(Dispatchers.IO) {
        var entity = AccountDbRepository.getUserInfoByUid(content.userId!!)
        if (entity == null) {
            entity = UserEntity()
        }
        entity.id = content.userId
        entity.phoneNumber = content.phone
        entity.emailAddress = content.email
        entity.avatar = content.avatar

        this@UserInfoManager.userEntity?.let {
            it.id = entity.id
            it.phoneNumber = entity.phoneNumber
            it.emailAddress = entity.emailAddress
            it.avatar = entity.emailAddress
        } ?:let {
            this@UserInfoManager.userEntity = entity
        }

        AccountDbRepository.saveUser(entity)?.let {
            updateUserId(content.userId)
            setPhone(content.phone ?: "")
            it
        } ?: -1
    }
}
