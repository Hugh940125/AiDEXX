package com.microtech.aidexx.ble.device

import com.microtech.aidexx.base.BaseApi
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.common.net.entity.BaseResponse
import com.microtech.aidexx.db.entity.TransmitterEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 *@date 2023/2/24
 *@author Hugh
 *@desc
 */
object DeviceApi : BaseApi() {

    suspend fun getDevice(
        success: ((info: BaseResponse<TransmitterEntity>) -> Unit)? = null,
        failure: (() -> Unit)? = null
    ) = withContext(Dispatchers.IO) {
        when (val apiResult = ApiService.instance.getDevice()) {
            is ApiResult.Success -> {
                apiResult.result.let { result ->
                    withContext(Dispatchers.Main) {
                        success?.invoke(result)
                    }
                }
            }
            is ApiResult.Failure -> {
                withContext(Dispatchers.Main) {
                    failure?.invoke()
                }
            }
        }
    }

}