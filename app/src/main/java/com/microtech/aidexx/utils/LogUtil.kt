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
    }
}