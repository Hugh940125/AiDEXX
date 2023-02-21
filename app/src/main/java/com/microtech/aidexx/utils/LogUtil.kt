package com.microtech.aidexx.utils

import com.tencent.mars.xlog.Log

/**
 *@date 2023/2/15
 *@author Hugh
 *@desc
 */
class LogUtil {
    companion object{
        private const val tag = "AiDEX-X"
        @JvmStatic
        fun eAiDEX(msg: String) {
            Log.e(tag, msg)
        }
    }
}