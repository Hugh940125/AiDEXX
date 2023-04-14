package com.microtech.aidexx.common.user

import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.common.equal
import com.microtech.aidexx.common.net.entity.LoginInfo
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.entity.ShareUserEntity
import com.microtech.aidexx.db.entity.UserEntity
import com.microtech.aidexx.db.entity.UserEntity_
import com.microtech.aidexx.utils.mmkv.MmkvManager
import io.objectbox.kotlin.awaitCallInTx
import kotlinx.coroutines.launch

/**
 *@date 2023/2/9
 *@author Hugh
 *@desc 用户信息管理
 */
class UserInfoManager {
    private var entity: UserEntity? = null

    init {
        AidexxApp.mainScope.launch {
            entity = ObjectBox.store.awaitCallInTx {
                ObjectBox.userBox!!.query().orderDesc(UserEntity_.idx)
                    .build().findFirst()
            }
        }
    }

    companion object {

        var shareUserInfo: ShareUserEntity? = null

        private val INSTANCE = UserInfoManager()

        fun instance(): UserInfoManager {
            return INSTANCE
        }

        fun getCurShowUserId() = shareUserInfo?.id ?: INSTANCE.userId()
    }

    suspend fun loadUserInfo() {

    }

    fun userId(): String {
        return entity?.id ?: ""
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

    fun updateLoginFlag(isLogin: Boolean) {
        MmkvManager.setLogin(isLogin)
    }

    fun updateProfile(profile: String) {
        MmkvManager.saveProfile(profile)
    }

    private suspend fun getUserInfoById(userId: String): UserEntity {
        return ObjectBox.store.awaitCallInTx {
            ObjectBox.userBox!!.query().equal(UserEntity_.id, userId).orderDesc(UserEntity_.idx)
                .build().findFirst()
        } ?: UserEntity()
    }

    suspend fun onUserLogin(content: LoginInfo, callback: ((success: Boolean) -> Unit)?) {
        content.id?.let { id ->
            entity = getUserInfoById(id)
            entity?.let {
                it.id = id
                it.phoneNumber = content.phoneNumber
                it.avatar = content.profile
                ObjectBox.runAsync({
                    ObjectBox.userBox!!.put(it)
                }, {
                    instance().updateLoginFlag(true)
                    callback?.invoke(true)
                }, {
                    callback?.invoke(false)
                })
            }
            return
        }
        callback?.invoke(false)
    }
}
