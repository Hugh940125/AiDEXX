package com.microtech.aidexx.ui.web

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import com.microtech.aidexx.IntentKey
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.databinding.ActivityWebBinding
import com.microtech.aidexx.utils.ProcessUtil
import com.microtech.aidexx.utils.ToastUtil

class WebActivity : BaseActivity<BaseViewModel, ActivityWebBinding>() {

    override fun getViewBinding(): ActivityWebBinding {
        return ActivityWebBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.actionBar.setTitle(intent.getStringExtra(IntentKey.WEB_TITLE))
        binding.webBase.loadUrl((intent.getStringExtra(IntentKey.WEB_URL)))
        val whereFrom = intent.getStringExtra(IntentKey.WHERE_FROM)
        if (!intent.getBooleanExtra(IntentKey.IS_SHOW_ACTIONBAR, true)) {
            binding.actionBar.visibility = View.GONE
        }
        binding.actionBar.setOnClickListener { finish() }
        binding.webBase.setOnLoadingUrlListener {
            var intercept = false
            if (whereFrom?.contains("welfare_center") == true || whereFrom?.contains("third-part") == true) {
                val uri: Uri = Uri.parse(it)
                val scheme = uri.scheme
                when {
                    it.endsWith("close") -> {
                        finish()
                        intercept = true
                    }
                    it.contains("https://m.tb.cn") -> {
                        intercept = loadWeb(it)
                    }
                    it.contains("https://item.m.jd.com") -> {
                        intercept = loadWeb(it)
                    }
                    it.contains("https://tuicashier.youzan.com") -> {
                        intercept = loadWeb(it)
                    }
                    scheme.equals("weixin") -> {
                        if (wxAvailable(this)){
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }else{
                            ToastUtil.showShort("此设备未安装微信")
                        }
                        intercept = true
                    }
                }
            }
            intercept
        }
    }

    fun wxAvailable(context: Context): Boolean {
        return ProcessUtil.isInstalled(this,"com.tencent.mm")
    }

    private fun loadWeb(it: String?): Boolean {
        var inter = false
        if (Build.VERSION.SDK_INT < 26) {
            binding.webBase.loadUrl(it)
            inter = true
        }
        return inter
    }

    override fun onBackPressed() {
        val mWebView = binding.webBase.mWebView
        if (mWebView.canGoBack()) {
            mWebView.goBack()
        } else {
            finish()
        }
    }
}
