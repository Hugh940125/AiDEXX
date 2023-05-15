package com.microtech.aidexx.ui.main.home

import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.repository.AccountRepository
import com.microtech.aidexx.db.entity.ShareUserEntity
import com.microtech.aidexx.utils.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeViewModel: BaseViewModel() {

    // 关注人列表
    val mFollowers: MutableList<ShareUserEntity> = mutableListOf()

    suspend fun getFollowers(): Boolean = withContext(Dispatchers.IO) {
        when (val ret = AccountRepository.getFollowers()) {
            is ApiResult.Success -> {

                if (ret.result.data?.records.isNullOrEmpty()) {
                    false
                } else {
                    mFollowers.clear()
                    mFollowers.addAll(ret.result.data!!.records)
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