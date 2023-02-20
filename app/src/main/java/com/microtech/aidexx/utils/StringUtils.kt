package com.microtech.aidexx.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import com.microtech.aidexx.IntentKey
import com.microtech.aidexx.R
import com.microtech.aidexx.ui.web.WebActivity

const val WELCOME = 1
const val LOGIN = 2

object StringUtils {
    fun getPrivacyPhone(mobile: String): String {
        return mobile.substring(0, 3) + "****" + mobile.substring(
            7,
            mobile.length
        )
    }

    fun initProtocol(
        context: Context,
        type: Int,
        bundle: SpannableStringBuilder
    ): SpannableStringBuilder {
        if (type == LOGIN) {
            bundle.append(context.getString(R.string.txt_protocal_1))
        } else if (type == WELCOME) {
            bundle.append(context.resources.getString(R.string.about_1))
        }
        val sp2 = SpannableString(context.getString(R.string.User_Agreement))
        val array = context.theme.obtainStyledAttributes(
            intArrayOf(
                R.attr.appColorAccent
            )
        )
        val appColorAccent = array.getColor(0, Color.WHITE)
        sp2.setSpan(
            ForegroundColorSpan(appColorAccent),
            0,
            context.getString(R.string.User_Agreement).length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        sp2.setSpan(
            object : ClickableSpan() {
                override fun onClick(view: View) {
                    val intent = Intent(context, WebActivity::class.java)
                    intent.putExtra(
                        IntentKey.WEB_TITLE,
                        context.getString(R.string.User_Agreement)
                    )
                    intent.putExtra(
                        IntentKey.WEB_URL,
                        context.getString(R.string.Terms_of_Service_url)
                    )
                    context.startActivity(intent)
                }

                override fun updateDrawState(ds: TextPaint) {
                    ds.color = appColorAccent
                    ds.isUnderlineText = true
                }
            },
            0,
            context.getString(R.string.User_Agreement).length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        bundle.append(sp2)
        bundle.append(context.getString(R.string.txt_protocal_and))
        val sp3 = SpannableString(context.getString(R.string.Privacy_Policy2))
        sp3.setSpan(
            ForegroundColorSpan(appColorAccent),
            0,
            context.getString(R.string.Privacy_Policy2).length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        sp3.setSpan(
            object : ClickableSpan() {
                override fun onClick(view: View) {
                    val intent = Intent(context, WebActivity::class.java)
                    intent.putExtra(
                        IntentKey.WEB_TITLE,
                        context.getString(R.string.Privacy_Policy2)
                    )
                    intent.putExtra(
                        IntentKey.WEB_URL,
                        context.getString(R.string.Privacy_Policy_url)
                    )
                    context.startActivity(intent)
                }

                override fun updateDrawState(ds: TextPaint) {
                    ds.color = appColorAccent
                    ds.isUnderlineText = true
                }

            },
            0,
            context.getString(R.string.Privacy_Policy2).length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        if (type == WELCOME) {
            bundle.append(context.resources.getString(R.string.about_2))
        }
        return bundle.append(sp3)
    }
}