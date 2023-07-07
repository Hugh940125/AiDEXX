package com.microtech.aidexx.common.net.repository

import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.common.net.entity.ReqDeleteEventIds
import com.microtech.aidexx.common.net.entity.ReqGetFollowUserById
import com.microtech.aidexx.common.net.entity.ReqGetShareOrFollowUsers
import com.microtech.aidexx.common.net.entity.ReqModifyShareUserInfo
import com.microtech.aidexx.common.net.entity.ReqShareUserInfo
import com.microtech.aidexx.common.net.entity.toQueryMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ShareAndFollowRepository {

    private val dispatcher = Dispatchers.IO

    suspend fun findUserAuthorizationList(isShare: Boolean = false) = withContext(dispatcher) {
        val query = ReqGetShareOrFollowUsers(if (isShare) "0" else "1")
        ApiService.instance.findUserAuthorizationList(query.toQueryMap())
    }
    suspend fun findAuthorizationInfoById(userAuthorizationId: String) = withContext(dispatcher) {
        val query = ReqGetFollowUserById(userAuthorizationId)
        ApiService.instance.findAuthorizationInfoById(query.toQueryMap())
    }

    suspend fun saveOrUpdateUserAuthorization(userName: String, userAlise: String?) = withContext(dispatcher) {
        ApiService.instance.saveOrUpdateUserAuthorization(ReqShareUserInfo(
            readerUserName = userName,
            readerAlias = userAlise
        ))
    }

    suspend fun deleteByIdsShareFollow(ids: List<String>) = withContext(dispatcher) {
        ApiService.instance.deleteByIdsShareFollow( ReqDeleteEventIds(ids) )
    }

    suspend fun updateAuthorizationInfo(
        providerAlias: String? = null, // 非必须 string 数据提供者的昵称
        readerAlias: String? = null, // 非必须 string 数据查看者的昵称。
        hideState: Int? = null, // 非必须 1 是否隐藏,隐藏同时关闭所有推送
        emergePushState: Int? = null, // 非必须 1 紧急推送开关
        normalPushState: Int? = null, // 非必须 1 普通是否推送
        userAuthorizationId: String, // 必须 string 主键
    ) = withContext(dispatcher) {
        ApiService.instance.updateAuthorizationInfo(ReqModifyShareUserInfo(
            providerAlias,
            readerAlias,
            hideState,
            emergePushState,
            normalPushState,
            userAuthorizationId,
        ))
    }



}