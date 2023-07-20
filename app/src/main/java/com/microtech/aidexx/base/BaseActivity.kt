package com.microtech.aidexx.base

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.microtech.aidexx.R
import com.microtech.aidexx.ui.account.LoginActivity
import com.microtech.aidexx.ui.setting.SettingsManager
import com.microtech.aidexx.ui.setting.alert.*
import com.microtech.aidexx.utils.*
import com.microtech.aidexx.utils.eventbus.AlertInfo
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.utils.permission.PermissionGroups
import com.microtech.aidexx.utils.permission.PermissionsUtil
import com.microtech.aidexx.utils.statusbar.StatusBarHelper
import com.microtech.aidexx.views.dialog.Dialogs
import com.microtech.aidexx.views.dialog.customerservice.CustomerServiceDialog
import kotlinx.coroutines.*
import java.lang.reflect.ParameterizedType

abstract class BaseActivity<VM : BaseViewModel, VB : ViewBinding> : AppCompatActivity() {

    lateinit var viewModel: VM
    lateinit var binding: VB
    lateinit var mainScope: CoroutineScope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(ThemeManager.theme.id)
        binding = getViewBinding()
        initWindow()
        mainScope = MainScope()
        initViewModel()
        observe()
    }

    private fun initWindow() {
        val window = this.window
        val decorView = window.decorView
        binding.root.setOnApplyWindowInsetsListener { _, insets ->
            val navigationBarHeight =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    insets.getInsets(WindowInsets.Type.navigationBars()).bottom
                } else {
                    insets.systemWindowInsetBottom
                }
            binding.root.setPadding(0, 0, 0, navigationBarHeight)
            insets
        }
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

    private fun dialogAlert(content: String, showCustomerService: Boolean = false) {
        Dialogs.showAlert(this@BaseActivity, null, content) {
            AlertUtil.stop()
            if (showCustomerService) {
                CustomerServiceDialog.Setter().create(this@BaseActivity)?.show()
            }
        }
    }

    private fun observe() {
        EventBusManager.onReceive<AlertInfo>(EventBusKey.EVENT_SHOW_ALERT, this) {
            dialogAlert(it.content, it.showCustomerService)
        }
        EventBusManager.onReceive<Nothing>(EventBusKey.EVENT_RESTART_BLUETOOTH, this)
        {

        }
        EventBusManager.onReceive<Boolean>(EventBusKey.TOKEN_EXPIRED, this)
        {
            lifecycleScope.launch {
                Dialogs.showMessage(this@BaseActivity, content = getString(R.string.token_expired), callBack = {
                    val intent = Intent(this@BaseActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                })
            }
        }
    }

    private fun process(type: Int, isUrgent: Boolean) {
        if (type == MESSAGE_TYPE_SIGNAL_LOST) {
            val signalLossAlertMethod = SettingsManager.settingEntity!!.signalMissingAlertType - 1
            AlertUtil.alert(this, signalLossAlertMethod, isUrgent)
            return
        }
        if (type != MESSAGE_TYPE_REPLACE_SENSOR && type != MESSAGE_TYPE_NEW_SENSOR) {
            if (isUrgent) {
                val urgentAlertMethod = SettingsManager.settingEntity!!.urgentAlertType - 1
                AlertUtil.alert(this, urgentAlertMethod, true)
            } else {
                val alertMethod = SettingsManager.settingEntity!!.alertType - 1
                AlertUtil.alert(this, alertMethod, false)
            }
        }
    }

    abstract fun getViewBinding(): VB

    override fun onResume() {
        super.onResume()
        LogUtil.eAiDEX("onResume ----> ${this::class.java.name}")
    }

    override fun onPause() {
        super.onPause()
        LogUtil.eAiDEX("onPause ----> ${this::class.java.name}")
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (ThemeManager.isLight()) {
            StatusBarHelper.setStatusBarLightMode(this)
        } else {
            StatusBarHelper.setStatusBarDarkMode(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionsUtil.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }

    override fun getResources(): Resources {
        val resources = super.getResources()
        if (resources != null) {
            val configuration = Configuration()
            configuration.setToDefaults()
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
        return resources
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.eAiDEX("onDestroy ----> ${this::class.java.name}")
        mainScope.cancel()
    }

    open fun initViewModel() {
        @Suppress("UNCHECKED_CAST")
        val clazz =
            (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<VM>
        viewModel = ViewModelProvider(this)[clazz]
    }

    protected fun checkEnvironment(onSuccess: (() -> Unit)) {
        if (!BleUtil.isBleEnable(this)) {
            enableBluetooth()
            return
        }
        var needBtPermission = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionsUtil.checkPermissions(this, PermissionGroups.Bluetooth) {
                PermissionsUtil.requestPermissions(this, PermissionGroups.Bluetooth)
                needBtPermission = true
            }
        } else {
            PermissionsUtil.checkPermissions(this, PermissionGroups.Location) {
                PermissionsUtil.requestPermissions(this, PermissionGroups.Location)
                needBtPermission = true
            }
        }
        if (needBtPermission) {
            return
        }
        if (!LocationUtils.isLocationServiceEnable(this) && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            enableLocation()
            return
        }
        if (!NetUtil.isNetAvailable(this)) {
            Dialogs.showError(getString(R.string.net_error))
            return
        }
        onSuccess.invoke()
    }

    protected fun enableBluetooth() {
        Dialogs.showWhether(
            this,
            resources.getString(R.string.Bluetooth),
            resources.getString(R.string.guide_ble),
            {
                BleUtil.enableBluetooth(this, 1)
            }
        )
    }

    protected fun enableLocation() {
        Dialogs.showWhether(
            this,
            content = resources.getString(R.string.location_service),
            confirm = {
                LocationUtils.enableLocationService(this)
            }, key = "location_service"
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(resources.configuration)
    }

    /**
     * 检查是否被容许
     *
     * @param context
     * @param permissions
     * @return
     */
    fun checkSelfPermission(context: Context, permissions: Array<String>): Array<String>? {
        val list = ArrayList<String>()
        for (permission in permissions) {

            val rl = ContextCompat.checkSelfPermission(context, permission)

            if (rl == PackageManager.PERMISSION_DENIED) {
                list.add(permission)
            }
        }
        return if (list.size > 0) {
            list.toTypedArray()
        } else null
    }

    //打开权限
    private fun onOpenPermission(permissionName: String) {
        val builder = AlertDialog.Builder(this)
        val dialog = builder.setMessage(getString(R.string.permission_setting_tips, permissionName))
            .setNegativeButton(
                getString(R.string.btn_cancel)
            ) { dialog, _ -> dialog.dismiss() }.setPositiveButton(
                getString(R.string.go_setting)
            ) { _, _ ->
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.data = Uri.parse("package:$packageName")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                startActivity(intent)
            }.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
    }

    fun startActivity(bundle: Bundle?, cls: Class<*>) {
        val intent = Intent(this, cls)
        val bundle1 = Bundle()
        if (bundle != null) {
            bundle1.putAll(bundle)
        }
        intent.putExtras(bundle1)
        startActivity(intent)
    }
}