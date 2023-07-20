package com.microtech.aidexx.ui.main.home

import androidx.lifecycle.viewModelScope
import com.jeremyliao.liveeventbus.LiveEventBus
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.getContext
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.common.net.entity.WelfareInfo
import com.microtech.aidexx.common.net.repository.ShareAndFollowRepository
import com.microtech.aidexx.common.toast
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.data.CloudHistorySync
import com.microtech.aidexx.ui.setting.share.ShareUserInfo
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.views.dialog.Dialogs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : BaseViewModel() {

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
                    mFollowers.any { !it.hide }
                }
            }

            is ApiResult.Failure -> {
                LogUtil.d("getFollowers fail code=${ret.code} msg=${ret.msg}")
                false
            }
        }
    }

    fun switchUser(userId: String) {
        viewModelScope.launch {
            when (val ret = ShareAndFollowRepository.findAuthorizationInfoById(userId)) {
                is ApiResult.Success -> {
                    val shareUserInfo = ret.result.data
                    if (shareUserInfo?.dataProviderId != null && UserInfoManager.shareUserInfo?.dataProviderId != shareUserInfo.dataProviderId) {
                        UserInfoManager.shareUserInfo = shareUserInfo
                        Dialogs.showWait(getContext().getString(R.string.loading))

                        // 该用户的数据下载成功后再执行切换
                        if (CloudHistorySync.downloadRecentData(shareUserInfo.dataProviderId!!)) {
                            Dialogs.dismissWait()
                            LiveEventBus
                                .get(EventBusKey.EVENT_SWITCH_USER, ShareUserInfo::class.java)
                                .post(shareUserInfo)
                        } else {
                            Dialogs.dismissWait()
                            getContext().getString(R.string.switch_user_fail).toast()
                        }
                    }
                }

                is ApiResult.Failure -> {
                    LogUtil.xLogE("通知过来的分享人切换失败-数据拉取失败${ret.code}-${ret.msg}")
                }
            }
        }
    }

    suspend fun getActivities(): WelfareInfo? = withContext(Dispatchers.IO) {
        when (val ret = ApiService.instance.getWelfareActivity()) {
            is ApiResult.Success -> {
                ret.result.data
            }

            is ApiResult.Failure -> {
                LogUtil.d("getFollowers fail code=${ret.code} msg=${ret.msg}")
                null
            }
        }
    }

}