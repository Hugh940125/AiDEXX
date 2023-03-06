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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.microtech.aidexx.R
import com.microtech.aidexx.constant.MESSAGE_TYPE_SENRORERROR
import com.microtech.aidexx.constant.MESSAGE_TYPE_SENROR_EMBEDDING
import com.microtech.aidexx.constant.MESSAGE_TYPE_SENROR_EMBEDDING_SUPER
import com.microtech.aidexx.utils.*
import com.microtech.aidexx.utils.eventbus.AlertInfo
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.utils.permission.PermissionsUtil
import com.microtech.aidexx.utils.statusbar.StatusBarHelper
import com.microtech.aidexx.widget.dialog.Dialogs
import com.microtech.aidexx.widget.dialog.customerservice.CustomerServiceDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.lang.reflect.ParameterizedType


abstract class BaseActivity<VM : BaseViewModel, VB : ViewBinding> : AppCompatActivity() {

    lateinit var viewModel: VM
    lateinit var binding: VB
    lateinit var mainScope: CoroutineScope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogUtil.eAiDEX("onCreate ----> ${this::class.java.name}")
        setTheme(ThemeManager.theme.id)
        binding = getViewBinding()
        mainScope = MainScope()
        initViewModel()
        initEvent()
    }

    private fun initEvent() {
        EventBusManager.onReceive<AlertInfo>(EventBusKey.EVENT_SHOW_ALERT, this) {
            if (ActivityUtil.isForeground(this) && it.content.isNotBlank()) {
                mainScope.launch {
                    Dialogs.showAlert(this@BaseActivity, null, it.content) {
                        if (it.type == MESSAGE_TYPE_SENROR_EMBEDDING_SUPER || it.type == MESSAGE_TYPE_SENROR_EMBEDDING || it.type == MESSAGE_TYPE_SENRORERROR) {
                            CustomerServiceDialog.Setter().create(this@BaseActivity)?.show()
                        }
                    }
                }
            }
        }
        EventBusManager.onReceive<Nothing>(EventBusKey.EVENT_RESTART_BLUETOOTH, this) {

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
        if (ThemeManager.theme.index == 1) {
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

    protected fun enableBluetooth() {
        if (!BleUtil.isBleEnable(this)) {
            Dialogs.showWhether(
                this,
                resources.getString(R.string.Bluetooth),
                resources.getString(R.string.guide_ble),
                {
                    BleUtil.enableBluetooth(this, 1)
                }
            )
        }
    }

    protected fun enableLocation() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && !LocationUtils.isLocationServiceEnable(
                this
            )
        ) {
            Dialogs.showWhether(
                this,
                content = resources.getString(R.string.location_service),
                confirm = {
                    LocationUtils.enableLocationService(this)
                }
            )
        }
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
}