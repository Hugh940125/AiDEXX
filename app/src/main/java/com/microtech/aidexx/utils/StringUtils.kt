package com.microtech.aidexx.utils

class StringUtils private constructor() {

    companion object {
        private val INSTANCE = StringUtils()

        fun instance(): StringUtils {
            return INSTANCE
        }
    }

    fun getPrivacyPhone(mobile: String): String {
        return mobile.substring(0, 3) + "****" + mobile.substring(
            7,
            mobile.length
        )
    }
}