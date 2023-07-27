package com.microtech.aidexx.ui.main

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.PowerManager
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.igexin.sdk.PushManager
import com.microtech.aidexx.BuildConfig
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseWelcomeActivity
import com.microtech.aidexx.common.compliance.EnquireManager
import com.microtech.aidexx.common.toast
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.data.resource.AppUpgradeManager
import com.microtech.aidexx.data.resource.EventUnitManager
import com.microtech.aidexx.data.resource.LanguageResourceManager
import com.microtech.aidexx.databinding.ActivityMainBinding
import com.microtech.aidexx.db.entity.BaseEventEntity
import com.microtech.aidexx.service.MainService
import com.microtech.aidexx.ui.account.AccountViewModel
import com.microtech.aidexx.ui.main.event.EventFragment
import com.microtech.aidexx.ui.main.home.HomeViewModel
import com.microtech.aidexx.ui.upgrade.AppUpdateFragment
import com.microtech.aidexx.ui.welcome.WelcomeActivity
import com.microtech.aidexx.utils.ActivityUtil
import com.microtech.aidexx.utils.BleUtil
import com.microtech.aidexx.utils.LocationUtils
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.ThemeManager.themeConfig
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.utils.mmkv.MmkvManager
import com.microtech.aidexx.utils.permission.PermissionGroups
import com.microtech.aidexx.utils.permission.PermissionsUtil
import com.microtech.aidexx.views.dialog.Dialogs
import com.microtech.aidexx.views.dialog.standard.StandardDialog
import com.tencent.mars.xlog.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.TimeZone

private const val REQUEST_STORAGE_PERMISSION = 2000
private const val REQUEST_BLUETOOTH_PERMISSION = 2001
private const val REQUEST_ENABLE_LOCATION_SERVICE = 2002
private const val REQUEST_IGNORE_BATTERY_OPTIMIZATIONS = 2003
private const val REQUEST_ENABLE_BLUETOOTH = 2004

