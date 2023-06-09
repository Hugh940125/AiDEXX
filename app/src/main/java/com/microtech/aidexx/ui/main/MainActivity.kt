package com.microtech.aidexx.ui.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.common.compliance.EnquireManager
import com.microtech.aidexx.data.AppUpgradeManager
import com.microtech.aidexx.data.EventUnitManager
import com.microtech.aidexx.databinding.ActivityMainBinding
import com.microtech.aidexx.service.MainService
import com.microtech.aidexx.ui.account.AccountViewModel
import com.microtech.aidexx.ui.setting.alert.AlertUtil
import com.microtech.aidexx.ui.upgrade.AppUpdateFragment
import com.microtech.aidexx.utils.*
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.utils.permission.PermissionGroups
import com.microtech.aidexx.utils.permission.PermissionsUtil
import com.microtech.aidexx.widget.dialog.Dialogs
import com.tencent.mars.xlog.Log
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

private const val REQUEST_STORAGE_PERMISSION = 2000
private const val REQUEST_BLUETOOTH_PERMISSION = 2001
private const val REQUEST_ENABLE_LOCATION_SERVICE = 2002
private const val REQUEST_IGNORE_BATTERY_OPTIMIZATIONS = 2003
private const val REQUEST_ENABLE_BLUETOOTH = 2004

class MainActivity : BaseActivity<AccountViewModel, ActivityMainBinding>() {
    var mCurrentOrientation: Int = Configuration.ORIENTATION_PORTRAIT
    private lateinit var mHandler: Handler
    private var checkStep = 0

    companion object {
        const val HISTORY = 0
        const val TRENDS = 1
        const val HOME = 2
        const val BG = 3
        const val EVENT = 4
    }

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
                            EnquireManager.instance()
                                .showEnquireOrNot(
                                    it,
                                    it.getString(R.string.need_bt_permission),
                                    it.getString(R.string.bt_permission_use_for), {
                                        PermissionsUtil.requestPermissions(
                                            it,
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PermissionGroups.Bluetooth
                                            else PermissionGroups.Location
                                        )
                                    }, flag = null
                                )
                        }
                        REQUEST_ENABLE_LOCATION_SERVICE -> {
                            it.enableLocation()
                        }
                        REQUEST_ENABLE_BLUETOOTH -> {
                            activity.enableBluetooth()
                        }
                        REQUEST_IGNORE_BATTERY_OPTIMIZATIONS -> {
                            val powerManager = it.getSystemService(POWER_SERVICE) as PowerManager
                            val hasIgnored =
                                powerManager.isIgnoringBatteryOptimizations(it.packageName)
                            if (!hasIgnored) {
                                Dialogs.showWhether(
                                    it,
                                    content = activity.getString(R.string.content_ignore_battery),
                                    cancel = {
                                    },
                                    confirm = {
                                        ignoreBatteryOptimization(it)
                                    }, key = "battery_optimize"
                                )
                            }
                        }
                    }
                }
            }
        }

        @SuppressLint("BatteryLife")
        fun ignoreBatteryOptimization(activity: Activity) {
            try {
                val intent: Intent
                if (ActivityUtil.isHarmonyOS()) {
                    intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_LAUNCHER)
                    intent.component = ComponentName(
                        "com.android.settings",
                        "com.android.settings.Settings\$HighPowerApplicationsActivity"
                    )
                } else {
                    intent = Intent(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = Uri.parse("package:" + activity.packageName)
                }
                activity.startActivity(intent)
            } catch (e: Exception) {
                LogUtil.eAiDEX("Set ignore battery optimizations failed:${e.printStackTrace()}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        mHandler = MainHandler(this)
        initSDKs()
        fitOrientation()
        initView()
        loadData()
        initEvent()
    }

    private fun initEvent() {
        EventBusManager.onReceive<Int>(EventBusKey.EVENT_JUMP_TO_TAB, this) {
            if (it in 0..4) {
                binding.viewpager.currentItem = it
            }
        }
        EventBusManager.onReceive<Int>(EventBusKey.EVENT_LOGOUT, this) {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            if (!ActivityUtil.isServiceRunning(this, MainService::class.java)) {
                val intent = Intent(this, MainService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
            }
        } catch (e: Exception) {
            LogUtil.eAiDEX("Start service error:${e.message.toString()}")
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            AlertUtil.loadSettingsFromDb()
        }
        EventUnitManager.loadUnit(LanguageUnitManager.getCurrentLanguageCode())
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
        lifecycleScope.launch {
            AppUpgradeManager.fetchVersionInfo()?.let {
                AppUpdateFragment(it).show(supportFragmentManager, AppUpdateFragment.TAG)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacksAndMessages(null)
    }

    private fun checkPermission() {
        if (PermissionsUtil.goSystemSettingShowing) {
            return
        }
        var needBtPermission = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionsUtil.checkPermissions(this, PermissionGroups.Bluetooth) {
                mHandler.removeMessages(REQUEST_BLUETOOTH_PERMISSION)
                mHandler.sendEmptyMessageDelayed(
                    REQUEST_BLUETOOTH_PERMISSION,
                    if (checkStep != 1) 1000 else 30 * 1000
                )
                needBtPermission = true
            }
        } else {
            PermissionsUtil.checkPermissions(this, PermissionGroups.Location) {
                mHandler.removeMessages(REQUEST_BLUETOOTH_PERMISSION)
                mHandler.sendEmptyMessageDelayed(
                    REQUEST_BLUETOOTH_PERMISSION,
                    if (checkStep != 1) 1000 else 30 * 1000
                )
                needBtPermission = true
            }
        }
        if (needBtPermission) {
            checkStep = 1
            return
        }
        if (!LocationUtils.isLocationServiceEnable(this) && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            mHandler.removeMessages(REQUEST_ENABLE_LOCATION_SERVICE)
            mHandler.sendEmptyMessageDelayed(
                REQUEST_ENABLE_LOCATION_SERVICE,
                if (checkStep != 2) 1000 else 30 * 1000
            )
            checkStep = 2
            return
        }
        if (!BleUtil.isBleEnable(this)) {
            mHandler.removeMessages(REQUEST_ENABLE_BLUETOOTH)
            mHandler.sendEmptyMessageDelayed(
                REQUEST_ENABLE_BLUETOOTH,
                if (checkStep != 3) 1000 else 30 * 1000
            )
            checkStep = 3
            return
        }
//        PermissionsUtil.checkPermissions(this, PermissionGroups.Storage) {
//            mHandler.removeMessages(REQUEST_STORAGE_PERMISSION)
//            mHandler.sendEmptyMessageDelayed(
//                REQUEST_STORAGE_PERMISSION,
//                if (checkStep != 4) 5 * 1000 else 30 * 1000
//            )
//            needBtPermission = true
//        }
        if (needBtPermission) {
            checkStep = 4
            return
        }
        mHandler.removeMessages(REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        mHandler.sendEmptyMessageDelayed(
            REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            15L * 1000
        )
    }

    private fun initView() {
        val mainViewPagerAdapter = MainViewPagerAdapter(this)
        binding.viewpager.apply {
            this.offscreenPageLimit = 2
            this.adapter = mainViewPagerAdapter
            this.isUserInputEnabled = false
            this.setCurrentItem(HOME, false)
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
    }

    fun fitOrientation() {
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
        binding.mainTabView.onTabChange = null
    }

    override fun onBackPressed() {
        ActivityUtil.toSystemHome(this)
    }
}