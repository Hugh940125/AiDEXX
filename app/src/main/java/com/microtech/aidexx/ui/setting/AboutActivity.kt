package com.microtech.aidexx.ui.setting

import android.graphics.Color
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.BuildConfig
import com.microtech.aidexx.IntentKey
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.common.setDebounceClickListener
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.data.AppUpgradeManager
import com.microtech.aidexx.databinding.ActivityAboutBinding
import com.microtech.aidexx.ui.setting.log.FeedbackUtil
import com.microtech.aidexx.ui.upgrade.AppUpdateFragment
import com.microtech.aidexx.ui.web.WebActivity
import com.microtech.aidexx.utils.DeviceInfoHelper
import com.microtech.aidexx.widget.dialog.Dialogs
import com.tencent.mars.xlog.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class AboutActivity : BaseActivity<BaseViewModel, ActivityAboutBinding>() {

    override fun getViewBinding(): ActivityAboutBinding {
        return ActivityAboutBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.run {
            actionBar.getLeftIcon().setOnClickListener {
                finish()
            }

            launcher.setOnClickListener{
            }

            settingSoftVersion.setValue("v${BuildConfig.VERSION_NAME}")
            settingName.setValue(getString(R.string.software_name))
            settingOpenVersion.setValue("v1")
            settingType.setValue("AiDEX-X")
            settingService.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(
                    IntentKey.WEB_URL,
                    resources.getString(R.string.Terms_of_Service_url)
                )
                bundle.putString(
                    IntentKey.WEB_TITLE,
                    resources.getString(R.string.Terms_of_Service)
                )
                startActivity(bundle, WebActivity::class.java)
            }

            settingCheckVersion.setOnClickListener {
                lifecycleScope.launch {
                    AppUpgradeManager.fetchVersionInfo(true)?.let {
                        AppUpdateFragment(it).show(supportFragmentManager, AppUpdateFragment.TAG)
                    }
                }
            }

            settingProtocal.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(
                    IntentKey.WEB_URL,
                    resources.getString(R.string.Privacy_Policy_url)
                )
                bundle.putString(IntentKey.WEB_TITLE, resources.getString(R.string.Privacy_Policy))
                startActivity(bundle, WebActivity::class.java)
            }
        }

        lifecycleScope.launch {
            AppUpgradeManager.fetchVersionInfo(true)?.let {
                binding.settingCheckVersion.setValue(null)
                val textVersion = binding.settingCheckVersion.getSecondTextView()
                textVersion.text = getString(R.string.txt_new)
                textVersion.setTextColor(Color.WHITE)
                textVersion.textSize = 13f
                textVersion.setBackgroundResource(R.drawable.shape_version_new); textVersion.setPadding(
                18,
                2,
                18,
                2
            )
            } ?:let {
                binding.settingCheckVersion.setValue(getString(R.string.verson_last))
            }
        }

        binding.settingUploadLog.setDebounceClickListener {
            Dialogs.showWait(getString(R.string.log_uploading))
            Log.appenderFlushSync(true)
            val externalFile = getExternalFilesDir(null)?.absolutePath
            val logPath = "$externalFile${File.separator}aidex"
            val logFile = File("${logPath}${File.separator}log")
            val userId = UserInfoManager.instance().userId()
            val deviceName = DeviceInfoHelper.deviceName()
            val installVersion = DeviceInfoHelper.installVersion(this@AboutActivity)
            val osVersion = DeviceInfoHelper.osVersion()
            val sn = TransmitterManager.instance().getDefault()?.entity?.deviceSn ?: "unknown"
            val zipFileName = "AiDEX${installVersion}_${deviceName}_${osVersion}_${sn}_${userId}.zip"
            if (logFile.isDirectory) {
                lifecycleScope.launch(Dispatchers.IO) {
                    FeedbackUtil.zipAndUpload(this@AboutActivity, logFile, logPath, zipFileName, false)
                }
            } else {
                Dialogs.showSuccess(getString(R.string.str_succ))
            }
        }
    }
}