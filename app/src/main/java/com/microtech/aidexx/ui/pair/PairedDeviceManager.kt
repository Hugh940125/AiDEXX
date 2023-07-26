package com.microtech.aidexx.ui.pair

import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.utils.ToastUtil

/**
 *@date 2023/7/26
 *@author Hugh
 *@desc
 */
object PairedDeviceManager {

    suspend fun loadHistoryDevice() {
        when (val historyDevice = ApiService.instance.getHistoryDevice()) {
            is ApiResult.Success -> {
                historyDevice.result.data?.let {
                    ObjectBox.historyDeviceBox!!.put(it)
                }
            }

            is ApiResult.Failure -> {
                ToastUtil.showShort(historyDevice.msg)
            }
        }
    }
}