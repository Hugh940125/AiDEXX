package com.microtech.aidexx.ui.welcome

import android.content.Intent
import androidx.core.view.isVisible
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.base.BaseWelcomeActivity
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.ui.account.LoginActivity
import com.microtech.aidexx.ui.account.RegisterActivity
import com.microtech.aidexx.ui.main.MainActivity
import com.microtech.aidexx.utils.ActivityUtil

class WelcomeActivity : BaseWelcomeActivity<BaseViewModel>() {

    override fun afterAgreeUserProtocal() {
        if (UserInfoManager.instance().isLogin()) {
            ActivityUtil.toActivity(this, MainActivity::class.java)
            finish()
        } else {
            binding.apply {
                buttonLogin.setOnClickListener {
                    startActivity(Intent(this@WelcomeActivity, LoginActivity::class.java))
                }
                buttonRegist.setOnClickListener {
                    startActivity(Intent(this@WelcomeActivity, RegisterActivity::class.java))
                }
                buttonLogin.isVisible = true
                buttonRegist.isVisible = true
            }
        }
    }

}