class MainActivity : BaseActivity<AccountViewModel, ActivityMainBinding>() {
    var mCurrentOrientation: Int = Configuration.ORIENTATION_PORTRAIT
    private lateinit var mHandler: Handler
    private var checkStep = 0
    private var curTimeZoneId: Int? = null
    private val homeViewModel: HomeViewModel by viewModels()

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        const val HISTORY = 0
        const val TRENDS = 1
        const val HOME = 2
        const val BG = 3
        const val EVENT = 4
    }

    class MainHandler(activity: MainActivity) : Handler(Looper.getMainLooper()) {
        private val reference = WeakReference(activity)
        private var dialogKey: String = "battery_optimize-${System.currentTimeMillis()}"

        private var storagePermissionDialog: StandardDialog? = null
        private var bluetoothPermissionDialog: StandardDialog? = null
        private var ignoreBatteryDialog: StandardDialog? = null

        override fun handleMessage(msg: Message) {
            reference.get()?.let {
                if (!it.isFinishing) {
                    when (msg.what) {
                        REQUEST_STORAGE_PERMISSION -> {
                            storagePermissionDialog?.dismiss()
                            storagePermissionDialog = EnquireManager.instance()
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
                            bluetoothPermissionDialog?.dismiss()
                            bluetoothPermissionDialog = EnquireManager.instance()
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
                            it.enableBluetooth()
                        }

                        REQUEST_IGNORE_BATTERY_OPTIMIZATIONS -> {
                            val powerManager = it.getSystemService(POWER_SERVICE) as PowerManager
                            val hasIgnored =
                                powerManager.isIgnoringBatteryOptimizations(it.packageName)
                            if (!hasIgnored) {
                                ignoreBatteryDialog?.dismiss()
                                ignoreBatteryDialog = Dialogs.showWhether(
                                    it,
                                    content = it.getString(R.string.content_ignore_battery),
                                    cancel = {
                                    },
                                    confirm = {
                                        it.ignoreBatteryOptimization(it)
                                    }, key = dialogKey
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        processIntent(intent, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        themeConfig()
        super.onCreate(savedInstanceState)
        UserInfoManager.shareUserInfo = null
        setContentView(binding.root)
        mHandler = MainHandler(this)
        initSDKs()
        fitHomeOrientation()
        initView()
        loadData()
        initEvent()
        processIntent(intent)
    }

    private fun initEvent() {
        EventBusManager.onReceive<Int>(EventBusKey.EVENT_JUMP_TO_TAB, this) {
            if (it in 0..4) {
                binding.viewpager.currentItem = it
            }
        }
        EventBusManager.onReceive<BaseEventEntity>(EventBusKey.EVENT_GO_TO_HISTORY, this) {
            binding.viewpager.currentItem = HISTORY
        }
        EventBusManager.onReceive<Int>(EventBusKey.EVENT_LOGOUT, this) {
            finish()
        }

        /** 图表异常 */
        EventBusManager.onReceive<Int?>(EventBusKey.EVENT_RELOAD_CHART, this) {
            lifecycleScope.launch {
                delay(500)
                LogUtil.xLogE("EVENT_RELOAD_CHART - recreate", TAG)
                recreate()
            }
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
                LogUtil.eAiDEX("start main service")
            } else {
                LogUtil.eAiDEX("main service is running,need not start")
            }
        } catch (e: Exception) {
            LogUtil.eAiDEX("start main service error:${e.message.toString()}")
        }
    }

    private fun loadData() {
        EventUnitManager.loadUnit(LanguageResourceManager.getCurLanguageTag())
    }

    override fun onResume() {
        super.onResume()
        if (checkAndUpdateResourceIfNeeded()) {
            return
        }
        checkTimeZoneChange()
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

    private fun checkTimeZoneChange() {
        curTimeZoneId?.let {
            if (it != TimeZone.getDefault().rawOffset) {
                lifecycleScope.launch {
                    delay(500)
                    recreate()
                }
            }
        }
        curTimeZoneId = TimeZone.getDefault().rawOffset
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
        PermissionsUtil.checkPermissions(this, PermissionGroups.Storage) {
            mHandler.removeMessages(REQUEST_STORAGE_PERMISSION)
            mHandler.sendEmptyMessageDelayed(
                REQUEST_STORAGE_PERMISSION,
                if (checkStep != 4) 5 * 1000 else 30 * 1000
            )
            needBtPermission = true
        }
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
        binding.apply {
            viewpager.apply {
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
            binding.viewpager.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.mainTabView.check(binding.viewpager.currentItem)
                    binding.viewpager.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
            mainTabView.onTabChange = {
                if ((it == BG || it == EVENT) && UserInfoManager.shareUserInfo != null) {
                    resources.getString(R.string.denied).toast()
                    false
                } else if (viewpager.currentItem == EVENT) {
                    val hasConfirm =
                        ((viewpager.adapter as MainViewPagerAdapter).getItem(EVENT) as EventFragment?)?.needConfirmLeave {
                            viewpager.setCurrentItem(it, false)
                        } ?: true
                    if (!hasConfirm) {
                        viewpager.setCurrentItem(it, false)
                        true
                    } else {
                        false
                    }
                } else {
                    viewpager.setCurrentItem(it, false)
                    true
                }
            }
        }
    }

    private fun initSDKs() {
        //Bugly初始化
//        CrashReport.initCrashReport(applicationContext, "b2c5f05676", BuildConfig.DEBUG)
        PushManager.getInstance().initialize(this)
        if (BuildConfig.DEBUG) {
            PushManager.getInstance().setDebugLogger(this) { s ->
                Log.i("PUSH_LOG", s)
            }
        }
    }

    fun fitHomeOrientation() {
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
            } else {
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
        UserInfoManager.shareUserInfo = null
    }

    override fun onBackPressed() {
        ActivityUtil.toSystemHome(this)
    }


    private fun checkAndUpdateResourceIfNeeded(): Boolean {
        return MmkvManager.getUpgradeResourceZipFileInfo().ifEmpty { null }?.let {
            ActivityUtil.toActivity(this, Bundle().also {
                it.putBoolean(BaseWelcomeActivity.EXT_UPDATE_RESOURCE, true)
            }, WelcomeActivity::class.java)
            finish()
            true
        } ?: false
    }

    private fun processIntent(intent: Intent?, fromOnCreate: Boolean = true) {
        LogUtil.d("processIntent intent=$intent fromOnCreate=$fromOnCreate", TAG)
        intent?.let {
            val payload = it.getStringExtra("payload")
            payload?.ifEmpty { null }?.let {
                intent.removeExtra("payload")
                kotlin.runCatching {
                    val pObj = JSONObject(payload)
                    // { “userId”：“userAuthorizationId” }
                    if (pObj.has(UserInfoManager.instance().userId())) {
                        pObj.optString(UserInfoManager.instance().userId())
                            .ifEmpty { null }?.let { userAuthorizationId ->
                                homeViewModel.switchUser(userAuthorizationId)
                            }
                    } else {
                        LogUtil.xLogE("intent数据未处理", TAG)
                    }
                }.exceptionOrNull()?.let { err ->
                    LogUtil.xLogE("通知数据解析异常-$err", TAG)
                }
            }
        }
    }

}