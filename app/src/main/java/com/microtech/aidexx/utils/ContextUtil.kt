package com.microtech.aidexx.utils

import android.content.Context
import android.content.res.Resources

class ContextUtil {
    companion object {
        private lateinit var res: Resources

        fun init(context: Context) {
            res = context.resources
        }

        fun getResources(): Resources {
            return res
        }
    }
}