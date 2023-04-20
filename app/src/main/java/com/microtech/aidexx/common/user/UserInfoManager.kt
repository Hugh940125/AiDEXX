package com.microtech.aidexx.common.user

import com.microtech.aidexx.common.net.entity.ResUserInfo
import com.microtech.aidexx.db.entity.ShareUserEntity
import com.microtech.aidexx.db.entity.UserEntity
import com.microtech.aidexx.db.repository.AccountDbRepository
import com.microtech.aidexx.utils.mmkv.MmkvManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

        /**
         * 获取当前展示的用户id 自己的或者共享中的用户
         */
        fun getCurShowUserId() = shareUserInfo?.id ?: INSTANCE.userId()
    }

    fun userId(): String {
        return MmkvManager.getUserId()
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

    suspend fun onUserLogin(content: ResUserInfo): Long = withContext(Dispatchers.IO){
        var entity = AccountDbRepository.getUserInfoByUid(content.userId!!)
        if (entity == null) {
            entity = UserEntity()
        }
        entity.id = content.userId
        entity.phoneNumber = content.phone
        entity.emailAddress = content.email
        entity.avatar = content.avatar

        AccountDbRepository.saveUser(entity)?.let {
            updateUserId(content.userId)
            setPhone(content.phone ?: "")
            instance().updateLoginFlag(true)
            it
        } ?: -1
    }
}
