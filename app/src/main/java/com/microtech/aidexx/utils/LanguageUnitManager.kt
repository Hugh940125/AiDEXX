package com.microtech.aidexx.utils

import android.content.Context
import com.microtech.aidexx.AidexxApp
import java.text.SimpleDateFormat
import java.util.*

object LanguageUnitManager {


    enum class LanguageUnit(
        val index: Int,
        val language: String,
        val dmyFormat: SimpleDateFormat,
        val hmFormat: SimpleDateFormat,
        val dmyhmFormat: SimpleDateFormat,
        val monthDayDateFormat: SimpleDateFormat,
    ) {

        LANGUAGE_EN(
            0,
            "en-US",
            SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
            SimpleDateFormat("HH:mm", Locale.ENGLISH),
            SimpleDateFormat("dd/MM/yyyy H:mm", Locale.ENGLISH),
            SimpleDateFormat("dd/MM", Locale.ENGLISH),
        ),
        LANGUAGE_CN(
            1,
            "zh-cn",
            SimpleDateFormat("yyyy/MM/dd", Locale.CHINA),
            SimpleDateFormat("HH:mm", Locale.CHINA),
            SimpleDateFormat("yyyy/MM/dd H:mm", Locale.CHINA),
            SimpleDateFormat("MM/dd", Locale.CHINA),
        )
    }


    fun languageUnitByIndex(context: Context): LanguageUnit {
        return when (LanguageUtil.getInstance(context).selectLanguage) {
            0 -> LanguageUnit.LANGUAGE_EN
            1 -> LanguageUnit.LANGUAGE_CN
            else -> LanguageUnit.LANGUAGE_CN
        }
    }

    fun getCurrentLanguageCode() =
        languageUnitByIndex(AidexxApp.instance).language.substring(
            0,
            2
        )

}
