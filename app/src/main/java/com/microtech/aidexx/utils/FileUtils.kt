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

    fun delete(path: String): Boolean = delete(File(path))

    fun delete(file: File?): Boolean {
        if (file == null) return false
        return if (file.isDirectory) {
            deleteDir(file)
        } else deleteFile(file)
    }

    private fun deleteFile(file: File?): Boolean {
        return file != null && (file.exists() || file.isFile && file.delete())
    }

    private fun deleteDir(dir: File): Boolean {
        if (!dir.exists()) return true
        if (!dir.isDirectory) return false
        val files = dir.listFiles()
        if (files != null && files.isNotEmpty()) {
            for (file in files) {
                if (file.isFile) {
                    if (!file.delete()) return false
                } else if (file.isDirectory) {
                    if (!deleteDir(file)) return false
                }
            }
        }
        return dir.delete()
    }

}