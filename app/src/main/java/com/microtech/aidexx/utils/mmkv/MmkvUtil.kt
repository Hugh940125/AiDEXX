package com.microtech.aidexx.utils.mmkv

import android.os.Parcelable
import com.tencent.mmkv.MMKV

class MmkvUtil {
    companion object {
        private val INSTANCE = MMKV.defaultMMKV()

        fun encodeString(key: String, value: String) {
            INSTANCE.encode(key, value)
        }

        fun decodeString(key: String, defValue: String): String {
            return INSTANCE.decodeString(key, defValue)!!
        }

        fun encodeBoolean(key: String, value: Boolean) {
            INSTANCE.encode(key, value)
        }

        fun decodeBoolean(key: String, defValue: Boolean): Boolean {
            return INSTANCE.decodeBool(key, defValue)
        }

        fun decodeLong(key: String, defValue: Long): Long {
            return INSTANCE.decodeLong(key, defValue)
        }

        fun encodeLong(key: String, value: Long) {
            INSTANCE.encode(key, value)
        }

        fun decodeInt(key: String, defValue: Int): Int {
            return INSTANCE.decodeInt(key, defValue)
        }

        fun encodeInt(key: String, value: Int) {
            INSTANCE.encode(key, value)
        }

        fun decodeFloat(key: String, defValue: Float): Float {
            return INSTANCE.decodeFloat(key, defValue)
        }

        fun encodeFloat(key: String, value: Float) {
            INSTANCE.encode(key, value)
        }

        fun <T : Parcelable> decodeParcelable(key: String, clazz: Class<T>): T? {
            return INSTANCE.decodeParcelable(key, clazz)
        }

        fun encodeParcelable(key: String, parcelable: Parcelable) {
            INSTANCE.encode(key, parcelable)
        }
    }
}