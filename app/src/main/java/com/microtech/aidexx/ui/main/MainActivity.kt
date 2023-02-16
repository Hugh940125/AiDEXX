package com.microtech.aidexx.ui.main

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.microtech.aidexx.BuildConfig
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.databinding.ActivityMainBinding
import com.microtech.aidexx.ui.main.bg.BgFragment
import com.microtech.aidexx.ui.main.event.EventFragment
import com.microtech.aidexx.ui.main.history.HistoryFragment
import com.microtech.aidexx.ui.main.home.HomeFragment
import com.microtech.aidexx.ui.main.trend.TrendFragment
import com.microtech.aidexx.utils.ProcessUtil
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.mars.xlog.Log
import com.tencent.mars.xlog.Xlog

class MainActivity : BaseActivity<BaseViewModel, ActivityMainBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initSDKs()
        fitWindow()
        initView()
    }

    private fun initView() {
        val mainViewPagerAdapter = MainViewPagerAdapter(this)
        binding.viewpager.apply {
            this.offscreenPageLimit = 2
            this.adapter = mainViewPagerAdapter
            this.isUserInputEnabled = false
            this.setCurrentItem(2, false)
            this.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    binding.mainTabView.check(position)
                }
            })
        }
        binding.mainTabView.onTabChange = {
            binding.viewpager.setCurrentItem(it, false)
            true
        }
    }

    private fun initSDKs() {
        //Bugly初始化
//        CrashReport.initCrashReport(applicationContext, "b2c5f05676", BuildConfig.DEBUG)
        //Xlog初始化
        initXlog()
    }

    private fun fitWindow() {
        binding.container.setOnApplyWindowInsetsListener { _, insets ->
            val navigationBarHeight =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    insets.getInsets(WindowInsets.Type.navigationBars()).bottom
                } else {
                    insets.systemWindowInsetBottom
                }
            binding.container.setPadding(0, 0, 0, navigationBarHeight)
            insets
        }
        EventBusManager.onReceive(EventBusKey.HOME_ORIENTATION, Int::class.java, this) {
            val window = this.window
            val decorView = window.decorView
            if (it == Configuration.ORIENTATION_LANDSCAPE) {
                binding.mainTabView.visibility = View.GONE
                binding.bottomSpace.visibility = View.GONE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.insetsController?.also { controller ->
                        controller.hide(WindowInsets.Type.statusBars())
                        controller.hide(WindowInsets.Type.navigationBars())
                        controller.systemBarsBehavior =
                            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                } else if (it == Configuration.ORIENTATION_PORTRAIT) {
                    decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                }
            } else {
                binding.mainTabView.visibility = View.VISIBLE
                binding.bottomSpace.visibility = View.VISIBLE
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
    }

    override fun getViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.appenderClose()
    }

    private fun initXlog() {
        val cacheDays = 15
        val namePrefix = "AiDEX"
        System.loadLibrary("c++_shared")
        System.loadLibrary("marsxlog")
        val sdCard = Environment.getExternalStorageDirectory().getAbsolutePath()
        val logPath = "$sdCard/aidex/log"
        val cachePath = "${this.filesDir}/xlog"
        val logConfig = Xlog.XLogConfig()
        logConfig.mode = Xlog.AppednerModeAsync
        logConfig.logdir = logPath
        logConfig.nameprefix = namePrefix
        logConfig.pubkey = ""
        logConfig.compressmode = Xlog.ZLIB_MODE
        logConfig.compresslevel = 0
        logConfig.cachedir = cachePath
        logConfig.cachedays = cacheDays
        val xlog = Xlog()
        Log.setLogImp(xlog)
        if (ProcessUtil.isMainProcess(this)) {
            if (BuildConfig.DEBUG) {
                Log.setConsoleLogOpen(true)
                Log.appenderOpen(
                    Xlog.LEVEL_DEBUG,
                    Xlog.AppednerModeAsync,
                    "",
                    logPath,
                    namePrefix,
                    0
                )
            } else {
                Log.setConsoleLogOpen(false)
                Log.appenderOpen(
                    Xlog.LEVEL_DEBUG,
                    Xlog.AppednerModeAsync,
                    "",
                    logPath,
                    namePrefix,
                    cacheDays
                )
            }
        }
    }
}