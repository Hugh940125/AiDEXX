package com.microtech.aidexx.db.dao

import com.microtech.aidexx.common.equal
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.ObjectBox.awaitCallInTx
import com.microtech.aidexx.db.entity.UserEntity
import com.microtech.aidexx.db.entity.UserEntity_
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.query

object UserDao {

    private val box by lazy { ObjectBox.store.boxFor<UserEntity>() }

    suspend fun saveUser(user: UserEntity): Long? = awaitCallInTx {
        box.put(user)
    }

    suspend fun getUserInfoByUid(userId: String): UserEntity? = awaitCallInTx {
        box.query()
            .equal(UserEntity_.userId, userId)
            .orderDesc(UserEntity_.idx)
            .build().findFirst()
    }

    suspend fun delUserByUid(uid: String): Boolean = awaitCallInTx {
        box.query {
            equal(UserEntity_.userId, uid)
        }.remove() > 0
    } ?: false

    suspend fun removeAll() = awaitCallInTx {
        box.removeAll()
    }

}