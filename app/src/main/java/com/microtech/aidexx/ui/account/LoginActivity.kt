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
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.databinding.ActivityLoginBinding
import com.microtech.aidexx.ui.main.MainActivity
import com.microtech.aidexx.utils.*
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
            val reqContent = hashMapOf(
                "phoneNumber" to user.trim()
            )
            viewModel.getVerCode(reqContent, {
                viewModel.startCountDown()
                Dialogs.dismissWait()
            }, {
                Dialogs.showError(getString(R.string.get_ver_code_fail))
            })
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
            val reqContent = hashMapOf(
                "phoneNumber" to account,
                "verificationCode" to code
            )
            if (account.isEmpty()) {
                ToastUtil.showShort(getString(R.string.account_empty))
            } else {
                login(reqContent)
            }
        } else {
            val reqContent = linkedMapOf(
                "username" to account,
                "password" to EncryptUtils.md5(password)
            )
            if (account.isEmpty() || password.isEmpty()) {
                ToastUtil.showShort(getString(R.string.email_password_empty))
            } else {
                login(reqContent)
            }
        }
    }

    private fun login(map: HashMap<String, String>) {
        if (!NetUtil.isNetAvailable(this)) {
            ToastUtil.showShort(getString(R.string.net_error))
            return
        }
        Dialogs.showWait(getString(R.string.Login_loging))
        viewModel.login(map, { baseResponse ->
            val content = baseResponse.content
            content?.let {
                lifecycleScope.launch {
                    UserInfoManager.instance().onUserLogin(content) {
                        if (it) {
                            downloadData()
                            onLoginSuccess()
                        } else {
                            ToastUtil.showShort(getString(R.string.login_fail))
                        }
                    }
                }
            }
        }, {

        })
    }

    private fun onLoginSuccess() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun downloadData() {
        //{"info":{"code":"100000","msg":"success"},"content":{"pageInfo":{"currentPage":1,"pageSize":200,"sortOrder":"DESC","totalCount":1},"records":[{"id":"4664cccb4894cc835bf61a775ef6c709","recordIndex":1,"guidanceDone":true,"heightUnit":1,"heightUnitStr":"cm","weightUnit":1,"weightUnitStr":"kg","glucoseUnit":1,"glucoseUnitStr":"mmol/L"}]}}
//        viewModel.getUserPreference({
//
//        }, {})
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
}