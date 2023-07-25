package com.microtech.aidexx.ui.setting

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.compliance.EnquireManager
import com.microtech.aidexx.common.setDebounceClickListener
import com.microtech.aidexx.databinding.ActivityPermissionCheckBinding
import com.microtech.aidexx.utils.BleUtil
import com.microtech.aidexx.utils.LocationUtils
import com.microtech.aidexx.utils.permission.PermissionGroups
import com.microtech.aidexx.utils.permission.PermissionsUtil
import com.microtech.aidexx.views.SettingItemWidget
import com.microtech.aidexx.views.dialog.Dialogs


class PermissionCheckActivity : BaseActivity<BaseViewModel, ActivityPermissionCheckBinding>() {


    override fun getViewBinding(): ActivityPermissionCheckBinding {
        return ActivityPermissionCheckBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.run {
            actionBar.getLeftIcon().setOnClickListener {
                finish()
            }

            checkOnce.setDebounceClickListener {
                checkAllPermission()
            }

        }
    }

    override fun onResume() {
        super.onResume()
        checkAllPermission()
    }

    private fun checkAllPermission() {
        binding.itemLl.removeAllViews()
        // 蓝牙
        checkBluetooth()
        checkNotification()
        checkBattery()
        checkStorage()
        checkCamera()

    }

    private fun addItemToView(item: SettingItemWidget) {
        binding.itemLl.addView(item)
    }

    //region 蓝牙
    private fun checkLocation(
        checkItemView: SettingItemWidget? = null
    ): Pair<Boolean, SettingItemWidget?> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            var hasGranted = 0 // 1-没有定位权限 2-没有打开定位服务
            PermissionsUtil.checkPermissions(this, PermissionGroups.Location) {
                hasGranted = 1
            }
            if (hasGranted != 0) {
                if (!LocationUtils.isLocationServiceEnable(this)) {
                    hasGranted = 2
                }
            }

            var value = ""
            var rightIcon = R.drawable.ic_access
            if (hasGranted != 0) {
                rightIcon = R.drawable.ic_warnning
                value = getString(R.string.manual_setting)
            }

            val onClick = if (hasGranted != 0) {
                object: ((SettingItemWidget)->Unit) {
                    override fun invoke(p1: SettingItemWidget) {
                        if (hasGranted == 1) {
                            EnquireManager.instance()
                                .showEnquireOrNot(
                                    this@PermissionCheckActivity,
                                    getString(R.string.need_bt_permission),
                                    getString(R.string.bt_permission_use_for), {
                                        PermissionsUtil.requestPermissions(
                                            this@PermissionCheckActivity,
                                            PermissionGroups.Location
                                        )
                                    }, flag = null
                                )
                        } else if (hasGranted == 2) {
                            enableLocation()
                        }
                    }
                }
            } else null

