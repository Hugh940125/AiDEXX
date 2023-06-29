package com.microtech.aidexx.ui.account

import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.databinding.ActivityLoginBinding

class RegisterActivity: BaseActivity<AccountViewModel, ActivityLoginBinding>() {

    override fun getViewBinding(): ActivityLoginBinding =
        ActivityLoginBinding.inflate(layoutInflater)

    companion object {
        private const val TAG = "RegisterActivity"
    }



}