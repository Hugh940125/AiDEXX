package com.microtech.aidexx.utils

import com.getui.gtc.base.util.CommonUtil.getExternalFilesDir
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.common.getContext
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.ui.setting.log.FeedbackUtil
import com.tencent.mars.xlog.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

/**
 *@date 2023/2/15
 *@author Hugh
 *@desc
 */
class LogUtil {
    companion object{
        private const val COMMON = "AiDEX-X"
        private const val HTTP = "Request"

        @JvmStatic
        fun eAiDEX(msg: String) {
            Log.e(COMMON, msg)
        }

        fun xLogI(msg: String, tag: String = COMMON) {
            Log.i(tag, msg)
        }

        fun xLogE(msg: String, tag: String = COMMON) {
            Log.e(tag, msg)
        }

        @JvmStatic
        fun eHttp(msg: String) {
            Log.e(HTTP, msg)
        }


        // 仅输出到控制台
        @JvmStatic
        fun d(msg: String, tag: String = COMMON) {
            android.util.Log.d(tag, msg)
        }
        @JvmStatic
        fun i(msg: String, tag: String = COMMON) {
            android.util.Log.i(tag, msg)
        }
        @JvmStatic
        fun e(msg: String, tag: String = COMMON) {
            android.util.Log.e(tag, msg)
        }

        fun uploadLog(scope: CoroutineScope = GlobalScope, mute: Boolean = false) {
            Log.appenderFlushSync(true)
            val externalFile = getExternalFilesDir(null)?.absolutePath
            val logPath = "$externalFile${File.separator}aidex"
            val logFile = File("${logPath}${File.separator}log")
            val userId = UserInfoManager.instance().userId()
            val deviceName = DeviceInfoHelper.deviceName()
            val installVersion = DeviceInfoHelper.installVersion(getContext())
            val osVersion = DeviceInfoHelper.osVersion()
            val sn = TransmitterManager.instance().getDefault()?.entity?.deviceSn ?: ""
            val zipFileName = "AiDEX${installVersion}_${deviceName}_${osVersion}_${sn}_${userId}.zip"
            if (logFile.isDirectory) {
                scope.launch(Dispatchers.IO) {
                    FeedbackUtil.zipAndUpload(getContext(), logFile, logPath, zipFileName, mute)
                }
            }
        }

    }
}