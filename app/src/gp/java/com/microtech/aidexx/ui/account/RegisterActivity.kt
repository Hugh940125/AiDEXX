package com.microtech.aidexx.ui.account

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.common.LOGIN_TYPE_EMAIL_VER_CODE
import com.microtech.aidexx.common.toast
import com.microtech.aidexx.common.toastShort
import com.microtech.aidexx.databinding.ActivityRegisterBinding
import com.microtech.aidexx.ui.main.MainActivity
import com.microtech.aidexx.utils.EncryptUtils
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.NetUtil
import com.microtech.aidexx.utils.StringUtils
import com.microtech.aidexx.utils.ToastUtil
import com.microtech.aidexx.views.dialog.Dialogs
import kotlinx.coroutines.launch

class RegisterActivity: BaseActivity<AccountViewModel, ActivityRegisterBinding>() {

    override fun getViewBinding(): ActivityRegisterBinding =
        ActivityRegisterBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
        initEvent()
    }

    private fun initView() {

        binding.apply {

            actionBar.getLeftIcon().setOnClickListener { finish() }
            btnGetCode.setOnClickListener { getVerCode() }
            buttonRegist.setOnClickListener { checkAndRegister() }

        }

    }

    private fun initEvent() {
        viewModel.timeLeft.observe(this) {
            binding.btnGetCode.setTextColor(getColor(if (it.first) R.color.btnVerCodeColor else R.color.light_colorAccent))
            binding.btnGetCode.isClickable = !it.first
            binding.btnGetCode.text = getString(R.string.bt_retry, "${it.second}s ")
        }
    }

    private fun getVerCode() {
        val user = binding.etUsername.text.toString().trim()
        if (!user.isNullOrEmpty() && StringUtils.checkEmail(user) && user.indexOf(" ") == -1) {
            Dialogs.showWait(getString(R.string.loading))

            lifecycleScope.launch {
                if (viewModel.sendRegisterEmailVerificationCode(user.trim())) {
                    Dialogs.dismissWait()
                } else {
                    Dialogs.showError(getString(R.string.get_ver_code_fail))
                }
            }
        } else {
            resources.getString(R.string.email_error).toast()
        }
    }

    private fun checkAndRegister() {
        val regexPsd = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*\$"
        val account = binding.etUsername.text.toString().trim()
        val password = binding.etPwd.text.toString().trim()
        val passwordConfirm = binding.etPwdConfirm.text.toString().trim()
        val code = binding.etCode.text.toString().trim()

        if (code.isEmpty()) {
            getString(R.string.code_empty).toastShort()
            return
        }
        if (!binding.checkProtocal.isChecked) {
            Dialogs.showMessage(this, content = getString(R.string.user_check))
            return
        }
        if (account.isEmpty()) {
            getString(R.string.email_error).toastShort()
            return
        }

        if (password.isNullOrEmpty()) {
            Dialogs.showMessage(this, content = getString(R.string.password_error))
            return
        }

        if (!(Regex(regexPsd).matches(password)) || password.length < 8) {
            Dialogs.showMessage(this, content = getString(R.string.password_error))
            return
        }

        if (password != passwordConfirm) {
            Dialogs.showMessage(this, content = getString(R.string.password_uneq))
            return
        }

        if (!account.isNullOrEmpty() && StringUtils.checkEmail(account) && account.indexOf(" ") == -1) {
            Dialogs.showMessage(this, content = getString(R.string.email_error))
            return
        }

        register(account, code, password)
    }

    private fun register(name: String, password: String, verCode: String) {
        if (!NetUtil.isNetAvailable(this)) {
            ToastUtil.showShort(getString(R.string.net_error))
            return
        }
        Dialogs.showWait(getString(R.string.Login_loging))

        lifecycleScope.launch {
            viewModel.login(name, EncryptUtils.md5(password), verCode, LOGIN_TYPE_EMAIL_VER_CODE).collect {
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
        private const val TAG = "RegisterActivity"
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.timeLeft.removeObservers(this)
    }


}