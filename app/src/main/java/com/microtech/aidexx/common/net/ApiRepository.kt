package com.microtech.aidexx.common.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ApiRepository {

    private val dispatcher = Dispatchers.IO

    suspend fun checkAppUpdate() = withContext(dispatcher) {
        val appId = "cn" // 国际版再改
        ApiService.instance.checkAppUpdate(appId)
    }



}