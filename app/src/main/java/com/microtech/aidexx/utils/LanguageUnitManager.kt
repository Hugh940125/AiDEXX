package com.microtech.aidexx.utils

import android.content.Context
import com.microtech.aidexx.AidexxApp
import java.text.SimpleDateFormat
import java.util.Locale

object LanguageUnitManager {


    enum class LanguageConf(
        val index: Int,
        val language: String,
        val dmyFormat: SimpleDateFormat,
        val hmFormat: SimpleDateFormat,
        val dmyhmFormat: SimpleDateFormat,
        val monthDayDateFormat: SimpleDateFormat,
    ) {
        LANGUAGE_CN(
            0,
            "zh-cn",
            SimpleDateFormat("yyyy/MM/dd", Locale.CHINA),
            SimpleDateFormat("HH:mm", Locale.CHINA),
            SimpleDateFormat("yyyy/MM/dd H:mm", Locale.CHINA),
            SimpleDateFormat("MM/dd", Locale.CHINA),
        ),
        LANGUAGE_EN(
            1,
            "en-US",
            SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
            SimpleDateFormat("HH:mm", Locale.ENGLISH),
            SimpleDateFormat("dd/MM/yyyy H:mm", Locale.ENGLISH),
            SimpleDateFormat("dd/MM", Locale.ENGLISH),
        )
    }


    fun getCurLanguageConf(context: Context): LanguageConf {
        return when (LanguageUtil.getInstance(context).selectLanguage) {
            0 -> LanguageConf.LANGUAGE_EN
            1 -> LanguageConf.LANGUAGE_CN
            else -> LanguageConf.LANGUAGE_CN
        }
    }

    fun getCurrentLanguageCode() =
        getCurLanguageConf(AidexxApp.instance).language.substring(
            0,
            2
        )

}
