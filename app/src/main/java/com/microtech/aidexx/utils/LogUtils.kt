package com.microtech.aidexx.utils


import android.os.Looper
import com.microtech.aidexx.BuildConfig
import com.tencent.mars.xlog.Log


class LogUtils {


    companion object {

        const val LOG_NAME = "CGMS"
        private const val LOG_BLE = "AiDex"
        const val LOG_NAME_SQL = "CGMS-SQL"

        @JvmStatic
        fun debug(tag: String, msg: String) {
//            if (BuildConfig.DEBUG) {
            Log.d(tag, msg)
//            }
        }


        @JvmStatic
        fun xLog(tag: String, msg: String) {
            com.tencent.mars.xlog.Log.d(tag, msg) // Mars打印
        }

        @JvmStatic
        fun debug(msg: String) {
//            if (BuildConfig.DEBUG) {
            debugLong(LOG_NAME, msg)
//            }
        }

        fun data(msg: String) {
//            if (BuildConfig.DEBUG) {
            debugLong(LOG_NAME_SQL, msg)
//            }
        }


        fun debugLong(tag: String, msg: String) {  //信息太长,分段打印
            var msg = msg
            //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
            //  把4*1024的MAX字节打印长度改为2001字符数
            val max_str_length = 2001 - tag.length
            //大于4000时
            while (msg.length > max_str_length) {
//                Log.d(LOG_NAME, )
                Log.d(LOG_NAME, msg.substring(0, max_str_length)) // Mars打印
                msg = msg.substring(max_str_length)
            }
            //剩余部分
            Log.d(LOG_NAME, msg)
        }


        fun debugErrorLong(tag: String, msg: String) {  //信息太长,分段打印
            var msg = msg
            //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
            //  把4*1024的MAX字节打印长度改为2001字符数
            val max_str_length = 2001 - tag.length
            //大于4000时
            while (msg.length > max_str_length) {
                Log.e(LOG_NAME, msg.substring(0, max_str_length))
                msg = msg.substring(max_str_length)
            }
            //剩余部分
            Log.e(LOG_NAME, msg)
        }

        fun printStackTrace() {
            if (!BuildConfig.DEBUG) {
                return
            }
            try {
                throw Exception()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun debug_s(msg: String) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_NAME, msg)
            }
        }

        fun debug_thread(msg: String) {
            if (BuildConfig.DEBUG) {
                Log.d(
                    LOG_NAME,
                    "isMainTread:" + (Looper.getMainLooper() == Looper.myLooper()) + ",thread:" + Thread.currentThread().name + ":" + msg
                )
            }
        }

        @JvmStatic
        fun error(msg: String) {
//            if (BuildConfig.DEBUG) {
            Log.e(LOG_NAME, msg)
//            }
        }

        @JvmStatic
        fun error(tag: String, msg: String) {
//            if (BuildConfig.DEBUG) {
            Log.e(tag, msg)
//            }
        }

        @JvmStatic
        fun eAiDex(msg: String) {
            Log.e(LOG_BLE, msg)
        }

        fun error_s(msg: String, tr: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(LOG_NAME, msg, tr)
            }
        }
    }


}
