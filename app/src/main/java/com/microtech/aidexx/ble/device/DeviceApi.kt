package com.microtech.aidexx.ble.device

import com.microtech.aidexx.base.BaseApi
import com.microtech.aidexx.ble.device.entity.TransmitterEntity
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.ApiService
import kotlinx.coroutines.withContext

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
        when (val apiResult = ApiService.instance.deviceRegister(entity)) {
            is ApiResult.Success -> {
                apiResult.result.run {
                    success?.invoke(this)
                }
            }
            is ApiResult.Failure -> {
                failure?.invoke()
            }
        }
    }

    suspend fun deviceUnregister(
        map: HashMap<String, String>,
        success: ((entity: TransmitterEntity) -> Unit)? = null,
        failure: ((msg:String) -> Unit)? = null
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
}