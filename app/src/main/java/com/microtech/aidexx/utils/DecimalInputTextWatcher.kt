package com.microtech.aidexx.utils

import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.widget.EditText
import com.microtech.aidexx.common.getContext
import java.text.DecimalFormatSymbols


/**
 * @param editText      editText
 * @param totalDigits   最大长度
 * @param decimalDigits 小数的位数
 */
class DecimalInputTextWatcher(
    editText: EditText,
    totalDigits: Int,
    decimalDigits: Int = DEFAULT_DECIMAL_DIGITS
): TextWatcher {

    private var editText: EditText
    private val decimalDigits: Int // 小数的位数
    private val totalDigits: Int //最大长度
    private var decimalSeparator: String //APP选中语言的小数点 . 或者 ,


    init {
        this.editText = editText
        if (totalDigits <= 0) throw RuntimeException("totalDigits must > 0")
        if (decimalDigits <= 0) throw RuntimeException("decimalDigits must > 0")
        this.totalDigits = totalDigits
        this.decimalDigits = decimalDigits

        decimalSeparator =
            DecimalFormatSymbols.getInstance(LocalManageUtil.getSetLanguageLocale(getContext())).decimalSeparator.toString()

        this.editText.filters = arrayOf(DigitFilter(decimalSeparator).apply {
            pointerLength = 1
        })
    }

    fun getDoubleValue(): Double? {
        val text = editText.text?.toString()
        if (text.isNullOrEmpty()) {
            return null
        }
        return text.replace(decimalSeparator,".").toDoubleOrNull()
    }

    fun setValue() {

    }

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
    }
    override fun afterTextChanged(editable: Editable) {
        try {
            var s = editable.toString()
            editText.removeTextChangedListener(this)

            var limitLen = 1000
            if (s.contains(decimalSeparator)) {
                limitLen = totalDigits
                //限制最大长度
                editText.filters = arrayOf<InputFilter>(LengthFilter(limitLen))
                //超过小数位限定位数,只保留限定小数位数
                if (s.length - 1 - s.indexOf(decimalSeparator) > decimalDigits) {
                    s = s.substring( 0, s.indexOf(decimalSeparator) + decimalDigits + 1 )
                    editable.replace(0, editable.length, s.trim { it <= ' ' })
                }

            } else {
                val maxLen = totalDigits - decimalDigits // 长度限制多一位为了能输入小数点
                limitLen = maxLen
                //限制最大长度
                editText.filters = arrayOf<InputFilter>(LengthFilter(limitLen))
                if(s.length >= maxLen && !s.endsWith(decimalSeparator)) {
                    editable.replace(maxLen-1,maxLen,"")
                }
            }

            //如果首位输入"."自动补0
            if (s.trim { it <= ' ' } == decimalSeparator) {
                s = Zero + s
                editable.replace(0, editable.length, s.trim { it <= ' ' })
            }
            //首位输入0时 后面如果不是. 则把0去掉
            if (s.startsWith(Zero)
                && s.trim { it <= ' ' }.length > 1
            ) {
                if (s.substring(1, 2) != decimalSeparator) {
                    editable.replace(0, 1, "")
                }
            }

            editText.filters = arrayOf(DigitFilter(decimalSeparator).apply {
                pointerLength = 1
            },LengthFilter(limitLen))

            editText.addTextChangedListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val Zero = "0"

        /**
         * 默认  小数的位数   2 位
         */
        private const val DEFAULT_DECIMAL_DIGITS = 2
    }
}