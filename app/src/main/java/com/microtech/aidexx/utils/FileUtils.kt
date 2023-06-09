package com.microtech.aidexx.utils

import com.microtech.aidexx.common.getContext
import java.io.File
import java.io.IOException

object FileUtils {

    fun setPermission(filePath: String) {
        val command = "chmod 777 $filePath"
        val runtime = Runtime.getRuntime()
        try {
            runtime.exec(command)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getDownloadDir(subDir: String?): String {
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

    fun isFileExists(filePath: String): Boolean {
        return isFileExists(File(filePath))
    }

    fun isFileExists(file: File): Boolean {
        return file.isFile && file.exists()
    }

}