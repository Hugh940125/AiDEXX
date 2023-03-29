package com.microtech.aidexx.data

import com.microtech.aidexx.BuildConfig
import com.microtech.aidexx.common.getContext
import com.microtech.aidexx.common.getStartOfTheDay
import com.microtech.aidexx.common.net.ApiRepository
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.entity.AppUpdateInfo
import com.microtech.aidexx.utils.LogUtils
import com.microtech.aidexx.utils.NetUtil
import com.microtech.aidexx.utils.StringUtils
import com.microtech.aidexx.utils.mmkv.MmkvManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

private val appUpgradeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

/**
 * app版本检查、下载、进度、安装或者跳转应用市场
 */
object AppUpgradeManager {

    const val TAG = "AppUpgradeManager"
    const val SYNC_STATUS_DONE = 100
    const val SYNC_STATUS_ERROR = -1

    /**
     * 标记任务执行状态
     * 1：启动状态
     * 0：停止状态
     */
    private val mUpgradeState: MutableStateFlow<Int> = MutableStateFlow(0)

    /**
     * 更新进度
     */
    private val mUpgradeProgress: MutableStateFlow<Int> = MutableStateFlow(0)

    /** 外部使用 */
    val upgradeProgress = mUpgradeProgress.asStateFlow()

    private val syncExceptionHandler = CoroutineExceptionHandler { context, throwable ->
        LogUtils.error(TAG,"app升级异常 \n ${throwable.stackTraceToString()}")
        stopUpgrade(true)
    }

    init {
        GlobalScope.launch {
            mUpgradeState.collect {
                if (it == 1) {
                    appUpgradeScope.launch(syncExceptionHandler) {
                        // 启动下载
                    }
                }
            }
        }
    }

    /**
     * 拉升级信息
     * @param isManual 是否是手动触发
     * @return [null] 暂无更新
     */
    suspend fun fetchVersionInfo(
        isManual: Boolean = false
    ): AppUpdateInfo? = withContext(Dispatchers.IO) {

        if (needCheckNewVersion(isManual)) {
            when (val apiResult = ApiRepository.checkAppUpdate()) {
                is ApiResult.Success -> {
                    MmkvManager.updateAppCheckVersionTime()
                    val info = apiResult.result
                    if (StringUtils.versionCompare(BuildConfig.VERSION_NAME, info.data.version)) {
                        info
                    } else null
                }
                is ApiResult.Failure -> null
            }
        } else null
    }

    fun startUpgrade(): Boolean {
        val ret = mUpgradeState.compareAndSet(0, 1)
        LogUtils.debug(TAG,"启动升级 ret=$ret")
        return ret
    }

    /**
     * 结束同步
     * 同步成功及同步异常都需要调用
     */
    private fun stopUpgrade(isFromException: Boolean = false){
        if(isFromException){
            GlobalScope.launch { mUpgradeProgress.emit(SYNC_STATUS_ERROR) }
        }
        val isStop = mUpgradeState.compareAndSet(1,0)
        LogUtils.data("$TAG 结束同步结果=$isStop  curState=${mUpgradeState.value}")
    }

    private fun needCheckNewVersion(isManual: Boolean): Boolean {

        val isCheckedToday = MmkvManager.getAppCheckVersionTime() == Date().getStartOfTheDay().time
        if (!isManual && isCheckedToday) {
            LogUtils.data("needCheckNewVersion 当天已经检测过")
            return false
        }

        if (!NetUtil.isNetAvailable(getContext())) {
            LogUtils.data("needCheckNewVersion 网络不可用")
            return false
        }
        return true
    }

}