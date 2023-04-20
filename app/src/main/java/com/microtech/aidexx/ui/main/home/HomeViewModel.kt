package com.microtech.aidexx.ui.main.home

import com.google.gson.Gson
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.getMutableListType
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.repository.AccountRepository
import com.microtech.aidexx.db.entity.ShareUserEntity
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
            else -> {
                val str = "[{\"emergeState\":false,\"isChildAccount\":0,\"pushState\":false,\"source\":1,\"wxState\":false,\"id\":\"6d1a8785045fbe4a1319a450490764ba\",\"user\":{\"id\":\"3ac82eeb24f2e3aec8ffd63ba7f35711\",\"phoneNumber\":\"17326038125\",\"profile\":\"/profiles/UPP3ac82eeb24f2e3aec8ffd63ba7f35711\"},\"authorizedUserAlias\":\"吃鸡果宝机甲循环巡检不不\",\"device\":{\"id\":\"26c4b039f6365ab86f2ebcdd33961129\",\"recordIndex\":4,\"deviceModel\":\"0\",\"deviceSn\":\"NB0200\",\"deviceMac\":\"80:EA:CA:70:00:05\",\"sensorIndex\":0,\"eventIndex\":0,\"fullSensorIndex\":0,\"fullEventIndex\":0,\"registerTime\":\"2023-03-23 13:21:46+0800\",\"et\":0},\"userHealthTarget\":{\"id\":\"c8f71d7e6f6e3536923875fae93504fe\",\"recordIndex\":1,\"glucoseLower\":4.9,\"glucoseUpper\":12.0},\"hide\":false}]"

                val ss = runCatching<MutableList<ShareUserEntity>> {
                    Gson().fromJson(str, getMutableListType<ShareUserEntity>())
                }.getOrNull()
                mFollowers.clear()
                mFollowers.addAll(ss!!)
                true
            }
        }
    }

}