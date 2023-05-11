package com.microtech.aidexx.data

import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.common.ioScope
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.utils.LogUtil
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.reflect.ParameterizedType

abstract class DataSyncController<T> {

    val tag = "DataSyncController"
    val tClazz =
        (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>

    sealed class SyncStatus {
        data class Loading(val progress: Int = 0): SyncStatus()
        object Success: SyncStatus()
        data class Failure(val msg: String? = null): SyncStatus()
    }

    private val dataSyncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val dataSyncExceptionHandler = CoroutineExceptionHandler { context, throwable ->
        LogUtil.xLogE( "$this 数据同步异常 \n ${throwable.stackTraceToString()}", tag)
        stopDownloadData(true)
    }
    private val shareUserDataSyncExceptionHandler = CoroutineExceptionHandler { context, throwable ->
        LogUtil.xLogE( "$this share数据同步异常 \n ${throwable.stackTraceToString()}", tag)
        stopDownloadShareUserData(true)
    }
    /**
     * 控制本用户数据下载
     */
    private val downloadStateFlow = MutableStateFlow(false)
    private val downloadStatusStateFlow = MutableStateFlow<SyncStatus?>(null)
    val downloadStatus = downloadStatusStateFlow.asStateFlow()

    /**
     * 控制本分享用户的数据下载 value 是分享用户的id
     */
    private val downloadShareDataStateFlow = MutableStateFlow<String?>(null)
    private val downloadShareDataStatusStateFlow = MutableStateFlow<SyncStatus?>(null)
    val downloadShareDataStatus = downloadShareDataStatusStateFlow.asStateFlow()

    init {
        AidexxApp.instance.ioScope.launch {
            launch {
                downloadStateFlow.collect {
                    if (it) {
                        dataSyncScope.launch(dataSyncExceptionHandler) {
                            download(UserInfoManager.instance().userId(), downloadStatusStateFlow, ::stopDownloadData)
                        }
                    }
                }
            }
            launch {
                downloadShareDataStateFlow.collect {
                    it?.let { userId ->
                        dataSyncScope.launch(shareUserDataSyncExceptionHandler) {
                            download(userId, downloadShareDataStatusStateFlow, ::stopDownloadShareUserData)
                        }
                    }
                }
            }
        }
    }

    private suspend fun download(userId: String, statusFlow: MutableStateFlow<SyncStatus?>, stopDownloadFun: (Boolean)->Unit) {

        if (!UserInfoManager.instance().isLogin()) {
            LogUtil.xLogE("未登录，$this 下载失败")
            downloadStatusStateFlow.emit(SyncStatus.Failure())
            return
        }

        statusFlow.emit(SyncStatus.Loading())
        if (downloadData(userId)) {
            statusFlow.emit(SyncStatus.Success)
        } else {
            statusFlow.emit(SyncStatus.Failure())
        }
        stopDownloadFun.invoke(false)
    }

    /**
     * 启动事件数据下载
     * 当前用户和关注人数据下载不互斥
     * 关注人之间的数据下载互斥
     */
    fun startDownload(userId: String?, scope: CoroutineScope = dataSyncScope, cb: ((SyncStatus?)->Unit)? = null): Boolean {

        val isShareData = userId?.let {
            it != UserInfoManager.instance().userId()
        } ?: false

        val ret: Boolean
        if (isShareData) {
            ret = downloadShareDataStateFlow.compareAndSet(null, userId)
            if (ret) {
                scope.launch {
                    downloadShareDataStatusStateFlow.collect {
                        cb?.invoke(it)
                    }
                }
            }
        } else {
            ret = downloadStateFlow.compareAndSet(expect = false, true)
            if (ret) {
                scope.launch {
                    downloadStatusStateFlow.collect {
                        cb?.invoke(it)
                    }
                }
            }
        }

        return ret
    }

    private fun stopDownloadData(isFromException: Boolean = false){
        if(isFromException){
            AidexxApp.instance.ioScope.launch { downloadStatusStateFlow.emit(SyncStatus.Failure()) }
        }
        downloadStateFlow.compareAndSet(expect = true, false)
        LogUtil.xLogI("download data curState=${downloadStateFlow.value}", tag)
    }

    private fun stopDownloadShareUserData(isFromException: Boolean = false){
        if(isFromException){
            AidexxApp.instance.ioScope.launch { downloadShareDataStatusStateFlow.emit(SyncStatus.Failure()) }
        }
        downloadShareDataStateFlow.value = null
        LogUtil.xLogI("download share user data curState=${downloadShareDataStateFlow.value}", tag)
    }


    fun getDataMinIdKey(userId: String): String = "$userId-${tClazz.simpleName}-MIN-ID"

    protected abstract suspend fun downloadData(userId: String): Boolean

}