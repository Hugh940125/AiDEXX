package com.microtech.aidexx.ui.welcome

import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.base.BaseWelcomeActivity
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.ui.account.LoginActivity
import com.microtech.aidexx.ui.main.MainActivity
import com.microtech.aidexx.utils.ActivityUtil

class WelcomeActivity : BaseWelcomeActivity<BaseViewModel>() {

    override fun afterAgreeUserProtocal() {
        if (UserInfoManager.instance().isLogin()) {
            ActivityUtil.toActivity(this, MainActivity::class.java)
        } else {
            ActivityUtil.toActivity(this, LoginActivity::class.java)
        }
        finish()
    }

}
