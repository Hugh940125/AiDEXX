package com.microtech.aidexx.ui.account

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.common.LOGIN_TYPE_PWD
import com.microtech.aidexx.common.LOGIN_TYPE_VER_CODE
import com.microtech.aidexx.common.LoginType
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

class LoginActivity : BaseActivity<AccountViewModel, ActivityLoginBinding>(), View.OnClickListener {

    private var isLoginByVerCode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.loginActionBar.getLeftIcon().setOnClickListener {
            ActivityUtil.toSystemHome(this)
        }
        binding.etUsername.inputType = InputType.TYPE_CLASS_PHONE
        binding.tvExchange.setOnClickListener(this)
        binding.loginByCode.btnGetVerCode.setOnClickListener(this)
        binding.btnLogin.setOnClickListener(this)
        binding.loginByPwd.tvForget.setOnClickListener(this)

        viewModel.timeLeft.observe(this) {
            if (it.first) {
                binding.loginByCode.btnGetVerCode.setTextColor(
                    ThemeManager.getTypeValue(
                        this,
                        R.attr.textColorHint
                    )
                )
                binding.loginByCode.btnGetVerCode.isClickable = false
                binding.loginByCode.btnGetVerCode.text =
                    getString(R.string.bt_retry, "${it.second}s ")
            } else {
                binding.loginByCode.btnGetVerCode.setTextColor(
                    ThemeManager.getTypeValue(
                        this,
                        R.attr.appColorAccent
                    )
                )
                binding.loginByCode.btnGetVerCode.isClickable = true
                binding.loginByCode.btnGetVerCode.text =
                    getString(R.string.bt_retry, "")
            }
        }
        binding.loginByCode.txtUserProtocol.text = StringUtils.initProtocol(
            this, LOGIN,
            SpannableStringBuilder()
        )
        binding.loginByCode.txtUserProtocol.movementMethod = LinkMovementMethod.getInstance()


    }

    override fun getViewBinding(): ActivityLoginBinding {
        return ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.tvExchange -> {
                changeLoginMethod()
            }
            binding.loginByCode.btnGetVerCode -> {
                getVerCode()
            }
            binding.btnLogin -> {
                checkLoginInfo()
            }
            binding.loginByPwd.tvForget -> {
                startActivity(Intent(this, ForgetPwdActivity::class.java))
            }
        }
    }

    private fun getVerCode() {
        val user = binding.etUsername.text.toString().trim()
        if (user.isNotEmpty() && user.length >= 11) {
            Dialogs.showWait(getString(R.string.loading))

            lifecycleScope.launch {
                if (viewModel.sendRegisterPhoneVerificationCode(user.trim())) {
                    viewModel.startCountDown()
                    Dialogs.dismissWait()
                } else {
                    Dialogs.showError(getString(R.string.get_ver_code_fail))
                }
            }
        } else {
            ToastUtil.showShort(getString(R.string.phone_error))
        }
    }

    private fun checkLoginInfo() {
        val account = binding.etUsername.text.toString().trim()
        val password = binding.loginByPwd.etPwd.text.toString().trim()
        val code = binding.loginByCode.etCode.text.toString().trim()
        if (isLoginByVerCode) {
            if (code.isEmpty()) {
                ToastUtil.showShort(getString(R.string.code_empty))
                return
            }
            if (!binding.loginByCode.checkProtocol.isChecked) {
                Dialogs.showMessage(this, content = getString(R.string.user_check))
                return
            }
            if (account.isEmpty()) {
                ToastUtil.showShort(getString(R.string.account_empty))
            } else {
                login(account, code, LOGIN_TYPE_VER_CODE)
            }
        } else {
            if (account.isEmpty() || password.isEmpty()) {
                ToastUtil.showShort(getString(R.string.email_password_empty))
            } else {
                login(account, EncryptUtils.md5(password), LOGIN_TYPE_PWD)
            }
        }
    }

    /**
     * @param name 手机号或者邮箱
     * @param type 1-验证码登录 2-密码登录
     */
    private fun login(name: String, pwdOrCode: String, @LoginType type: Int) {
        if (!NetUtil.isNetAvailable(this)) {
            ToastUtil.showShort(getString(R.string.net_error))
            return
        }
        Dialogs.showWait(getString(R.string.Login_loging))

        lifecycleScope.launch {
            viewModel.login(name, pwdOrCode, type).collect {
                Dialogs.dismissWait()
                when (it.first) {
                    1 -> Dialogs.showWait("假装正在-"+getString(R.string.download_data))
                    2 -> onLoginSuccess()
                    -1 -> ToastUtil.showShort(getString(R.string.login_fail))
                }
                LogUtil.d(it.second, TAG)
            }
        }
    }

    private fun onLoginSuccess() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun changeLoginMethod() {
        if (isLoginByVerCode) {
            binding.loginByCode.root.visibility = View.GONE
            binding.loginByPwd.root.visibility = View.VISIBLE
            binding.tvExchange.text = getString(R.string.login_content_2)
        } else {
            binding.loginByCode.root.visibility = View.VISIBLE
            binding.loginByPwd.root.visibility = View.GONE
            binding.tvExchange.text = getString(R.string.login_exchange)
        }
        isLoginByVerCode = !isLoginByVerCode
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}