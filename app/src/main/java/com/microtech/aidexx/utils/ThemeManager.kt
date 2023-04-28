package com.microtech.aidexx.utils

import android.content.Context
import android.util.TypedValue
import com.microtech.aidexx.R
import com.microtech.aidexx.utils.mmkv.MmkvManager

object ThemeManager {

    enum class Theme(val index: Int, val id: Int) {
        LIGHT(0, R.style.AppThemeLight),
        DARK(1, R.style.AppThemeDark) }

    var theme: Theme = themeByIndex(
        MmkvManager.getTheme()
    )
        set(theme) {
            field = theme
            MmkvManager.saveTheme(theme.index)
        }


    fun isLight(): Boolean {
        return theme.index == 0
    }

    fun themeByIndex(index: Int): Theme {
        return when (index) {
            0 -> Theme.LIGHT
            1 -> Theme.DARK
            else -> Theme.LIGHT
        }
    }

    @JvmStatic
    fun getTypeValue(context: Context?, attr: Int): Int {
        if (context == null) {
            return 0
        }
        val typedValue = TypedValue()
        context.theme?.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }
}
