package com.microtech.aidexx.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.microtech.aidexx.R
import com.microtech.aidexx.databinding.WidgetSettingItemBinding

class SettingItemWidget : RelativeLayout {
    private lateinit var settingItemBinding: WidgetSettingItemBinding

    constructor(context: Context) : super(context) {
        initLayout()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initLayout()
        val typedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.SettingItemWidget, 0, 0)
        settingItemBinding.tvTitle.text = typedArray.getString(R.styleable.SettingItemWidget_title)
        settingItemBinding.tvValue.text = typedArray.getString(R.styleable.SettingItemWidget_value)
        val icon = typedArray.getResourceId(R.styleable.SettingItemWidget_icon, 0)
        if (icon != 0) settingItemBinding.ivIconLeft.setImageResource(icon) else settingItemBinding.ivIconLeft.visibility =
            View.GONE
        val hasSwitch = typedArray.getBoolean(R.styleable.SettingItemWidget_has_switch, false)
        settingItemBinding.swOn.visibility = if (hasSwitch) View.VISIBLE else View.GONE
        val hasNext = typedArray.getBoolean(R.styleable.SettingItemWidget_has_next, false)
        settingItemBinding.tvNext.visibility = if (hasNext) View.VISIBLE else View.GONE
        val hasDown = typedArray.getBoolean(R.styleable.SettingItemWidget_has_down, false)
        settingItemBinding.tvDown.visibility = if (hasDown) View.VISIBLE else View.GONE
        val hasLine = typedArray.getBoolean(R.styleable.SettingItemWidget_has_line, true)
        settingItemBinding.viewLine.visibility = if (hasLine) View.VISIBLE else View.GONE
    }

    fun hasNext(hasNext: Boolean) {
        settingItemBinding.viewLine.visibility = if (hasNext) View.VISIBLE else View.GONE
    }

    private fun initLayout() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val inflate = inflater.inflate(R.layout.widget_setting_item, this, true)
        settingItemBinding = WidgetSettingItemBinding.bind(inflate)
    }


    fun getRightImage(): ImageView {
        return settingItemBinding.tvNext
    }


    fun setValue(str: String?) {
        settingItemBinding.tvValue.text = str
    }

    fun getSecondTextView(): TextView {
        return settingItemBinding.txtSecondValue
    }

    fun setEnable(enable: Boolean) {
        settingItemBinding.tvValue.setTextColor(
            ContextCompat.getColor(
                context, if (enable) R.color.white
                else R.color.white80
            )
        )
        settingItemBinding.tvDown.setImageResource(if (enable) R.drawable.ic_next_down else R.drawable.ic_next_down_eighty)
    }

    fun getValue() = settingItemBinding.tvValue.text.toString()

    fun getSwitch(): SwitchCompat {
        return settingItemBinding.swOn
    }
}