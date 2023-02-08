package com.microtech.aidexx.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.microtech.aidexx.R

class SettingItemWidget : RelativeLayout {

    constructor(context: Context) : super(context) {
        initLayout()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initLayout()
        val typedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.SettingItemWidget, 0, 0)
        tv_title.text = typedArray.getString(R.styleable.SettingItemWidget_title)
        tv_value.text = typedArray.getString(R.styleable.SettingItemWidget_value)
        val icon = typedArray.getResourceId(R.styleable.SettingItemWidget_icon, 0)
        if (icon != 0) iv_icon_left.setImageResource(icon) else iv_icon_left.visibility = View.GONE
        val hasSwitch = typedArray.getBoolean(R.styleable.SettingItemWidget_has_switch, false)
        sw_on.visibility = if (hasSwitch) View.VISIBLE else View.GONE
        val hasNext = typedArray.getBoolean(R.styleable.SettingItemWidget_has_next, false)
        tv_next.visibility = if (hasNext) View.VISIBLE else View.GONE
        val hasDown = typedArray.getBoolean(R.styleable.SettingItemWidget_has_down, false)
        tv_down.visibility = if (hasDown) View.VISIBLE else View.GONE
        val hasLine = typedArray.getBoolean(R.styleable.SettingItemWidget_has_line, true)
        view_line.visibility = if (hasLine) View.VISIBLE else View.GONE
    }

    fun hasNext(hasNext:Boolean){
        view_line.visibility = if (hasNext) View.VISIBLE else View.GONE
    }

    private fun initLayout() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.widget_setting_item, this, true)
    }


    fun getRightImage(): ImageView {
        return tv_next
    }


    fun setValue(str: String?) {
        tv_value.text = str
    }

    fun getSecondTextView(): TextView {
        return txt_second_value
    }

    fun setEnable(enable: Boolean) {

        Log.d("TAG", "enable: ${enable}")
        tv_value.setTextColor(
            ContextCompat.getColor(
                context, if (enable) R.color.white
                else R.color.white80
            )
        )

        tv_down.setImageResource(if (enable) R.drawable.ic_next_down else R.drawable.ic_next_down80)
    }

    fun getValue() = tv_value.text.toString()

    fun getSwitch(): Switch {
        return sw_on
    }
}