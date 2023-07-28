package com.microtech.aidexx.ui.pair

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Color
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.microtech.aidexx.R
import com.microtech.aidexx.databinding.PairCheckDialogBinding
import com.microtech.aidexx.utils.DensityUtils
import com.microtech.aidexx.utils.ToastUtil
import com.microtech.aidexx.views.HyperLinkText
import com.microtech.aidexx.views.codeview.VerificationCodeView.InputCompleteListener
import com.microtechmd.blecomm.controller.BleControllerInfo


/**
 *@date 2023/7/25
 *@author Hugh
 *@desc
 */
class PairCheckDialog : AlertDialog {

    var onPass: ((info: BleControllerInfo) -> Unit)? = null

    private var mContext: Context
    private lateinit var bind: PairCheckDialogBinding

    constructor(context: Context) : this(context, 0)
    constructor(context: Context, themeResId: Int) : super(context, themeResId) {
        this.mContext = context
    }

    fun build(info: BleControllerInfo): PairCheckDialog {
        val inflate = LayoutInflater.from(context).inflate(R.layout.pair_check_dialog, null)
        bind = PairCheckDialogBinding.bind(inflate)
        val spannableStringBuilder = SpannableStringBuilder()
        spannableStringBuilder.append("当前可用设备较多，避免连接错误，请核对自身佩戴设备，并手动输入SN进行再次确认或")
        val spannableString = SpannableString("扫描二维码")
        spannableString.setSpan(
            ForegroundColorSpan(mContext.getColor(R.color.green_65)), 0, spannableString.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        //字体大小
        spannableString.setSpan(
            AbsoluteSizeSpan(DensityUtils.sp2px(16f).toInt()),
            0,
            spannableString.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            object : HyperLinkText(mContext) {
                override fun onClick(widget: View) {
                    ToastUtil.showShort("扫描二维码")
                }
            }, 0,
            spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableStringBuilder.append(spannableString)
        bind.pairTips.text = spannableStringBuilder
        bind.pairTips.movementMethod = LinkMovementMethod.getInstance()
        bind.pairTips.highlightColor = Color.TRANSPARENT
        bind.btnReselect.setOnClickListener {
            dismiss()
        }
        bind.etVerCode.isFocusable = true
        bind.etVerCode.isFocusableInTouchMode = true
        bind.etVerCode.requestFocus()
        bind.etVerCode.setInputCompleteListener(object : InputCompleteListener {
            override fun inputComplete() {
                if (bind.etVerCode.inputContent.length == bind.etVerCode.etNumber) {
                    val equals = bind.etVerCode.inputContent.equals(info.sn)
                    if (equals) {
                        onPass?.invoke(info)
                        dismiss()
                    } else {
                        bind.clReselect.isVisible = true
                        bind.tvNotSame.isVisible = true
                        bind.ivClosePairCheck.isVisible = false
                    }
                }
            }

            override fun deleteContent() {
                bind.clReselect.isVisible = false
                bind.tvNotSame.isVisible = false
                bind.ivClosePairCheck.isVisible = true
            }
        })
        bind.ivClosePairCheck.setOnClickListener {
            dismiss()
        }
        setView(bind.root)
        setOnDismissListener {
            val imm = mContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(bind.etVerCode.editText.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
        setOnShowListener {
            val imm = mContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.showSoftInput(bind.etVerCode.editText, InputMethodManager.SHOW_IMPLICIT)
        }
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return this
    }
}