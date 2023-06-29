package com.microtech.aidexx.utils

import android.content.Context
import android.util.TypedValue
import androidx.appcompat.app.AppCompatDelegate
import com.microtech.aidexx.R
import com.microtech.aidexx.ui.setting.SettingsManager
import com.microtech.aidexx.views.dialog.lib.DialogX

object ThemeManager {

    enum class Theme(val index: Int, val id: Int) {
        DARK(0, R.style.AppThemeDark),
        LIGHT(1, R.style.AppThemeLight)
    }

    fun themeConfig(){
        AppCompatDelegate.setDefaultNightMode(
            if (isLight()) AppCompatDelegate.MODE_NIGHT_NO
            else AppCompatDelegate.MODE_NIGHT_YES
        )
        DialogX.globalTheme = if (isLight()) DialogX.THEME.DARK else DialogX.THEME.LIGHT
    }

    var theme: Theme = themeByIndex(SettingsManager.settingEntity!!.theme)
        set(theme) {
            field = theme
            SettingsManager.setTheme(theme.index)
        }
        get() {
            return themeByIndex(SettingsManager.settingEntity!!.theme)
        }

    fun isLight(): Boolean {
        return SettingsManager.settingEntity!!.theme == 1
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
