package com.microtech.aidexx.utils

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

object LanguageUnitManager {


    enum class LanguageUnit(
        val index: Int,
        val language: String,
        val dmyFormat: SimpleDateFormat,
        val hmFormat: SimpleDateFormat,
        val dmyhmFormat: SimpleDateFormat
    ) {

        LANGUAGE_EN(
            0,
            "en-US",
            SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
            SimpleDateFormat("HH:mm", Locale.ENGLISH),
            SimpleDateFormat("dd/MM/yyyy H:mm", Locale.ENGLISH)
        ),
        LANGUAGE_CN(
            1,
            "zh-cn",
            SimpleDateFormat("yyyy/MM/dd", Locale.CHINA),
            SimpleDateFormat("HH:mm", Locale.CHINA),
            SimpleDateFormat("yyyy/MM/dd H:mm", Locale.CHINA)
        )
    }


//    fun languageUnitByIndex(context: Context): LanguageUnit {
//        return when (LanguageUtil.getInstance(context).selectLanguage) {
//            0 -> LanguageUnit.LANGUAGE_EN
//            1 -> LanguageUnit.LANGUAGE_CN
//            else -> LanguageUnit.LANGUAGE_CN
//        }
//    }

}
