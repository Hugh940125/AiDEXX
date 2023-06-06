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
import java.lang.ref.WeakReference
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.pow

const val WELCOME = 1
const val LOGIN = 2

object StringUtils {

    fun binaryToHexString(bytes: ByteArray?): String {
        var result = ""
        if (bytes == null) {
            return result
        }
        var hex: String
        for (i in bytes.indices) {
            //字节高4位
            hex = "0123456789ABCDEF"[bytes[i].toInt() and 0xF0 shr 4].toString()
            //字节低4位
            hex += "0123456789ABCDEF"[bytes[i].toInt() and 0x0F].toString()
            result += "$hex,"
        }
        return result
    }

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
        val contextRef = WeakReference(context).get()
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
            ClickSpan(contextRef, appColorAccent),
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

    class ClickSpan(private val contextRef: Context?, val color: Int) : ClickableSpan() {
        override fun onClick(widget: View) {
            contextRef?.let {
                val intent = Intent(it, WebActivity::class.java)
                intent.putExtra(
                    IntentKey.WEB_TITLE,
                    it.getString(R.string.User_Agreement)
                )
                intent.putExtra(
                    IntentKey.WEB_URL,
                    it.getString(R.string.Terms_of_Service_url)
                )
                it.startActivity(intent)
            }
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.color = color
            ds.isUnderlineText = true
        }
    }

    /** 版本比较
     * @param setupVer 字符串格式 2.5.1 或者 2.5.12
     * @param onLineVer 字符串格式 2.5.1 或者 2.5.12
     * @return  true:更新  false:不更新
     */
    fun versionCompare(setupVer: String, onLineVer: String): Boolean {
        val localVersion = setupVer.split(".")
        val netVersion = onLineVer.split(".")
        val minLength = Integer.min(netVersion.size, localVersion.size)
        var netResult = 0f
        var localResult = 0f
        for (i in 0 until minLength) {
            val netVersionBit = netVersion[i].toIntOrNull()
            val localVersionBit = localVersion[i].toIntOrNull()
            if (netVersionBit == null || localVersionBit == null) {
                return false
            }
            netResult += netVersionBit * 256f.pow(minLength - 1 - i)
            localResult += localVersionBit * 256f.pow(minLength - 1 - i)
        }
        if (localResult < netResult) {
            return true
        }
        return false
    }

    /**
     * 验证邮箱地址是否正确
     * @param email
     * @return
     */
    fun checkEmail(email: String?): Boolean {
        return try {
            val check =
                "^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$"
            val regex: Pattern = Pattern.compile(check)
            val matcher: Matcher = regex.matcher(email)
            matcher.matches()
        } catch (e: Exception) {
            false
        }
    }

}