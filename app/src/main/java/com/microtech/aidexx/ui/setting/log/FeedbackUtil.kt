package com.microtech.aidexx.ui.setting.log

import android.content.Context
import com.microtech.aidexx.widget.dialog.Dialogs
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object FeedbackUtil {

    suspend fun zipAndUpload(
        context: Context,
        logFile: File,
        logPath: String,
        zipFileName: String,
        mute: Boolean
    ) {
        val files = logFile.listFiles()
        if (files.isNullOrEmpty()) {
            return
        }
        val fileList = files.filter { file -> file.name.endsWith("xlog", true) }.toMutableList()
        val dbFile =
            File("${context.filesDir.absolutePath}${File.separator}objectbox${File.separator}objectbox${File.separator}data.mdb")
        if (dbFile.exists()) {
            fileList.add(dbFile)
        }
        zipFolder(logPath, zipFileName, fileList)
//        if (File(pathZip).exists()) {
//            val okHttpClient = OkHttpClient()
//            val file = File(pathZip)
//            val requestBody = MultipartBody.Builder()
//                .setType(MultipartBody.ALTERNATIVE)
//                .addFormDataPart("appName", "cgms")  // 上传参数
//                .addFormDataPart(
//                    "file",
//                    "AiDEX_A_${AppUtils.getAppVersionName()}_${
//                        TransmitterManager.instance().getDefaultModel()?.entity?.deviceSn
//                    }_${getSimplePhoneOrEmail()}_${getCurrentFormattedTime()}.zip",
//                    file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
//                )   // 上传文件
//                .build()
//            val request = Request.Builder()
//                .url(Url.UPLOAD_LOG)
//                .post(requestBody)//默认就是GET请求，可以不写
//                .build()
//
//            val call = okHttpClient.newCall(request)
//            call.enqueue(object : Callback {
//                override fun onFailure(call: Call, e: IOException) {
//                    WaitDialog.dismiss()
//                    LogUtils.error("日志上传失败-$e")
//                }
//
//                override fun onResponse(call: Call, response: Response) {
//                    LogUtils.error("日志上传成功")
//                    if (!mute) {
//                        Handler(Looper.getMainLooper()).post {
//                            TipDialog.show(
//                                context,
//                                context?.resources?.getString(R.string.str_succ),
//                                TipDialog.TYPE.SUCCESS
//                            ).onDismissListener =
//                                OnDismissListener { }
//                        }
//                    }
//                }
//            })
//        } else {
//            WaitDialog.dismiss()
//        }
    }

    private fun zipFolder(pathZip: String, zipFileName: String, files: MutableList<File>): String {
        try {
            val outZip = ZipOutputStream(FileOutputStream(File("${pathZip}/${zipFileName}")))
            for (file in files) {
                zipFiles("${file.parent}${File.separator}", file.name, outZip)
            }
            outZip.finish()
            outZip.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Dialogs.dismissWait()
        }
        return pathZip
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
        }
    }
}