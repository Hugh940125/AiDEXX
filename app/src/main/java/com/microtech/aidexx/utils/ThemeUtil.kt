package com.microtech.aidexx.utils

import android.content.Context
import android.util.TypedValue
import com.microtech.aidexx.R

object ThemeUtil {

    enum class Theme(val index: Int, val id: Int) {
        DARK(0, R.style.AppThemeDark),
        LIGHT(1, R.style.AppThemeLight)
    }

    var theme: Theme = themeByIndex(
        MMKV.defaultMMKV().decodeInt(LocalPreference.THEME, 1)
    )
        set(theme) {
            field = theme
            MMKV.defaultMMKV().encode(LocalPreference.THEME, theme.index)
        }


    fun isLight(): Boolean {

        return theme.index == 1
    }

    fun themeByIndex(index: Int): Theme {
        return when (index) {
            0 -> Theme.DARK
            1 -> Theme.LIGHT
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
