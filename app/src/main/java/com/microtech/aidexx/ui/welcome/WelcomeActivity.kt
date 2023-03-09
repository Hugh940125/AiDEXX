package com.microtech.aidexx.ui.welcome

import android.os.Bundle
import android.view.View
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.databinding.ActivityWelcomeBinding
import com.microtech.aidexx.ui.account.LoginActivity
import com.microtech.aidexx.ui.main.MainActivity
import com.microtech.aidexx.utils.ActivityUtil
import com.microtech.aidexx.utils.ThemeManager
import com.microtech.aidexx.utils.mmkv.MmkvManager
import com.microtech.aidexx.widget.dialog.x.DialogX

class WelcomeActivity : BaseActivity<BaseViewModel, ActivityWelcomeBinding>() {

    override fun getViewBinding(): ActivityWelcomeBinding {
        return ActivityWelcomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        when (ThemeManager.theme.id) {
            0 -> DialogX.globalTheme = DialogX.THEME.DARK
            1 -> DialogX.globalTheme = DialogX.THEME.LIGHT
        }
        initView()
    }

    private fun initView() {
        val appFirstLaunch = MmkvManager.isAppFirstLaunch()
        if (appFirstLaunch) {
            binding.viewAgreeProtocal.visibility = View.VISIBLE
            binding.viewAgreeProtocal.onClick = {
                MmkvManager.saveAppLaunched()
                greenLight()
            }
        } else {
            greenLight()
        }
    }

    private fun greenLight() {
        if (UserInfoManager.instance().isLogin()) {
            ActivityUtil.toActivity(this, MainActivity::class.java)
            finish()
        } else {
            ActivityUtil.toActivity(this, LoginActivity::class.java)
            finish()
        }
    }

}
