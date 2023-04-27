package com.microtech.aidexx.data

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.BuildConfig
import com.microtech.aidexx.common.getContext
import com.microtech.aidexx.common.getStartOfTheDay
import com.microtech.aidexx.common.net.repository.ApiRepository
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.entity.UpgradeInfo
import com.microtech.aidexx.common.scope
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.NetUtil
import com.microtech.aidexx.utils.StringUtils
import com.microtech.aidexx.utils.mmkv.MmkvManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.IOException
import java.util.*

private val appUpgradeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

/**
 * app版本检查、下载、进度、安装或者跳转应用市场
 */
object AppUpgradeManager {

    const val TAG = "AppUpgradeManager"
    const val DOWNLOAD_STATUS_DONE = 100
    const val DOWNLOAD_STATUS_ERROR = -1

    /**
     * 标记任务执行状态
     * 1：启动状态
     * 0：停止状态
     */
    private val mUpgradeState: MutableStateFlow<UpgradeInfo?> = MutableStateFlow(null)

    /**
     * 更新进度 <进度，msg>
     */
    private val mUpgradeProgress: MutableStateFlow<Pair<Int, String>?> = MutableStateFlow(null)
    val upgradeProgressFlow: StateFlow<Pair<Int, String>?> = mUpgradeProgress

    private val syncExceptionHandler = CoroutineExceptionHandler { context, throwable ->
        LogUtil.xLogE( "app升级异常 \n ${throwable.stackTraceToString()}", TAG)
        stopUpgrade(true)
    }

    init {
        // 长生命周期下载
        AidexxApp.instance.scope.launch {
            mUpgradeState.collect {
                it?.let {
                    appUpgradeScope.launch(syncExceptionHandler) {

                        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
                            stopUpgrade()
                            mUpgradeProgress.emit(DOWNLOAD_STATUS_ERROR to "no sdcard")
                            return@launch
                        }

                        val fileName = "app_${it.appUpdateInfo!!.info.version}.apk"
                        val downloadPath = getDownloadDir("downloads")
                        // 启动下载
                        ApiRepository.downloadFile(it.appUpdateInfo.info.downloadpath, downloadPath, fileName).collect { ret ->
                            when (ret) {
                                is ApiRepository.NetResult.Loading -> {
                                    mUpgradeProgress.emit(ret.value to "正在下载")
                                }
                                is ApiRepository.NetResult.Success -> {
                                    mUpgradeProgress.emit(DOWNLOAD_STATUS_DONE to ret.result)
                                    stopUpgrade()
                                    installApk(ret.result)
                                }
                                is ApiRepository.NetResult.Failure -> {
                                    LogUtil.xLogE( "download fail ${ret.code}-${ret.msg}", TAG)
                                    mUpgradeProgress.emit(DOWNLOAD_STATUS_ERROR to "${ret.code}-${ret.msg}")
                                    stopUpgrade()
                                }
                            }
                        }
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
    ): UpgradeInfo? = withContext(Dispatchers.IO) {

        if (needCheckNewVersion(isManual)) {
            when (val apiResult = ApiRepository.checkAppUpdate()) {
                is ApiResult.Success -> {
                    MmkvManager.updateAppCheckVersionTime()
                    val info = apiResult.result
                    if (StringUtils.versionCompare(BuildConfig.VERSION_NAME,
                            info.data?.appUpdateInfo?.info?.version ?: "")) {
                        info.data
                    } else null
                }
                is ApiResult.Failure -> null
            }
        } else null
    }

    /**
     * 启动升级
     */
    fun startUpgrade(appUpdateInfo: UpgradeInfo): Boolean {
        val ret = mUpgradeState.compareAndSet(null, appUpdateInfo)
        LogUtil.xLogI("启动升级 ret=$ret", TAG)
        return ret
    }

    /**
     * 结束同步
     * 同步成功及同步异常都需要调用
     */
    private fun stopUpgrade(isFromException: Boolean = false){
        if(isFromException){
            AidexxApp.instance.scope.launch { mUpgradeProgress.emit(DOWNLOAD_STATUS_ERROR to "") }
        }
        mUpgradeState.value = null
        LogUtil.xLogI("结束升级 curState=${mUpgradeState.value}", TAG)
    }

    private fun needCheckNewVersion(isManual: Boolean): Boolean {

        val isCheckedToday = MmkvManager.getAppCheckVersionTime() == Date().getStartOfTheDay().time
        if (!isManual && isCheckedToday) {
            LogUtil.xLogE("当天已经检测过", TAG)
            return false
        }

        if (!NetUtil.isNetAvailable(getContext())) {
            LogUtil.xLogE("网络不可用", TAG)
            return false
        }
        return true
    }

    /**
     * 安装APK文件
     */
    private fun installApk(path: String) {
        val apkFile = File(path)
        setPermission(apkFile.path)
        if (!apkFile.exists()) {
            return
        }

        // 通过Intent安装APK文件
        val i = Intent(Intent.ACTION_VIEW)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val contentUri = FileProvider.getUriForFile(
                getContext(),
                getContext().packageName + ".FileProvider",
                apkFile
            )
            i.setDataAndType(contentUri, "application/vnd.android.package-archive")
        } else {
            i.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive")
        }

        getContext().startActivity(i)
    }



    private fun setPermission(filePath: String) {
        val command = "chmod 777 $filePath"
        val runtime = Runtime.getRuntime()
        try {
            runtime.exec(command)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getDownloadDir(subDir: String?): String {
        var sdpath: String = getContext().externalCacheDir!!.absolutePath

        sdpath = if (subDir != null) {
            "$sdpath/aidex/$subDir/"
        } else {
            "$sdpath/aidex/"
        }
        val saveDir = File(sdpath)
        if (!saveDir.exists()) {
            saveDir.mkdirs()
        }
        return sdpath
    }

}