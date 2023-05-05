package com.microtech.aidexx.ui.account

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.common.LOGIN_TYPE_PWD
import com.microtech.aidexx.common.LOGIN_TYPE_VER_CODE
import com.microtech.aidexx.common.LoginType
import com.microtech.aidexx.common.toastShort
import com.microtech.aidexx.databinding.ActivityLoginBinding
import com.microtech.aidexx.ui.main.MainActivity
import com.microtech.aidexx.utils.ActivityUtil
import com.microtech.aidexx.utils.EncryptUtils
import com.microtech.aidexx.utils.LOGIN
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.NetUtil
import com.microtech.aidexx.utils.StringUtils
import com.microtech.aidexx.utils.ThemeManager
import com.microtech.aidexx.utils.ToastUtil
import com.microtech.aidexx.widget.dialog.Dialogs
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity<AccountViewModel, ActivityLoginBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.actionBar.getLeftIcon().setOnClickListener {
            finish()
        }

        binding.buttonLogin.setOnClickListener {
            checkLoginInfo()
        }

        binding.tvForget.setOnClickListener {
            startActivity(Intent(this, ForgetPwdActivity::class.java))
        }
    }

    override fun getViewBinding(): ActivityLoginBinding {
        return ActivityLoginBinding.inflate(layoutInflater)
    }

    private fun checkLoginInfo() {
        val account = binding.etUsername.text.toString().trim()
        val password = binding.etPwd.text.toString().trim()

        if (account.isEmpty() || password.isEmpty()) {
            ToastUtil.showShort(getString(R.string.email_password_empty))
        } else {
            login(account, EncryptUtils.md5(password), LOGIN_TYPE_PWD)
        }
    }

    /**
     * @param name 手机号或者邮箱
     * @param type 1-验证码登录 2-密码登录
     */
    private fun login(name: String, password: String, @LoginType type: Int) {
        if (!NetUtil.isNetAvailable(this)) {
            ToastUtil.showShort(getString(R.string.net_error))
            return
        }
        Dialogs.showWait(getString(R.string.Login_loging))

        lifecycleScope.launch {
            viewModel.login(name, password, "", type).collect {
                Dialogs.dismissWait()
                when (it.first) {
                    1 -> Dialogs.showWait(getString(R.string.download_data))
                    2 -> onLoginSuccess()
                    -1 -> getString(R.string.login_fail).toastShort()
                    -2 -> it.second.toastShort()
                }
                LogUtil.d(it.second, TAG)
            }
        }
    }

    private fun onLoginSuccess() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        private const val TAG = "LoginActivity"
    }

}