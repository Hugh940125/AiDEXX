package com.microtech.aidexx.ui.setting

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.setDebounceClickListener
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.databinding.ActivityOtherSettingBinding
import com.microtech.aidexx.ui.account.LoginActivity
import com.microtech.aidexx.utils.mmkv.MmkvManager
import com.microtech.aidexx.widget.dialog.DIALOGS_TYPE_VERTICAL
import com.microtech.aidexx.widget.dialog.Dialogs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        }

    }

    override fun getViewBinding(): ActivityOtherSettingBinding {
        return ActivityOtherSettingBinding.inflate(layoutInflater)
    }
}