package com.microtech.aidexx.ui.setting

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.common.equal
import com.microtech.aidexx.common.ioScope
import com.microtech.aidexx.common.net.repository.AccountRepository
import com.microtech.aidexx.common.setDebounceClickListener
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.databinding.ActivityOtherSettingBinding
import com.microtech.aidexx.ui.account.LoginActivity
import com.microtech.aidexx.ui.setting.log.FeedbackUtil
import com.microtech.aidexx.utils.DeviceInfoHelper
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.mmkv.MmkvManager
import com.microtech.aidexx.widget.dialog.DIALOGS_TYPE_VERTICAL
import com.microtech.aidexx.widget.dialog.Dialogs
import com.tencent.mars.xlog.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

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

                        AidexxApp.instance.ioScope.launch {
                            val apiRet = AccountRepository.logout()
                            LogUtil.d("$apiRet")
                        }

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
                val logPath = "$externalFile${File.separator}aidex"
                val logFile = File("${logPath}${File.separator}log")
                val userId = UserInfoManager.instance().userId()
                val deviceName = DeviceInfoHelper.deviceName()
                val installVersion = DeviceInfoHelper.installVersion(this@OtherSettingActivity)
                val osVersion = DeviceInfoHelper.osVersion()
                val sn = TransmitterManager.instance().getDefault()?.entity?.deviceSn ?: "unknown"
                val zipFileName = "AiDEX${installVersion}_${deviceName}_${osVersion}_${sn}_${userId}.zip"
                if (logFile.isDirectory) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        FeedbackUtil.zipAndUpload(this@OtherSettingActivity, logFile, logPath, zipFileName, false)
                    }
                } else {
                    Dialogs.showSuccess(getString(R.string.str_succ))
                }
            }
        }
    }

    override fun getViewBinding(): ActivityOtherSettingBinding {
        return ActivityOtherSettingBinding.inflate(layoutInflater)
    }
}