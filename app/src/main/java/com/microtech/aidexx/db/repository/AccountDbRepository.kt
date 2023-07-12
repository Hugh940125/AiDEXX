package com.microtech.aidexx.db.repository

import com.microtech.aidexx.db.dao.UserDao
import com.microtech.aidexx.db.entity.UserEntity

object AccountDbRepository {

    suspend fun saveUser(userEntity: UserEntity): Long? = UserDao.saveUser(userEntity)
    suspend fun getUserInfoByUid(uid: String): UserEntity? = UserDao.getUserInfoByUid(uid)
    suspend fun removeUserByUId(uid: String): Boolean = UserDao.delUserByUid(uid)
    suspend fun removeAll() = UserDao.removeAll()

}