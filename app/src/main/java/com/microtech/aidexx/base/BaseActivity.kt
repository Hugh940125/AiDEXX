package com.microtech.aidexx.base

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import com.microtech.aidexx.utils.ThemeUtil
import com.microtech.aidexx.utils.statusbar.StatusBarHelper
import java.io.Serializable
import java.lang.reflect.ParameterizedType


abstract class BaseActivity<VM : BaseViewModel, VB : ViewBinding> : AppCompatActivity() {
    var loadingMsg: String? = null

    lateinit var viewModel: VM
    lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(ThemeUtil.theme.id)
        binding = getViewBinding()
        initVM()
    }

    abstract fun getViewBinding(): VB

    override fun onResume() {
        super.onResume()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (ThemeUtil.theme.index == 1) {
            StatusBarHelper.setStatusBarLightMode(this)
        } else {
            StatusBarHelper.setStatusBarDarkMode(this)
        }
    }

//    override fun getResources(): Resources {
//        val resources = super.getResources()
//        if (resources != null) {
//            val configuration = Configuration()
//            configuration.setToDefaults()
//            resources.updateConfiguration(configuration, resources.displayMetrics)
//        }
//        return resources
//    }

    override fun onDestroy() {
        super.onDestroy()
    }

    open fun initVM() {
        val clazz =
            (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<VM>
        vm = ViewModelProviders.of(this).get(clazz)

        vm.errData.observe(this, Observer {

            if (it.code == 100001) {
                getString(R.string.failure).toast(this)
            } else {
                it.msg.toast(this)
            }

            if (it.code == 120001 || it.code == 120003 || it.code == 120002) {
                com.microtechmd.cgms.manager.UserManager.instance().onUserExit(this)
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        })
        vm.showLoadData.observe(this, Observer {
            //WaitDialog.show(this, loadingMsg)
        })
        vm.dismissLoadData.observe(this, Observer {
            WaitDialog.dismiss()
        })


        //报警消息弹框
        LiveEventBus
            .get(
                EventKey.MESSAGE_COME, Message::
                class.java
            )
            .observe(this) {
                runOnUiThread {
                    if (ActivityForeground.isForeground(this)) {
                        if (it.content.isNotBlank()) {
                            if (ERROR_BLE == it.content) {
                                it.content = resources.getString(R.string.error_ble)
                                LogUtils.data(it.content)
                                ToastUtil.show(it.content)
                            }
                            MessageDialog.showAlert(
                                this,
                                it.content, null, it.type
                            ) {
                                if (it.type == MESSAGE_TYPE_SENROR_EMBEDDING_SUPER || it.type == MESSAGE_TYPE_SENROR_EMBEDDING || it.type == MESSAGE_TYPE_SENRORERROR) {
                                    CustomerServiceDialog.Setter().create(this@BaseActivity)?.show()
                                }
                            }
                        }
                    }
                }
            }

    }

    protected fun enableBluetooth() {
        if (!BleUtil.isBleEnable(this)) {
            CgmConfirmDialog.showBlueTooth(this) {
                BleUtil.enableBluetooth(this, 1)
            }
        }
    }

    fun enableLocation() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && !LocationUtils.isLocServiceEnable(this)) {
            LogUtils.data("弹出位置信息提示！！！")

            MessageDialog.show(
                this,
                resources.getString(R.string.location_service), null
            ) {
                LocationUtils.openLocation(this)
            }
        }
    }


    fun goActivity(clazz: Class<*>, vararg data: Pair<String, Any?>) {
        val intent = Intent(this, clazz)

        data.forEach {
            when (it.second) {
                is Boolean -> {
                    intent.putExtra(it.first, it.second as Boolean)
                }
                is Byte -> {
                    intent.putExtra(it.first, it.second as Byte)
                }
                is Int -> {
                    intent.putExtra(it.first, it.second as Int)
                }
                is Short -> {
                    intent.putExtra(it.first, it.second as Short)
                }
                is Long -> {
                    intent.putExtra(it.first, it.second as Long)
                }
                is Float -> {
                    intent.putExtra(it.first, it.second as Float)
                }
                is Double -> {
                    intent.putExtra(it.first, it.second as Double)
                }
                is Char -> {
                    intent.putExtra(it.first, it.second as Char)
                }
                is String -> {
                    intent.putExtra(it.first, it.second as String)
                }
                is Serializable -> {
                    intent.putExtra(it.first, it.second as Serializable)
                }
                is Parcelable -> {
                    intent.putExtra(it.first, it.second as Parcelable)
                }
            }
        }
        startActivity(intent)
    }


    /**
     * 跳转界面
     */
    fun goActivity(bundle: Bundle?, cls: Class<*>) {
        val intent = Intent(this, cls)
        val bundle1 = Bundle()
        if (bundle != null) {
            bundle1.putAll(bundle)
        }
        intent.putExtras(bundle1)
        startActivity(intent)
    }

    /**
     * 调用如: checkPermission(Manifest.permission.CALL_PHONE);
     *
     * @param permissions
     */
    fun checkMyPermission(permissions: PermissionListener) {
        this.checkMyPermission(permissions, false)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(resources.configuration)
        MultiLanguage.setApplicationLanguage(this)
    }

    /**
     * 调用如: checkPermission(Manifest.permission.CALL_PHONE);
     *
     * @param permissions
     * @param isOnce      是否一次,只能设置一个权限
     */
    fun checkMyPermission(permissions: PermissionListener?, isOnce: Boolean) {

        if (permissions == null) {
            return
        }

        if (isOnce) {
            //只提醒一次
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    permissions.permissions[0]
                )
            ) {
                return
            }
        }

        this.mPermissionListener = permissions

        val arr = mPermissionListener?.permissions?.let { checkSelfPermission(this, it) }

        if (arr != null) {
            ActivityCompat.requestPermissions(
                this,
                arr,
                1001
            )
        } else {
            this.mPermissionListener?.onClick(null)
        }
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

    /**
     * 权限回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (mPermissionListener == null) {
            return
        }
        when (requestCode) {
            1001 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()) {
                    var ishas = true
                    for (grantResult in grantResults) {
                        if (grantResult == PackageManager.PERMISSION_DENIED) {
                            //其中一个未授权，立即返回false授权失败
                            ishas = false
                            break
                        }
                    }

                    if (ishas) {
                        if (this.mPermissionListener != null) {
                            this.mPermissionListener?.onClick(null)
                            // this.mPermissionLinstener=null;
                        }
                    } else {
                        if (this.mPermissionListener?.isNeedHas() == true) {
                            this.mPermissionListener?.getPerName(this)?.let { onOpenPermission(it) }
                        }
                    }
                } else {
                }
            }
        }
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