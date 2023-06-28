package com.microtech.aidexx.ui.web

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.core.view.isVisible
import com.microtech.aidexx.IntentKey
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.databinding.ActivityWebBinding
import com.microtech.aidexx.utils.DensityUtils
import com.microtech.aidexx.utils.ProcessUtil
import com.microtech.aidexx.utils.ToastUtil

class WebActivity : BaseActivity<BaseViewModel, ActivityWebBinding>() {

    override fun getViewBinding(): ActivityWebBinding {
        return ActivityWebBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.actionBar.getLeftIcon().setOnClickListener { finish() }
        binding.actionBar.setTitle(intent.getStringExtra(IntentKey.WEB_TITLE))
        binding.webBase.setFullscreenContainer(binding.fullscreenContainer)
        binding.webBase.loadUrl((intent.getStringExtra(IntentKey.WEB_URL)))
        val whereFrom = intent.getStringExtra(IntentKey.WHERE_FROM)
        if (intent.getBooleanExtra(IntentKey.IS_FULL_SCREEN, false)) {
            binding.actionBar.visibility = View.GONE
        }
        if (!binding.actionBar.isVisible) {
            binding.webBase.setPadding(0, DensityUtils.dp2px(40f), 0, 0)
        }
        binding.webBase.setOnLoadingUrlListener {
            var intercept = false
            if (whereFrom?.contains("welfare_center") == true
                || whereFrom?.contains("third-part") == true
                || whereFrom?.contains("help_center") == true
            ) {
                val uri: Uri = Uri.parse(it)
                val scheme = uri.scheme
                when {
                    it.endsWith("close") -> {
                        finish()
                        intercept = true
                    }

                    it.contains("https://m.tb.cn") -> {
                        intercept = load(it)
                    }

                    it.contains("https://item.m.jd.com") -> {
                        intercept = load(it)
                    }

                    it.contains("https://tuicashier.youzan.com") -> {
                        intercept = load(it)
                    }

                    scheme.equals("weixin") -> {
                        if (wxAvailable()) {
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        } else {
                            ToastUtil.showShort("此设备未安装微信")
                        }
                        intercept = true
                    }
                }
            }
            intercept
        }
    }

    private fun wxAvailable(): Boolean {
        return ProcessUtil.isInstalled(this, "com.tencent.mm")
    }

    private fun load(it: String?): Boolean {
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

    fun fitWebOrientation(orientation: Int) {
        val window = this.window
        val decorView = window.decorView
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.also { controller ->
                    controller.hide(WindowInsets.Type.statusBars())
                    controller.hide(WindowInsets.Type.navigationBars())
                    controller.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false)
                window.statusBarColor = Color.TRANSPARENT
                window.insetsController?.also { controller ->
                    controller.show(WindowInsets.Type.statusBars())
                    controller.show(WindowInsets.Type.navigationBars())
                }
            } else {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                window.statusBarColor = Color.TRANSPARENT
                window.decorView.apply {
                    // 设置状态栏系统栏覆盖在应用内容上
                    systemUiVisibility =
                        systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                }
            }
        }
    }

    companion object {
        fun loadWeb(
            context: Context,
            title: String = "",
            url: String,
            fullScreen: Boolean = false,
            from: String? = null
        ) {
            val intent = Intent(context, WebActivity::class.java)
            intent.putExtra(IntentKey.WEB_TITLE, title)
            intent.putExtra(IntentKey.WEB_URL, url)
            intent.putExtra(IntentKey.IS_FULL_SCREEN, fullScreen)
            intent.putExtra(IntentKey.WHERE_FROM, from)
            context.startActivity(intent)
        }
    }
}
