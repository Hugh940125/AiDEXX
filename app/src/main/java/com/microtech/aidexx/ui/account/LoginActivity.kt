package com.microtech.aidexx.ui.account

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.view.View
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.databinding.ActivityLoginBinding
import com.microtech.aidexx.utils.LOGIN
import com.microtech.aidexx.utils.StringUtils

class LoginActivity : BaseActivity<AccountViewModel, ActivityLoginBinding>(), View.OnClickListener {

    private var isLoginByVerCode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.loginActionBar.getLeftIcon().setOnClickListener {
            finish()
        }
        binding.tvExchange.setOnClickListener(this)
        binding.loginByCode.btnGetVerCode.setOnClickListener(this)
        viewModel.timeLeft.observe(this) {
            if (it.first) {
                binding.loginByCode.btnGetVerCode.isClickable = false
                binding.loginByCode.btnGetVerCode.text =
                    resources.getString(R.string.bt_retry, "${it.second}s ")
            } else {
                binding.loginByCode.btnGetVerCode.isClickable = true
                binding.loginByCode.btnGetVerCode.text =
                    resources.getString(R.string.bt_retry, "")
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
                viewModel.startCountDown()
            }
        }
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