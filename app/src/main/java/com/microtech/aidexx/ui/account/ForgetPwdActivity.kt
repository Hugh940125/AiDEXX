package com.microtech.aidexx.ui.account


import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.common.isNumber
import com.microtech.aidexx.common.toast
import com.microtech.aidexx.common.toastShort
import com.microtech.aidexx.databinding.ActivityForgetPwdBinding
import com.microtech.aidexx.utils.ThemeManager
import com.microtech.aidexx.widget.dialog.Dialogs
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ForgetPwdActivity : BaseActivity<AccountViewModel, ActivityForgetPwdBinding>() {

    override fun getViewBinding(): ActivityForgetPwdBinding {
        return ActivityForgetPwdBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.run {
            actionBar.getLeftIcon().setOnClickListener { finish() }
            btnGetCode.setOnClickListener { getVerCode() }

            viewModel.timeLeft.observe(this@ForgetPwdActivity) {
                if (it.first) {
                    btnGetCode.setTextColor(
                        ThemeManager.getTypeValue(
                            this@ForgetPwdActivity,
                            R.attr.textColorHint
                        )
                    )
                    btnGetCode.isClickable = false
                    btnGetCode.text = getString(R.string.bt_retry, "${it.second}s ")
                } else {
                    btnGetCode.setTextColor(
                        ThemeManager.getTypeValue(
                            this@ForgetPwdActivity,
                            R.attr.appColorAccent
                        )
                    )
                    btnGetCode.isClickable = true
                    btnGetCode.text = getString(R.string.bt_retry, "")
                }
            }
        }

        val regexPsd = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*\$"
        binding.buttonRegist.setOnClickListener {
            val user = binding.etUsername.text.toString()
            val pwd = binding.etPwd.text.toString()
            val pwdConfirm = binding.etPwdConfirm.text.toString()
            val eCode = binding.etCode.text.toString()

            if (pwd.isEmpty()) {
                Dialogs.showMessage(this,
                    content = resources.getString(R.string.password_null))
                return@setOnClickListener
            }

            if (!(Regex(regexPsd).matches(pwd)) || pwd.length < 8) {
                Dialogs.showMessage(this,
                    content = resources.getString(R.string.password_error))
                return@setOnClickListener
            }

            if (pwd != pwdConfirm) {
                Dialogs.showMessage(this,
                    content = resources.getString(R.string.password_uneq))
                return@setOnClickListener
            }

            if (user.isNotEmpty() && user.isNumber() && user.indexOf(" ") == -1) {

                lifecycleScope.launch {
                    viewModel.changePWD(user, pwd, eCode).collectLatest {
                        if (it.first) {
                            finish()
                        } else {
                            it.second.toast()
                        }
                    }
                }

            } else {
                resources.getString(R.string.phone_error).toast()
            }
        }
    }

    private fun getVerCode() {
        val user = binding.etUsername.text.toString().trim()
        if (user.isNotEmpty() && user.isNumber() && user.indexOf(" ") == -1) {
            Dialogs.showWait(getString(R.string.loading))

            lifecycleScope.launch {
                viewModel.getChangePWDVerifyCode(user).collectLatest {
                    if (it) {
                        viewModel.startCountDown()
                        Dialogs.dismissWait()
                    } else {
                        Dialogs.showError(getString(R.string.get_ver_code_fail))
                    }
                }
            }

        } else {
            getString(R.string.phone_error).toastShort()
        }
    }

}