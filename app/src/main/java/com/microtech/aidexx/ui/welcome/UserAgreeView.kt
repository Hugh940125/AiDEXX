package com.microtech.aidexx.ui.welcome

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.microtech.aidexx.IntentKey
import com.microtech.aidexx.R
import com.microtech.aidexx.ui.web.WebActivity

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
        bundle.append(context.resources.getString(R.string.about_1))

        val sp2 = SpannableString(context.resources.getString(R.string.Terms_of_Service))

        val array = context.theme.obtainStyledAttributes(
            intArrayOf(
                R.attr.appColorAccent
            )
        )
        val appColorAccent = array.getColor(0, Color.WHITE)
        sp2.setSpan(
            ForegroundColorSpan(appColorAccent),
            0,
            context.resources.getString(R.string.Terms_of_Service).length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        sp2.setSpan(
            object : ClickableSpan() {
                override fun onClick(view: View) {
                    val intent = Intent(context, WebActivity::class.java)
                    intent.putExtra(
                        IntentKey.WEB_TITLE,
                        context.resources.getString(R.string.Terms_of_Service)
                    )
                    intent.putExtra(
                        IntentKey.WEB_URL,
                        context.resources.getString(R.string.Terms_of_Service_url)
                    )
                    context.startActivity(intent)
                }

                override fun updateDrawState(ds: TextPaint) {
                    ds.color = appColorAccent
                    ds.isUnderlineText = true
                }

            },
            0,
            context.resources.getString(R.string.Terms_of_Service).length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        bundle.append(sp2)
        bundle.append(context.resources.getString(R.string.txt_protocal_and))
        val sp3 = SpannableString(context.resources.getString(R.string.Privacy_Policy))
        sp3.setSpan(
            ForegroundColorSpan(appColorAccent),
            0,
            context.resources.getString(R.string.Privacy_Policy).length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        sp3.setSpan(
            object : ClickableSpan() {
                override fun onClick(view: View) {

                    val intent = Intent(context, WebActivity::class.java)
                    intent.putExtra(
                        IntentKey.WEB_TITLE,
                        context.resources.getString(R.string.Privacy_Policy)
                    )
                    intent.putExtra(
                        IntentKey.WEB_URL,
                        context.resources.getString(R.string.Privacy_Policy_url)
                    )
                    context.startActivity(intent)
                }

                override fun updateDrawState(ds: TextPaint) {
                    ds.color = appColorAccent
                    ds.isUnderlineText = true
                }
            },
            0,
            context.resources.getString(R.string.Privacy_Policy).length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        bundle.append(sp3)
        bundle.append(context.resources.getString(R.string.about_2))
        val tvContent = v.findViewById<TextView>(R.id.tv_content)
        tvContent.text = bundle
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