package com.microtech.aidexx.ui.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.common.equal
import com.microtech.aidexx.common.setDebounceClickListener
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.databinding.ActivityOtherSettingBinding
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity_
import com.microtech.aidexx.ui.account.LoginActivity
import com.microtech.aidexx.utils.DeviceInfoHelper
import com.microtech.aidexx.utils.mmkv.MmkvManager
import com.microtech.aidexx.widget.dialog.DIALOGS_TYPE_VERTICAL
import com.microtech.aidexx.widget.dialog.Dialogs
import com.tencent.mars.xlog.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipOutputStream

class OtherSettingActivity : BaseActivity<BaseViewModel, ActivityOtherSettingBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {

            actionBarOtherSetting.getLeftIcon().setOnClickListener {
                finish()
            }

            settingLogout.setDebounceClickListener {
                Dialogs.showWhether(
                    this@OtherSettingActivity,
                    content = getString(R.string.content_login_exit),
                    confirmBtnText = getString(R.string.logout),
                    btnOrientation = DIALOGS_TYPE_VERTICAL,
                    confirm = {

                        lifecycleScope.launch(Dispatchers.IO) {
                            MmkvManager.saveCustomerServiceIconTop(0)
                            MmkvManager.saveCustomerServiceIconRight(0)
                            MmkvManager.saveCustomerServiceIconBottom(0)
                            MmkvManager.saveCustomerServiceIconLeft(0)

                            UserInfoManager.instance().onUserExit()

                            val intent = Intent(this@OtherSettingActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)

                            finish()
                        }

                    }
                )
            }

            settingUploadLog.setDebounceClickListener {
                Dialogs.showWait(getString(R.string.log_uploading))
                Log.appenderFlushSync(true)
                val externalFile = getExternalFilesDir(null)?.absolutePath
                val logPath = "$externalFile/aidex"
                val logFile = File("${logPath}/log")
                val userId = UserInfoManager.instance().userId()
                val deviceName = DeviceInfoHelper.deviceName()
                val installVersion = DeviceInfoHelper.installVersion(this@OtherSettingActivity)
                val osVersion = DeviceInfoHelper.osVersion()
                val sn = TransmitterManager.instance().getDefault()?.entity?.deviceSn ?: "unknown"
                val zipFileName = "AiDEX_${installVersion}_${deviceName}_${osVersion}_${sn}_${userId}.zip"
                if (logFile.isDirectory) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        //zipAndUpload(this@OtherSettingActivity, logFile, logPath, zipFileName, false)
                    }
                } else {
                    Dialogs.showSuccess(getString(R.string.str_succ))
                }
            }
        }
    }

//    private suspend fun zipAndUpload(
//        context: Context?,
//        logFile: File,
//        logPath: String,
//        zipFileName: String,
//        mute: Boolean
//    ) {
//        //保存数据库
//        val all = ObjectBox.cgmHistoryBox!!.query()
//            .equal(RealCgmHistoryEntity_.userId, UserInfoManager.instance().userId())
//            .build()
//            .find()
//        all.let {
//            val listType = object : TypeToken<ArrayList<RealCgmHistoryEntity_>>() {}.type
//            val dataCgm = Gson().toJson(all, listType)
//            val saveFile = File(logFile, "History.json")
//            if (saveFile.exists()) {
//                saveFile.delete()
//            }
//            val outStream = FileOutputStream(saveFile)
//            outStream.write(dataCgm.toByteArray())
//            outStream.close()
//        }
//        val files = logFile.listFiles()
//        if (files.isNullOrEmpty()) {
//            return
//        }
//        ZipFolder(pathZip, files)
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
//    }

//    fun ZipFolder(pathZip: String, files: Array<File>): String {
//        //创建ZIP
//
//        try {
//            val outZip =
//                ZipOutputStream(
//                    FileOutputStream(
//                        File(
//                            pathZip
//                        )
//                    )
//                )
//            //创建文件
//
//            for (file in files) {
//                ZipFiles(file.parent + File.separator, file.name, outZip)
//            }
//            //压缩
//
//            //完成和关闭
//            outZip.finish()
//            outZip.close()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            WaitDialog.dismiss()
//        }
//        return pathZip
//    }

    override fun getViewBinding(): ActivityOtherSettingBinding {
        return ActivityOtherSettingBinding.inflate(layoutInflater)
    }
}