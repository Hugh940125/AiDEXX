package com.microtech.aidexx.ui.setting

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.common.ioScope
import com.microtech.aidexx.common.net.repository.AccountRepository
import com.microtech.aidexx.common.setDebounceClickListener
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.databinding.ActivityOtherSettingBinding
import com.microtech.aidexx.ui.account.LoginActivity
import com.microtech.aidexx.ui.widget.WidgetUpdateManager
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.mmkv.MmkvManager
import com.microtech.aidexx.views.dialog.DIALOGS_TYPE_VERTICAL
import com.microtech.aidexx.views.dialog.Dialogs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccountSecurityActivity : BaseActivity<BaseViewModel, ActivityOtherSettingBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {

            actionBarOtherSetting.getLeftIcon().setOnClickListener {
                finish()
            }

            settingLogout.setDebounceClickListener {
                Dialogs.showWhether(
                    this@AccountSecurityActivity,
                    content = getString(R.string.content_login_exit),
                    confirmBtnText = getString(R.string.logout),
                    btnOrientation = DIALOGS_TYPE_VERTICAL,
                    confirm = {
                        AidexxApp.instance.ioScope.launch {
                            val apiRet = AccountRepository.logout()
                            LogUtil.d("$apiRet")
                        }
                        lifecycleScope.launch(Dispatchers.IO) {
                            MmkvManager.saveCustomerIconPosition(0, 0, 0, 0)
                            TransmitterManager.instance().clear()
                            WidgetUpdateManager.instance().update(this@AccountSecurityActivity)
                            UserInfoManager.instance().onUserExit()
                            val intent = Intent(this@AccountSecurityActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    }
                )
            }
        }
    }

    override fun getViewBinding(): ActivityOtherSettingBinding {
        return ActivityOtherSettingBinding.inflate(layoutInflater)
    }
}