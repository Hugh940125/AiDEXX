package com.microtech.aidexx.ui.main.home

import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.repository.ShareAndFollowRepository
import com.microtech.aidexx.ui.setting.share.ShareUserInfo
import com.microtech.aidexx.utils.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeViewModel: BaseViewModel() {

    // 关注人列表
    val mFollowers: MutableList<ShareUserInfo> = mutableListOf()

    fun updateFollowers(data: MutableList<ShareUserInfo>) {
        mFollowers.clear()
        mFollowers.addAll(data)
    }

    suspend fun getFollowers(): Boolean = withContext(Dispatchers.IO) {
        when (val ret = ShareAndFollowRepository.findUserAuthorizationList()) {
            is ApiResult.Success -> {

                if (ret.result.data.isNullOrEmpty()) {
                    false
                } else {
                    mFollowers.clear()
                    mFollowers.addAll(ret.result.data!!)
                    true
                }
            }
            is ApiResult.Failure -> {
                LogUtil.d("getFollowers fail code=${ret.code} msg=${ret.msg}")
                false
            }
        }
    }

}