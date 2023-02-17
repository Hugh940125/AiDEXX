package com.microtech.aidexx.ui.welcome

import android.os.Bundle
import android.view.View
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.databinding.ActivityWelcomeBinding
import com.microtech.aidexx.ui.main.MainActivity
import com.microtech.aidexx.utils.ActivityUtil
import com.microtech.aidexx.utils.mmkv.MmkvManager

class WelcomeActivity : BaseActivity<BaseViewModel, ActivityWelcomeBinding>() {

    override fun getViewBinding(): ActivityWelcomeBinding {
        return ActivityWelcomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
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
//            goActivity(bundle, LoginActivity::class.java)
            finish()
        }
    }

}
