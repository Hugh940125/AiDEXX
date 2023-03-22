package com.microtech.aidexx.ble.device

import com.microtech.aidexx.base.BaseApi
import com.microtech.aidexx.db.entity.TransmitterEntity
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.common.net.entity.BaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.await

/**
 *@date 2023/2/24
 *@author Hugh
 *@desc
 */
object DeviceApi : BaseApi() {

    suspend fun deviceRegister(
        entity: TransmitterEntity,
        success: ((entity: TransmitterEntity) -> Unit)? = null,
        failure: (() -> Unit)? = null
    ) = withContext(dispatcher) {
        val apiResult = ApiService.instance.deviceRegister(entity).await()
    }

    suspend fun deviceUnregister(
        map: HashMap<String, String>,
        success: ((entity: TransmitterEntity) -> Unit)? = null,
        failure: ((msg: String) -> Unit)? = null
    ) = withContext(dispatcher) {
        when (val apiResult = ApiService.instance.deviceUnregister(map)) {
            is ApiResult.Success -> {
                apiResult.result.run {
                    success?.invoke(this)
                }
            }
            is ApiResult.Failure -> {
                apiResult.msg.run {
                    failure?.invoke(this)
                }
            }
        }
    }

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