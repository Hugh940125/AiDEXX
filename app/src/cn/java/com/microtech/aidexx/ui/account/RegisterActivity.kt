package com.microtech.aidexx.ui.account

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.common.LOGIN_TYPE_EMAIL_VER_CODE
import com.microtech.aidexx.common.toast
import com.microtech.aidexx.common.toastShort
import com.microtech.aidexx.databinding.ActivityLoginBinding
import com.microtech.aidexx.ui.main.MainActivity
import com.microtech.aidexx.utils.EncryptUtils
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.NetUtil
import com.microtech.aidexx.utils.StringUtils
import com.microtech.aidexx.utils.ToastUtil
import com.microtech.aidexx.widget.dialog.Dialogs
import kotlinx.coroutines.launch

class RegisterActivity: BaseActivity<AccountViewModel, ActivityLoginBinding>() {

    override fun getViewBinding(): ActivityLoginBinding =
        ActivityLoginBinding.inflate(layoutInflater)

    companion object {
        private const val TAG = "RegisterActivity"
    }



}