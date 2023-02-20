package com.microtech.aidexx.ui.welcome

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import com.microtech.aidexx.R
import com.microtech.aidexx.utils.StringUtils
import com.microtech.aidexx.utils.WELCOME

class UserAgreeView : RelativeLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context)
    }

    private fun initView(context: Context) {
        val v = LayoutInflater.from(context).inflate(R.layout.layout_user_agree, this, true)
        val bundle = SpannableStringBuilder()
        val tvContent = v.findViewById<TextView>(R.id.tv_content)
        tvContent.text = StringUtils.initProtocol(context, WELCOME, bundle)
        tvContent.movementMethod = LinkMovementMethod.getInstance()
        val btOk = v.findViewById<TextView>(R.id.bt_ok)
        btOk.setOnClickListener {
            onClick?.invoke(0)
        }
        val btCancel = v.findViewById<TextView>(R.id.bt_cancel)
        btCancel.setOnClickListener {
            onClick?.invoke(1)
        }
    }

    var onClick: ((type: Int) -> Unit)? = null

}