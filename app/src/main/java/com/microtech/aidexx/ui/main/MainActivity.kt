package com.microtech.aidexx.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.microtech.aidexx.BuildConfig
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.compliance.EnquireManager
import com.microtech.aidexx.databinding.ActivityMainBinding
import com.microtech.aidexx.service.MainService
import com.microtech.aidexx.utils.LocationUtils
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.ProcessUtil
import com.microtech.aidexx.utils.permission.PermissionGroups
import com.microtech.aidexx.utils.permission.PermissionsUtil
import com.microtech.aidexx.widget.dialog.Dialogs
import com.tencent.mars.xlog.Log
import com.tencent.mars.xlog.Xlog
import java.lang.ref.WeakReference

private const val REQUEST_STORAGE_PERMISSION = 2000
private const val REQUEST_BLUETOOTH_PERMISSION = 2001
private const val REQUEST_ENABLE_LOCATION_SERVICE = 2002
private const val REQUEST_IGNORE_BATTERY_OPTIMIZATIONS = 2003

class MainActivity : BaseActivity<BaseViewModel, ActivityMainBinding>() {
    var mCurrentOrientation: Int = Configuration.ORIENTATION_PORTRAIT
    private lateinit var mHandler: Handler

    class MainHandler(val activity: MainActivity) : Handler(Looper.getMainLooper()) {
        private val reference = WeakReference(activity)
        override fun handleMessage(msg: Message) {
            reference.get()?.let {
                if (!it.isFinishing) {
                    when (msg.what) {
                        REQUEST_STORAGE_PERMISSION -> {
                            EnquireManager.instance()
                                .showEnquireOrNot(
                                    it,
                                    it.getString(R.string.need_storage),
                                    it.getString(R.string.storage_use_for), {
                                        PermissionsUtil.requestPermissions(
                                            it,
                                            PermissionGroups.Storage
                                        )
                                    }, flag = null
                                )
                        }
                        REQUEST_BLUETOOTH_PERMISSION -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                PermissionsUtil.requestPermissions(it, PermissionGroups.Bluetooth)
                            } else {
                                PermissionsUtil.requestPermissions(it, PermissionGroups.Location)
                            }
                        }
                        REQUEST_ENABLE_LOCATION_SERVICE -> {
                            activity.enableLocation()
                        }
//                        REQUEST_IGNORE_BATTERY_OPTIMIZATIONS -> {
//                            val powerManager = activity.getSystemService(POWER_SERVICE) as PowerManager
//                            val hasIgnored = powerManager.isIgnoringBatteryOptimizations(activity.packageName)
//                            if (!hasIgnored) {
//                                Dialogs.showWhether(
//                                    activity,
//                                    content = activity.getString(R.string.content_ignore_battery),
//                                    confirm = {
//                                        activity.ignoreBatteryOptimization()
//                                    })
//                            }
//                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        startService(Intent(this, MainService::class.java))
        mHandler = MainHandler(this)
        initSDKs()
        fitOrientation()
        initView()
    }

    override fun onResume() {
        super.onResume()
        requestPermission()
    }

    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacksAndMessages(null)
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionsUtil.checkPermissions(this, PermissionGroups.Bluetooth) {
                mHandler.removeMessages(REQUEST_BLUETOOTH_PERMISSION)
                mHandler.sendEmptyMessageDelayed(REQUEST_BLUETOOTH_PERMISSION, 2 * 1000)
                return@checkPermissions
            }
        } else {
            PermissionsUtil.checkPermissions(this, PermissionGroups.Location) {
                mHandler.removeMessages(REQUEST_BLUETOOTH_PERMISSION)
                mHandler.sendEmptyMessageDelayed(REQUEST_BLUETOOTH_PERMISSION, 2 * 1000)
                return@checkPermissions
            }
        }
        if (!LocationUtils.isLocationServiceEnable(this) && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            mHandler.removeMessages(REQUEST_ENABLE_LOCATION_SERVICE)
            mHandler.sendEmptyMessageDelayed(REQUEST_ENABLE_LOCATION_SERVICE, 2 * 1000)
            return
        }
        PermissionsUtil.checkPermissions(this, PermissionGroups.Storage) {
            mHandler.removeMessages(REQUEST_STORAGE_PERMISSION)
            mHandler.sendEmptyMessageDelayed(REQUEST_STORAGE_PERMISSION, 5 * 1000)
            return@checkPermissions
        }
        mHandler.removeMessages(REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        mHandler.sendEmptyMessageDelayed(REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, 15 * 1000)
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

    fun fitOrientation() {
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
        val window = this.window
        val decorView = window.decorView
        if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.mainTabView.visibility = View.GONE
            binding.bottomSpace.visibility = View.GONE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.also { controller ->
                    controller.hide(WindowInsets.Type.statusBars())
                    controller.hide(WindowInsets.Type.navigationBars())
                    controller.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
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
        val root = externalCacheDir?.absolutePath
        val logPath = "$root/aidex/log"
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

    @SuppressLint("BatteryLife")
    fun ignoreBatteryOptimization() {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:" + this.packageName)
            startActivity(intent)
        } catch (e: Exception) {
            LogUtil.eAiDEX("Set ignore battery optimizations failed:${e.printStackTrace()}")
        }
    }
}