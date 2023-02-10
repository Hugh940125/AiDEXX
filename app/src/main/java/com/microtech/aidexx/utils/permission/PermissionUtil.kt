package com.microtech.aidexx.utils

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.microtech.aidexx.R
import com.microtech.aidexx.utils.permission.Permissions
import com.microtech.aidexx.widget.dialog.standard.StandardDialog


class PermissionsUtil private constructor() {
    private lateinit var permissions: Array<String>
    private val mRequestCode = 100 //权限请求码
    private var onAllow: (() -> Unit)? = null
    private var onDeny: (() -> Unit)? = null
    var mPermissionDialog: AlertDialog? = null

    fun checkPermissions(
        context: Activity,
        permissions: Array<String>,
        allow: (() -> Unit)?,
        deny: (() -> Unit)? = null
    ) {
        onAllow = allow
        onDeny = deny
        //创建一个mPermissionList，逐个判断哪些权限未授予，未授予的权限存储到mPerrrmissionList中
        val mPermissionList: MutableList<String> = ArrayList()
        //逐个判断你要的权限是否已经通过
        for (i in permissions.indices) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permissions[i]
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                mPermissionList.add(permissions[i]) //添加还未授予的权限
            }
        }
        //申请权限
        if (mPermissionList.size > 0) { //有权限没有通过，需要申请
            ActivityCompat.requestPermissions(context, permissions, mRequestCode)
        } else {
            //说明权限都已经通过
            allow?.invoke()
            return
        }
    }

    //请求权限后回调的方法
    //参数： requestCode  是我们自己定义的权限请求码
    //参数： permissions  是我们请求的权限名称数组
    //参数： grantResults 是我们在弹出页面后是否允许权限的标识数组，数组的长度对应的是权限名称数组的长度，数组的数据0表示允许权限，-1表示我们点击了禁止权限
    fun onRequestPermissionsResult(
        context: Activity, requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        this.permissions = permissions
        var hasPermissionDismiss = false //有权限没有通过
        if (mRequestCode == requestCode) {
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    hasPermissionDismiss = true
                }
            }
            //如果有权限没有被允许
            if (hasPermissionDismiss) {
                //跳转到系统设置权限页面，或者直接关闭页面，不让他继续访问
                if (showSystemSetting) {
                    showSystemPermissionsSettingDialog(context)
                } else {
                    onDeny?.invoke()
                }
            } else {
                //全部权限通过，可以进行下一步操作。。。
                onAllow?.invoke()
            }
        }
    }

    /**
     * 不再提示权限时的展示对话框
     */
    private fun showSystemPermissionsSettingDialog(context: Activity) {
        if (mPermissionDialog == null) {
            mPermissionDialog = StandardDialog.Setter(context)
                .content(
                    String.format(
                        context.getString(
                            R.string.permission_open_manual,
                        ), Permissions.getFuncToPermission(context, permissions)
                    )
                )
                .setPositive(
                    context.getString(R.string.permission_to_setting)
                ) { _, _ ->
                    cancelPermissionDialog()
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                    intent.data = Uri.parse("package:${context.packageName}")
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    context.startActivity(intent)
                }.setCancel(
                    context.getString(R.string.btn_cancel)
                ) { _, _ ->
                    cancelPermissionDialog()
                    onDeny?.invoke()
                }.create()
        }
        mPermissionDialog?.show()
    }

    //关闭对话框
    private fun cancelPermissionDialog() {
        if (mPermissionDialog != null) {
            mPermissionDialog?.cancel()
            mPermissionDialog = null
        }
    }

    companion object {
        var showSystemSetting = true
        private var permissionsUtil: PermissionsUtil = PermissionsUtil()

        fun instance(): PermissionsUtil {
            return permissionsUtil
        }
    }
}