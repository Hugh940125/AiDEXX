package com.microtech.aidexx.widget.dialog.customerservice

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.common.compliance.EnquireFlags
import com.microtech.aidexx.common.compliance.EnquireManager
import com.microtech.aidexx.ui.main.home.customerservice.CustomServiceActivity
import com.microtech.aidexx.utils.DensityUtils
import com.microtech.aidexx.utils.PermissionsUtil
import com.microtech.aidexx.utils.ThemeManager
import com.microtech.aidexx.utils.permission.PermissionGroups

class CustomerServiceDialog : Dialog {
    constructor(context: Context) : super(context)
    constructor(context: Context, themeResId: Int) : super(context, themeResId)
    constructor(
        context: Context,
        cancelable: Boolean,
        cancelListener: DialogInterface.OnCancelListener?
    ) : super(context, cancelable, cancelListener)

    class Setter {
        private lateinit var tvUnread: TextView
        private var mCustomerServiceDialog: CustomerServiceDialog? = null
        val setMessage = {
            setDialogMessageCount()
        }
        val clearMessage = {
            setDialogMessageCount()
        }

        fun create(activity: BaseActivity<*, *>): CustomerServiceDialog? {
            val clOnline: ConstraintLayout?
            val clTel: ConstraintLayout?
            mCustomerServiceDialog = CustomerServiceDialog(activity, R.style.BottomDialog)
            mCustomerServiceDialog?.setOnDismissListener {
                MessageManager.instance().removeAddMessageListener(setMessage)
                MessageManager.instance().removeClearMessageListener(clearMessage)
            }
            val view =
                LayoutInflater.from(activity)
                    .inflate(R.layout.layout_customer_service_dialog, null)
            val closeDialog = view.findViewById<ImageView>(R.id.iv_close_service_dialog)
            closeDialog.setOnClickListener {
                mCustomerServiceDialog?.dismiss()
            }
            clOnline = view.findViewById(R.id.cl_online_service)
            tvUnread = view.findViewById(R.id.tv_dialog_message_unread)
            setDialogMessageCount()
            clTel = view.findViewById(R.id.cl_tel_service)
            clOnline?.setOnClickListener {
                MessageManager.instance().clearMessage()
                mCustomerServiceDialog?.dismiss()
                activity.startActivity(Intent(activity, CustomServiceActivity::class.java))
            }
            MessageManager.instance().setClearMessageListener(clearMessage)
            MessageManager.instance().setAddMessageListener(setMessage)
            clTel?.setOnClickListener {
                mCustomerServiceDialog?.dismiss()
                activity.let {
                    EnquireManager.instance()
                        .showEnquireOrNot(it, it.getString(R.string.want_call_phone),
                            it.getString(R.string.use_phone_call_for_service), {
                                PermissionsUtil.checkPermissions(activity, PermissionGroups.CallPhone, {
                                        val phone = "4000811831"
                                        val intent = Intent()
                                        intent.action = Intent.ACTION_DIAL
                                        intent.data =
                                            Uri.parse("tel:$phone")
                                        activity.startActivity(intent)
                                    })
                            }, {}, EnquireFlags.CUSTOMER_SERVICE_PHONE_CALL_ENQUIRE
                        )
                }
            }
            mCustomerServiceDialog?.setContentView(view)
            mCustomerServiceDialog?.setCanceledOnTouchOutside(true) // 外部点击取消
            val window: Window? = mCustomerServiceDialog?.window
            val lp: WindowManager.LayoutParams? = window?.attributes
            lp?.gravity = Gravity.BOTTOM // 紧贴底部
            lp?.width = WindowManager.LayoutParams.MATCH_PARENT // 宽度持平
            val windowHeight = DensityUtils.getWindowHeight(activity)
            lp?.height = (windowHeight * 0.5).toInt()
            window?.attributes = lp
            window?.setBackgroundDrawable(
                if (ThemeManager.isLight()) ContextCompat.getDrawable(
                    activity,
                    R.drawable.bg_follow_list_dialog_light
                ) else ContextCompat.getDrawable(
                    activity,
                    R.drawable.bg_follow_list_dialog_dark
                )
            )
            return mCustomerServiceDialog
        }

        private fun setDialogMessageCount() {
            val savedMessageCount = MessageManager.instance().getMessageCountStr()
            if (savedMessageCount.isEmpty()) {
                tvUnread.visibility = View.GONE
            } else {
                tvUnread.text = savedMessageCount
                tvUnread.visibility = View.VISIBLE
            }
        }
    }
}