            val itemView = checkItemView?.let {
                updateCheckItem(it, value, rightIcon, onClick)
                it
            } ?:let {
                val item = genCheckItem(
                    getString(R.string.open_location),
                    value, rightIcon, false, onClick
                )
                addItemToView(item)
                item
            }
            return (hasGranted == 0) to itemView
        }
        return true to null
    }

    private fun checkBluetooth(checkItemView: SettingItemWidget? = null) {
        var hasGranted = 0 // 1-没定位权限 2-没有蓝牙权限 3-没有打开蓝牙服务

        if (checkLocation().first) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PermissionsUtil.checkPermissions(this, PermissionGroups.Bluetooth) {
                    hasGranted = 2
                }
            }
            if (hasGranted == 0) {
                if (!BleUtil.isBleEnable(this)) {
                    hasGranted = 3
                }
            }
        } else {
            hasGranted = 1
        }

        val onClick = if (hasGranted == 0) null else {
            object: ((SettingItemWidget)->Unit) {
                override fun invoke(siw: SettingItemWidget) {
                    if (hasGranted == 1) {
                        // 提示打开定位权限
                    } else if (hasGranted == 2){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            EnquireManager.instance()
                                .showEnquireOrNot(
                                    this@PermissionCheckActivity,
                                    getString(R.string.need_bt_permission),
                                    getString(R.string.bt_permission_use_for), {
                                        PermissionsUtil.requestPermissions(
                                            this@PermissionCheckActivity,
                                            PermissionGroups.Bluetooth
                                        )
                                    }, flag = null
                                )
                        }
                    } else if (hasGranted == 3) {
                        enableBluetooth()
                    }
                }
            }
        }

        var value = ""
        var rightIcon = R.drawable.ic_access
        if (hasGranted != 0) {
            value = getString(
                if (hasGranted == 1) R.string.no_open_location else R.string.manual_setting
            )
            rightIcon = R.drawable.ic_warnning
        }

        checkItemView?.let {
            updateCheckItem(it, value, rightIcon,onClick)
        } ?: addItemToView(
            genCheckItem(getString(R.string.open_bluetooth), value, rightIcon,
                false, onClick)
        )
    }
    //endregion

    //region 通知
    private fun checkNotification(itemView: SettingItemWidget? = null) {
        val isNotifyEnabled = NotificationManagerCompat.from(this).areNotificationsEnabled()
        val onClick = if (isNotifyEnabled) null else {
            object: ((SettingItemWidget)->Unit) {
                override fun invoke(p1: SettingItemWidget) {
                    val intent = Intent()
                    try {
                        intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS

                        //8.0及以后版本使用这两个extra.  >=API 26
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                        intent.putExtra(Settings.EXTRA_CHANNEL_ID, applicationInfo.uid)

                        //5.0-7.1 使用这两个extra.  <= API 25, >=API 21
                        intent.putExtra("app_package", packageName)
                        intent.putExtra("app_uid", applicationInfo.uid)

                        startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()

                        //其他低版本或者异常情况，走该节点。进入APP设置界面
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        intent.putExtra("package", packageName)

                        //val uri = Uri.fromParts("package", packageName, null)
                        //intent.data = uri
                        startActivity(intent)
                    }
                }
            }
        }

        val rightIcon = if (isNotifyEnabled) R.drawable.ic_access else R.drawable.ic_warnning
        itemView?.let {
            updateCheckItem(it, "", rightIcon, onClick )
        } ?: addItemToView(genCheckItem(getString(R.string.open_notification), "", rightIcon, false, onClick))

    }
    //endregion

    //region 不允许电池优化
    private fun checkBattery(itemView: SettingItemWidget? = null) {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        val hasIgnored = powerManager.isIgnoringBatteryOptimizations(packageName)


        val onClick = if (hasIgnored) null else {
            object: ((SettingItemWidget)->Unit) {
                override fun invoke(p1: SettingItemWidget) {
                    Dialogs.showWhether(
                        this@PermissionCheckActivity,
                        content = getString(R.string.content_ignore_battery),
                        confirm = {
                            ignoreBatteryOptimization(this@PermissionCheckActivity)
                        }, key = null
                    )
                }
            }
        }

        val rightIcon = if (hasIgnored) R.drawable.ic_access else R.drawable.ic_warnning
        itemView?.let {
            updateCheckItem(it, "", rightIcon, onClick )
        } ?: addItemToView(genCheckItem(getString(R.string.battery_no_optimization), "", rightIcon, false, onClick))
    }
    //endregion

    //region 存储及相册
    private fun checkStorage(itemView: SettingItemWidget? = null) {

        var hasGranted = true
        PermissionsUtil.checkPermissions(this, PermissionGroups.Storage) {
            hasGranted = false
        }

        val onClick = if (hasGranted) null else {
            object: (SettingItemWidget)->Unit {
                override fun invoke(p1: SettingItemWidget) {
                    EnquireManager.instance()
                        .showEnquireOrNot(
                            this@PermissionCheckActivity,
                            getString(R.string.need_storage),
                            getString(R.string.storage_use_for), {
                                PermissionsUtil.requestPermissions(
                                    this@PermissionCheckActivity,
                                    PermissionGroups.Storage
                                )
                            }, flag = null
                        )
                }
            }
        }
        val rightIcon = if (hasGranted) R.drawable.ic_access else R.drawable.ic_warnning
        itemView?.let {
            updateCheckItem(it,"", rightIcon, onClick)
        } ?: addItemToView(genCheckItem(
            getString(R.string.open_storage),
            "", rightIcon, false, onClick
        ))

    }
    //endregion

    //region 相机
    private fun checkCamera(itemView: SettingItemWidget? = null) {
        var hasGranted = true
        PermissionsUtil.checkPermissions(this, PermissionGroups.Camera) {
            hasGranted = false
        }

        val onClick = if (hasGranted) null else {
            object: (SettingItemWidget)->Unit {
                override fun invoke(p1: SettingItemWidget) {
                    EnquireManager.instance()
                        .showEnquireOrNot(
                            this@PermissionCheckActivity,
                            getString(R.string.want_visit_camera),
                            getString(R.string.camera_use_for), {
                                PermissionsUtil.requestPermissions(
                                    this@PermissionCheckActivity,
                                    PermissionGroups.Camera
                                )
                            }, flag = null
                        )
                }
            }
        }
        val rightIcon = if (hasGranted) R.drawable.ic_access else R.drawable.ic_warnning
        itemView?.let {
            updateCheckItem(it,"", rightIcon, onClick)
        } ?: addItemToView(genCheckItem(
            getString(R.string.open_camera),
            "", rightIcon, true, onClick
        ))
    }
    //endregion

    //region 动态view生成
    private fun genCheckItem(
        title: String,
        value: String = "",
        @DrawableRes rightIcon: Int,
        noDivider: Boolean = false,
        onClick: ((SettingItemWidget)->Unit)? = null
    ): SettingItemWidget {
        val itemView = SettingItemWidget(this@PermissionCheckActivity)
        itemView.setValue(value)
        itemView.setTitle(title)
        itemView.getLeftImage().isVisible = false
        itemView.getSwitch().isVisible = false
        itemView.getRightImage().isVisible = true
        itemView.getRightImage().setImageDrawable(
            ContextCompat.getDrawable(this, rightIcon))
        itemView.setDivider(noDivider)
        itemView.getRightImage().setDebounceClickListener {
            onClick?.invoke(itemView)
        }
        return itemView
    }

    private fun updateCheckItem(
        itemView: SettingItemWidget,
        value: String = "",
        @DrawableRes rightIcon: Int,
        onClick: ((SettingItemWidget)->Unit)? = null
    ) {
        itemView.setValue(value)
        itemView.getRightImage().setImageDrawable(
            ContextCompat.getDrawable(this, rightIcon))
        itemView.getRightImage().setDebounceClickListener {
            onClick?.invoke(itemView)
        }

    }
    //endregion
}