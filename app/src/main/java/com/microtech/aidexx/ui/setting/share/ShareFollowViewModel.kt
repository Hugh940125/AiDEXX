package com.microtech.aidexx.ui.setting.share

import androidx.lifecycle.ViewModel
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.repository.ShareAndFollowRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import java.util.Timer

class ShareFollowViewModel: ViewModel() {

    private var fixedRateTimer: Timer? = null
    private val periodPullFollowList: Long = 60 * 1000

    suspend fun loadData(isShare: Boolean) = flow {
        when (val ret = ShareAndFollowRepository.findUserAuthorizationList(isShare)) {
            is ApiResult.Success -> emit(ret.result.data?.toMutableList())
            is ApiResult.Failure -> emit(null)
        }
    }.flowOn(Dispatchers.IO)


    suspend fun shareMyselfToOther(userName: String, userAlise: String?) = flow {
        when (val ret = ShareAndFollowRepository.saveOrUpdateUserAuthorization(userName, userAlise)) {
            is ApiResult.Success -> emit(null)
            is ApiResult.Failure -> emit(ret.msg)
        }
    }.flowOn(Dispatchers.IO)


    suspend fun getQrCodeToShareMySelf() = flow {
        emit("")
    }.flowOn(Dispatchers.IO)


    suspend fun modifyFollowUser(
        providerAlias: String? = null, // 非必须 string 数据提供者的昵称
        readerAlias: String? = null, // 非必须 string 数据查看者的昵称。
        hideState: Int? = null, // 非必须 1 是否隐藏,隐藏同时关闭所有推送
        emergePushState: Int? = null, // 非必须 1 紧急推送开关
        normalPushState: Int? = null, // 非必须 1 普通是否推送
        userAuthorizationId: String, // 必须 string 主键
    ) = flow {
        when (ShareAndFollowRepository.updateAuthorizationInfo(
            providerAlias,
            readerAlias,
            hideState,
            emergePushState,
            normalPushState,
            userAuthorizationId,
        )) {
            is ApiResult.Success -> emit(true)
            is ApiResult.Failure -> emit(false)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun modifyShareUser(
        readerAlias: String? = null, // 非必须 string 数据查看者的昵称
        userAuthorizationId: String, // 必须 string 主键
    ) = flow {
        when (ShareAndFollowRepository.updateAuthorizationInfo(
            readerAlias = readerAlias,
            userAuthorizationId = userAuthorizationId,
        )) {
            is ApiResult.Success -> emit(true)
            is ApiResult.Failure -> emit(false)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun cancelShare(shareUserId: String) = flow {
        when (ShareAndFollowRepository.deleteByIdsShareFollow(listOf(shareUserId))) {
            is ApiResult.Success -> emit(true)
            else -> emit(false)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun fixedRateToGetFollowList() = callbackFlow {
        while (isActive) {
            loadData(false).collect {
                send(it)
            }
            delay(periodPullFollowList)
        }
        awaitClose()
    }

}