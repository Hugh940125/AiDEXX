package com.microtech.aidexx.utils

import com.tencent.mars.xlog.Log

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

        @JvmStatic
        fun eHttp(msg: String) {
            Log.e(HTTP, msg)
        }
    }
}