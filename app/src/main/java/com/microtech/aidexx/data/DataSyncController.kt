package com.microtech.aidexx.data

import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.BuildConfig
import com.microtech.aidexx.common.ioScope
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.entity.BaseEventEntity
import com.microtech.aidexx.db.entity.BloodGlucoseEntity
import com.microtech.aidexx.db.entity.CalibrateEntity
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import com.microtech.aidexx.db.entity.event.DietEntity
import com.microtech.aidexx.db.entity.event.ExerciseEntity
import com.microtech.aidexx.db.entity.event.InsulinEntity
import com.microtech.aidexx.db.entity.event.MedicationEntity
import com.microtech.aidexx.db.entity.event.OthersEntity
import com.microtech.aidexx.db.repository.CgmCalibBgRepository
import com.microtech.aidexx.db.repository.EventDbRepository
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.mmkv.MmkvManager
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.reflect.ParameterizedType

private val dataSyncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

abstract class DataSyncController<T: BaseEventEntity> {

    companion object {
        private const val TAG = "DataSyncController"
        val scope = dataSyncScope
        const val DATA_EMPTY_MIN_ID = 0L

        fun getDataSyncFlagKey(userId: String, clazz: Class<*>): String =
            "$userId-${clazz.simpleName}-DATA-SYNC-FLAG"

        suspend fun insertToDb(data: List<BaseEventEntity>, clazz: Class<out BaseEventEntity>) {
            when (clazz) {
                RealCgmHistoryEntity::class.java -> CgmCalibBgRepository.insertCgm(data as List<RealCgmHistoryEntity>)
                CalibrateEntity::class.java -> CgmCalibBgRepository.insertCal(data as List<CalibrateEntity>)
                BloodGlucoseEntity::class.java -> CgmCalibBgRepository.insertBg(data as List<BloodGlucoseEntity>)

                DietEntity::class.java, ExerciseEntity::class.java,
                MedicationEntity::class.java, InsulinEntity::class.java,
                OthersEntity::class.java -> EventDbRepository.insertEvents(data)

                else -> {
                    if (BuildConfig.DEBUG) TODO("添加对应类型的数据库写入接口 clazz=${clazz.simpleName}")
                    else LogUtil.xLogE("不支持当前类型下载数据写入db clazz=${clazz.simpleName}")
                }
            }
        }
    }


    val tClazz =
        (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>

    sealed class SyncStatus {
        data class Loading(val progress: Int = 0): SyncStatus()
        object Success: SyncStatus()
        data class Failure(val msg: String? = null): SyncStatus()
    }

    private val dataSyncExceptionHandler = CoroutineExceptionHandler { context, throwable ->
        LogUtil.xLogE( "$this 数据同步异常 \n ${throwable.stackTraceToString()}", TAG)
        stopDownloadData(true)
    }
    private val shareUserDataSyncExceptionHandler = CoroutineExceptionHandler { context, throwable ->
        LogUtil.xLogE( "$this share数据同步异常 \n ${throwable.stackTraceToString()}", TAG)
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

        if (!canSync()) {
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
                        if (it is SyncStatus.Success || it is SyncStatus.Failure) {
                            cancel()
                        }
                    }
                }
            }
        } else {
            ret = downloadStateFlow.compareAndSet(expect = false, true)
            if (ret) {
                scope.launch {
                    downloadStatusStateFlow.collect {
                        cb?.invoke(it)
                        if (it is SyncStatus.Success || it is SyncStatus.Failure) {
                            cancel()
                        }
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
        LogUtil.xLogI("download data curState=${downloadStateFlow.value}", TAG)
    }

    private fun stopDownloadShareUserData(isFromException: Boolean = false){
        if(isFromException){
            AidexxApp.instance.ioScope.launch { downloadShareDataStatusStateFlow.emit(SyncStatus.Failure()) }
        }
        downloadShareDataStateFlow.value = null
        LogUtil.xLogI("download share user data curState=${downloadShareDataStateFlow.value}", TAG)
    }

    fun canSync(): Boolean {
        val ret = UserInfoManager.instance().isLogin()
        if (!ret) {
            LogUtil.xLogE("未登录，$this 停止下载")
        }
        return ret
    }

    protected suspend fun applyData(userId: String, data: List<T>) {
        insertToDb(data, tClazz)
        updateSyncFlagMinId(userId, data.last().autoIncrementColumn)
    }

    private fun updateSyncFlagMinId(userId: String, minId: Long?) =
        MmkvManager.setEventDataMinId(getDataSyncFlagKey(userId),minId)

    fun getCurrMinId(userId: String) = MmkvManager.getEventDataMinId<Long>(
        getDataSyncFlagKey(userId)
    )

    fun getSyncMaxId(userId: String) = getCurrMinId(userId)?.let { it - 1 }

    fun getDataSyncFlagKey(userId: String): String = getDataSyncFlagKey(userId, tClazz)

    protected abstract suspend fun downloadData(userId: String): Boolean

}