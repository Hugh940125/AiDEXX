package com.microtech.aidexx.data

import com.google.gson.Gson
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.BuildConfig
import com.microtech.aidexx.common.ioScope
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.entity.PAGE_SIZE
import com.microtech.aidexx.common.net.repository.EventRepository
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
import com.microtech.aidexx.utils.eventbus.DataChangedType
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.utils.eventbus.EventDataChangedInfo
import com.microtech.aidexx.utils.mmkv.MmkvManager
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.reflect.ParameterizedType

private val dataSyncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

abstract class DataSyncController<T: BaseEventEntity> {

    companion object {
        private const val TAG = "DataSyncController"
        val scope = dataSyncScope

        const val DOWNLOAD_INTERVAL: Long = 5 * 1000

        fun getLoginStateKey(userId: String, clazz: Class<*>): String =
            "LoginState-$userId-${clazz.simpleName}" // 标记登录时这个事件数据是否下载成功
        fun getLoginMaxIdKey(userId: String, clazz: Class<*>): String =
            "LoginState-$userId-${clazz.simpleName}-MaxId" // 标记登录时这个事件本地最大id

        fun getTaskItemListKey(userId: String, clazz: Class<*>): String =
            "TaskItemList-$userId-${clazz.simpleName}" // 标记登录之后的同步任务

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
    private val syncDeletedDataExceptionHandler = CoroutineExceptionHandler { context, throwable ->
        LogUtil.xLogE( "$this 同步删除数据异常 \n ${throwable.stackTraceToString()}", TAG)
        stopSyncDeletedData(true)
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
    private var downloadShareDataJob: Job? = null
    private val downloadShareDataStateFlow = MutableStateFlow<String?>(null)
    private val downloadShareDataStatusStateFlow = MutableStateFlow<SyncStatus?>(null)
    val downloadShareDataStatus = downloadShareDataStatusStateFlow.asStateFlow()

    /**
     * 控制本用户同步删除事件
     */
    private val uploadDeletedDataStateFlow = MutableStateFlow(false)

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
                            // 分享用户同步数据时需要下载最新数据
                            downloadDataOfRealTime(userId)
                            download(userId, downloadShareDataStatusStateFlow, ::stopDownloadShareUserData)
                        }
                    }
                }
            }

            launch {
                uploadDeletedDataStateFlow.collectLatest {
                    if (it) {
                        dataSyncScope.launch(syncDeletedDataExceptionHandler) {
                            syncDeletedData(UserInfoManager.instance().userId(), ::stopSyncDeletedData)
                        }
                    }
                }
            }
        }
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
            // 切换了关注用户
            if (downloadShareDataStateFlow.value != null && downloadShareDataStateFlow.value != userId) {
                downloadShareDataJob?.cancel()
                downloadShareDataStateFlow.value = null
            }
            ret = downloadShareDataStateFlow.compareAndSet(null, userId)
            if (ret) {
                downloadShareDataJob = scope.launch {
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

    /**
     * 启动删除数据同步任务
     */
    fun startUploadDeletedData() {
        uploadDeletedDataStateFlow.compareAndSet(expect = false, true)
    }

    private suspend fun download(
        userId: String,
        statusFlow: MutableStateFlow<SyncStatus?>,
        stopDownloadFun: (Boolean)->Unit
    ) {

        if (!canSync()) {
            downloadStatusStateFlow.emit(SyncStatus.Failure())
            LogUtil.xLogI("download data stop no login ${tClazz.simpleName}", TAG)
        } else {
            statusFlow.emit(SyncStatus.Loading())
            if (downloadData(userId)) {
                statusFlow.emit(SyncStatus.Success)
            } else {
                statusFlow.emit(SyncStatus.Failure())
            }
        }
        stopDownloadFun.invoke(false)
    }

    private fun stopDownloadData(isFromException: Boolean = false){
        if(isFromException){
            AidexxApp.instance.ioScope.launch { downloadStatusStateFlow.emit(SyncStatus.Failure()) }
        }
        downloadStateFlow.compareAndSet(expect = true, false)
        LogUtil.xLogI("download data ${tClazz.simpleName} curState=${downloadStateFlow.value}", TAG)
    }
    private fun stopDownloadShareUserData(isFromException: Boolean = false){
        if(isFromException){
            AidexxApp.instance.ioScope.launch { downloadShareDataStatusStateFlow.emit(SyncStatus.Failure()) }
        }
        downloadShareDataStateFlow.value = null
        LogUtil.xLogI("download share user data ${tClazz.simpleName} curState=${downloadShareDataStateFlow.value}", TAG)
    }

    private suspend fun syncDeletedData(userId: String, stopFun: (Boolean)->Unit) {

        if (!canSync()) {
            LogUtil.xLogI("sync deleted data stop no login ${tClazz.simpleName}", TAG)
        } else {
            if (!uploadDeletedData(userId)) {
                LogUtil.xLogI("sync deleted data stop fail ${tClazz.simpleName}", TAG)
            }
        }
        stopFun.invoke(false)
    }
    private fun stopSyncDeletedData(isFromException: Boolean = false){
        uploadDeletedDataStateFlow.compareAndSet(expect = true, false)
        LogUtil.xLogI("sync deleted data ${tClazz.simpleName} curState=${downloadStateFlow.value} $isFromException", TAG)
    }



    fun canSync(): Boolean {
        val ret = UserInfoManager.instance().isLogin()
        if (!ret) {
            LogUtil.xLogE("未登录，${tClazz.simpleName} 停止")
        }
        return ret
    }

    protected suspend fun applyData(userId: String, data: List<T>) {
        insertToDb(data, tClazz)
    }

    protected abstract suspend fun downloadData(userId: String): Boolean
    protected abstract suspend fun uploadDeletedData(userId: String): Boolean


    /**
     * 登录下载固定数量数据之后更新这个任务列表
     */
    data class SyncTaskItem(
        var startAutoIncrementColumn: Long?,
        var endAutoIncrementColumn: Long?
    ) {
        override fun hashCode(): Int {
            return "$startAutoIncrementColumn-$endAutoIncrementColumn".hashCode()
        }

        override fun equals(other: Any?): Boolean {
            return other?.let {
                it is SyncTaskItem &&
                        it.startAutoIncrementColumn == startAutoIncrementColumn &&
                        it.endAutoIncrementColumn == endAutoIncrementColumn
            } ?: false
        }
    }
    data class SyncTaskItemList(
        var list: MutableList<SyncTaskItem>
    ) {
        override fun toString(): String {
            return Gson().toJson(this)
        }
        companion object {
            fun fromString(str: String?): SyncTaskItemList? =
                runCatching {
                    Gson().fromJson(str, SyncTaskItemList::class.java)
                }.getOrNull()

        }
    }

    protected fun getFirstTaskItem(userId: String) =
        MmkvManager.getEventSyncTask(getTaskItemListKey(userId, tClazz))?.list?.ifEmpty { null }?.let {
            it.first()
        }

    protected fun updateFirstTaskItem(userId: String, taskItem: SyncTaskItem) {
        val key = getTaskItemListKey(userId, tClazz)
        MmkvManager.getEventSyncTask(key)?.let { tasks ->
            tasks.list.ifEmpty { null }?.removeAt(0)?.let {
                tasks.list.add(0, taskItem)
                MmkvManager.setEventSyncTask(key, tasks)
                LogUtil.d("SyncTaskItemList update $key=$tasks", TAG)
            } ?:let {
                LogUtil.d("SyncTaskItemList updateFail tasks=empty", TAG)
            }
        }?:let {
            LogUtil.d("SyncTaskItemList updateFail tasks=null", TAG)
        }
    }

    protected fun removeFirstTaskItem(userId: String) {
        val key = getTaskItemListKey(userId, tClazz)
        MmkvManager.getEventSyncTask(key)?.let {
            val removed = it.list.ifEmpty { null }?.removeAt(0)
            MmkvManager.setEventSyncTask(key, it)
            LogUtil.d("SyncTaskItemList removeFirst=$removed $key=$it", TAG)
        } ?:let {
            LogUtil.e("SyncTaskItemList removeFirst fail tasks=null", TAG)
        }
    }

    /**
     * 定时下载关注人的最新数据
     */
    private suspend fun downloadDataOfRealTime(userId: String): Boolean {
        var startAutoIncrementColumn: Long? = EventDbRepository.findMaxEventId(tClazz) ?: 0L
        startAutoIncrementColumn = if (startAutoIncrementColumn!! <= 0L) null else startAutoIncrementColumn

        return when (val apiResult = EventRepository.getEventRecordsByPageInfo(
            userId,
            PAGE_SIZE,
            startAutoIncrementColumn = startAutoIncrementColumn,
            endAutoIncrementColumn = null,
            tClazz)
        ) {
            is ApiResult.Success -> {
                apiResult.result.data?.ifEmpty { null }?.let {
                    applyData(userId, it as List<T>)
                    EventBusManager.send(
                        EventBusKey.EVENT_DATA_CHANGED,
                        EventDataChangedInfo(DataChangedType.ADD, it)
                    )
                }
                true
            }
            is ApiResult.Failure -> false
        }
    }

}