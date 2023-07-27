package com.microtech.aidexx.ui.setting.log

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.microtech.aidexx.BuildConfig
import com.microtech.aidexx.R
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.views.dialog.Dialogs
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import okio.BufferedSink
import okio.source
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object FeedbackUtil {

    fun zipAndUpload(
        context: Context,
        logFile: File,
        logPath: String,
        zipFileName: String,
        mute: Boolean
    ) {
        val files = logFile.listFiles()
        if (files.isNullOrEmpty()) {
            LogUtil.eAiDEX("log files is null or empty")
            Dialogs.showSuccess(context.resources?.getString(R.string.str_succ))
            return
        }
        val fileList = files.filter { file -> file.name.endsWith("xlog", true) }.toMutableList()
        val dbFile =
            File("${context.filesDir.absolutePath}${File.separator}objectbox${File.separator}objectbox${File.separator}data.mdb")
        val mmkvFile =
            File("${context.filesDir.absolutePath}${File.separator}mmkv${File.separator}mmkv.default")
        val mmkvFileCrc =
            File("${context.filesDir.absolutePath}${File.separator}mmkv${File.separator}mmkv.default.crc")
        if (mmkvFile.exists() && mmkvFileCrc.exists()) {
            fileList.add(mmkvFile)
            fileList.add(mmkvFileCrc)
        } else {
            LogUtil.eAiDEX("mmkv backup fail, files not exists")
        }
        if (dbFile.exists()) {
            fileList.add(dbFile)
        }
        val zipFile = zipFolder(logPath, zipFileName, fileList)
        if (zipFile?.exists() == true) {
            val fileRequestBody = object : RequestBody() {
                override fun contentType(): MediaType {
                    return "application/zip".toMediaType()
                }

                @Throws(IOException::class)
                override fun writeTo(sink: BufferedSink) {
                    zipFile.source().use { source ->
                        sink.writeAll(source)
                    }
                }

                override fun contentLength(): Long {
                    return zipFile.length()
                }
            }
            val okHttpClient = OkHttpClient.Builder()
                .callTimeout(5, TimeUnit.MINUTES)
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .retryOnConnectionFailure(true)
                .build()
            val requestBody = MultipartBody.Builder()
                .addFormDataPart("appName", "cgms")  // 上传参数
                .addFormDataPart(
                    "file",
                    zipFileName,
                    fileRequestBody
                )   // 上传文件
                .build()
            val request = Request.Builder()
                .url(BuildConfig.logUploadUrl)
                .post(requestBody)//默认就是GET请求，可以不写
                .build()
            val call = okHttpClient.newCall(request)
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    LogUtil.eAiDEX("Log upload fail:${e.printStackTrace()}")
                    Dialogs.dismissWait()
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!mute) {
                        Handler(Looper.getMainLooper()).post {
                            Dialogs.showSuccess(
                                context.resources?.getString(R.string.str_succ),
                            )
                        }
                    }
                }
            })
        } else {
            Dialogs.dismissWait()
            LogUtil.eAiDEX("Log file not exist")
        }
    }

    private fun zipFolder(pathZip: String, zipFileName: String, files: MutableList<File>): File? {
        var zipFile: File? = null
        try {
            zipFile = File("${pathZip}/${zipFileName}")
            val outZip = ZipOutputStream(FileOutputStream(zipFile))
            for (file in files) {
                zipFiles("${file.parent}${File.separator}", file.name, outZip)
            }
            outZip.finish()
            outZip.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Dialogs.dismissWait()
        }
        return zipFile
    }

    private fun zipFiles(
        folderString: String,
        fileString: String,
        zipOutputSteam: ZipOutputStream
    ) {
        try {
            val file = File(folderString + fileString);
            if (file.isFile) {
                val zipEntry = ZipEntry(fileString);
                val inputStream = FileInputStream(file);
                zipOutputSteam.putNextEntry(zipEntry);
                var len = 0
                val buffer = ByteArray(4096)
                while ((len) != -1) {
                    zipOutputSteam.write(buffer, 0, len)
                    len = inputStream.read(buffer)
                }
                zipOutputSteam.closeEntry();
            } else {
                //文件夹
                val fileList = file.list();
                //没有子文件和压缩
                if (fileList != null) {
                    if (fileList.isEmpty()) {
                        val zipEntry = ZipEntry(fileString + File.separator)
                        zipOutputSteam.putNextEntry(zipEntry)
                        zipOutputSteam.closeEntry()
                    } else {
                        for (it in fileList) {
                            zipFiles(
                                "$folderString$fileString${File.separator}",
                                it,
                                zipOutputSteam
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Dialogs.dismissWait()
        }
    }
}