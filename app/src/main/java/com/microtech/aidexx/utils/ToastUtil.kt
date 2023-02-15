package com.microtech.aidexx.utils

import android.widget.Toast
import com.microtech.aidexx.AidexxApp

object ToastUtil {

    fun showLong(info: String) {
        Toast.makeText(AidexxApp.instance, info, Toast.LENGTH_LONG).show()
    }

    fun showShort(info: String) {
        Toast.makeText(AidexxApp.instance, info, Toast.LENGTH_SHORT).show()
    }
}
