package com.microtech.aidexx.utils

import android.content.Context
import android.util.TypedValue
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.R
import com.microtech.aidexx.ui.setting.SettingsManager
import com.microtech.aidexx.utils.mmkv.MmkvManager
import kotlinx.coroutines.launch

object ThemeManager {
    lateinit var themeCurrent: Theme

    enum class Theme(val index: Int, val id: Int) {
        DARK(0, R.style.AppThemeDark),
        LIGHT(1, R.style.AppThemeLight)
    }

    suspend fun getCurrentTheme(): Theme {
        if (!::themeCurrent.isInitialized){
            themeCurrent = themeByIndex(SettingsManager.getSettings().theme)
        }
        return themeCurrent
    }


//        set(theme) {
//            field = theme
//            SettingsManager.setTheme(theme.index)
//        }

    fun isLight(): Boolean {
        return themeCurrent.index == 0
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
