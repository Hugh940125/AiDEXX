package com.microtech.aidexx.data.resource

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.LayoutInflaterCompat
import com.microtech.aidexx.db.repository.LanguageDbRepository
import com.microtech.aidexx.utils.LanguageUtil
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.mmkv.MmkvManager
import com.microtech.aidexx.widget.SettingItemWidget
import java.text.SimpleDateFormat
import java.util.Locale

object LocalManager {

    private const val TAG = "LocalManager"

    private lateinit var mResourcesInspector: Resources

    // 初次进入时系统语言


    suspend fun onLanguageChanged(languageTag: String) {

    }

    /**
     * 加载当前语言资源到内存
     */
    suspend fun loadLanguage() {

    }

    // todo 需要配置 code-item映射表
    suspend fun getSupportLanguages(): MutableList<String> {
        val conf = LanguageDbRepository().querySupportLanguages()
       if (conf.isNullOrEmpty()) {
           LogUtil.xLogE("没有语言相关配置", TAG)
           return getDefaultSupportLanguages()
       }
        return conf
    }


    fun getCurLanguageTag() = MmkvManager.getCurrentLanguageTag()

    fun getCurLanguageConf(context: Context): LanguageConf {
        return when (LanguageUtil.getInstance(context).selectLanguage) {
            0 -> LanguageConf.LANGUAGE_EN
            1 -> LanguageConf.LANGUAGE_CN
            else -> LanguageConf.LANGUAGE_CN
        }
    }

    private fun getDefaultSupportLanguages(): MutableList<String> {
        return mutableListOf("简体中文","English")
    }

    fun getAidexResourceInspector(resources: Resources): Resources {
        if (!LocalManager::mResourcesInspector.isInitialized) {
            mResourcesInspector = object: Resources(
                resources.assets,
                resources.displayMetrics,
                resources.configuration
            ) {
                @SuppressLint("ResourceType")
                override fun getString(id: Int): String {
                    LogUtil.d("==GB== id=$id name=${resources.getResourceEntryName(id)}")
                    if (id < 2131800000) {
                        return super.getString(id)
                    }
                    return "哼哼"
                }
            }
        }
        return mResourcesInspector
    }

    fun injectFactory2(layoutInflater: LayoutInflater) {
        LayoutInflaterCompat.setFactory2(layoutInflater, object : LayoutInflater.Factory2 {
            override fun onCreateView(
                parent: View?,
                name: String,
                context: Context,
                attrs: AttributeSet
            ): View? {

                LogUtil.d("==GB== factory2 name=$name")
                val inflater = LayoutInflater.from(context)
                var activity: AppCompatActivity? = null
                if (parent == null) {
                    if (context is AppCompatActivity) {
                        activity = context
                    }
                } else if (parent.context is AppCompatActivity) {
                    activity = parent.context as AppCompatActivity
                }

                if (activity == null) {
                    LogUtil.xLogE("==GB== injectFactory2 act null", TAG)
                    return null
                }

                val actDelegate = activity.delegate

                val set = intArrayOf(
                    R.attr.text
                )
                @SuppressLint("Recycle")
                val typedArray: TypedArray = context.obtainStyledAttributes(attrs, set)

                var view = actDelegate.createView(parent, name, context, attrs)

                if (view == null && name.indexOf('.') > 0) {
                    try {
                        view = inflater.createView(name, null, attrs)
                        LogUtil.d("==GB== factory2 view=$view name=$name")
                    } catch (e: ClassNotFoundException) {
                        e.printStackTrace()
                        LogUtil.d("==GB== factory2 view=null name=$name e=${e.message}")
                    }
                }
                if (view is TextView) {
                    val resourceId = typedArray.getResourceId(0, 0)
                    if (resourceId != 0) {
                        view.text = "哈哈"
                    }
                } else if (view is SettingItemWidget) { // 自定义view看能否找到合理方式

                    @SuppressLint("Recycle")
                    val typedArray: TypedArray = context.obtainStyledAttributes(attrs, com.microtech.aidexx.R.styleable.SettingItemWidget)
                    typedArray.getIndex(typedArray.indexCount)
                    val resourceId = typedArray.getResourceId(com.microtech.aidexx.R.styleable.SettingItemWidget_title, 0)
                    if (resourceId != 0) {
                        LogUtil.d("==GB== strName=${view.resources.getResourceEntryName(resourceId)}")
//                        view.setTitle("自定义")
                    }

                }

                return view
            }

            override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
                return null
            }
        })
    }
}


enum class LanguageConf(
    val index: Int,
    val accept_language: String,
    val yearMonthDayDateFormat: SimpleDateFormat,
    val hourMinuteDateFormat: SimpleDateFormat,
    val fullDateFormat: SimpleDateFormat,
    val monthDayDateFormat: SimpleDateFormat,
    val yearMonthFormat: SimpleDateFormat,
    val fullDateFormatWithSeparator: SimpleDateFormat,

    ) {

    LANGUAGE_EN(
        0,
        "en-US",
        SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
        SimpleDateFormat("HH:mm", Locale.ENGLISH),
        SimpleDateFormat("dd/MM/yyyy H:mm", Locale.ENGLISH),
        SimpleDateFormat("dd/MM", Locale.ENGLISH),
        SimpleDateFormat("yyyy/MM", Locale.ENGLISH),
        SimpleDateFormat("dd-MM-yyyy H:mm", Locale.ENGLISH)

    ),
    LANGUAGE_CN(
        1,
        "zh-cn",
        SimpleDateFormat("yyyy/MM/dd", Locale.CHINA),
        SimpleDateFormat("HH:mm", Locale.CHINA),
        SimpleDateFormat("yyyy/MM/dd H:mm", Locale.CHINA),
        SimpleDateFormat("MM/dd", Locale.CHINA),
        SimpleDateFormat("yyyy/MM", Locale.ENGLISH),
        SimpleDateFormat("yyyy-MM-dd H:mm", Locale.CHINA),

        ),
    LANGUAGE_CS(
        2,
        "cs",
        SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
        SimpleDateFormat("HH:mm", Locale.ENGLISH),
        SimpleDateFormat("dd/MM/yyyy H:mm", Locale.ENGLISH),
        SimpleDateFormat("dd/MM", Locale.ENGLISH),
        SimpleDateFormat("yyyy/MM", Locale.ENGLISH),
        SimpleDateFormat("dd-MM-yyyy H:mm", Locale.ENGLISH),


        ),
    LANGUAGE_SK(
        3,
        "sk",
        SimpleDateFormat("dd/MM/yyyy", Locale.KOREA),
        SimpleDateFormat("HH:mm", Locale.KOREA),
        SimpleDateFormat("dd/MM/yyyy H:mm", Locale.KOREA),
        SimpleDateFormat("dd/MM", Locale.KOREA),
        SimpleDateFormat("yyyy/MM", Locale.ENGLISH),
        SimpleDateFormat("dd-MM-yyyy H:mm", Locale.KOREA),

        ),
    LANGUAGE_FR(
        4,
        "fr",
        SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE),
        SimpleDateFormat("HH:mm", Locale.FRANCE),
        SimpleDateFormat("dd/MM/yyyy H:mm", Locale.FRANCE),
        SimpleDateFormat("dd/MM", Locale.FRANCE),
        SimpleDateFormat("yyyy/MM", Locale.ENGLISH),
        SimpleDateFormat("dd-MM-yyyy H:mm", Locale.FRANCE),


        ),
    LANGUAGE_AR(
        5,
        "ar",
        SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH),
        SimpleDateFormat("HH:mm", Locale.ENGLISH),
        SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH),
        SimpleDateFormat("MM/dd", Locale.ENGLISH),
        SimpleDateFormat("yyyy/MM", Locale.ENGLISH),
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH),
    ),
    LANGUAGE_IT(
        6,
        "it",
        SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
        SimpleDateFormat("HH:mm", Locale.ENGLISH),
        SimpleDateFormat("dd/MM/yyyy H:mm", Locale.ENGLISH),
        SimpleDateFormat("dd/MM", Locale.ENGLISH),
        SimpleDateFormat("yyyy/MM", Locale.ENGLISH),
        SimpleDateFormat("dd-MM-yyyy H:mm", Locale.ENGLISH),

        ),
    LANGUAGE_MN(
        7,
        "mn-MN",
        SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
        SimpleDateFormat("HH:mm", Locale.ENGLISH),
        SimpleDateFormat("dd/MM/yyyy H:mm", Locale.ENGLISH),
        SimpleDateFormat("dd/MM", Locale.ENGLISH),
        SimpleDateFormat("yyyy/MM", Locale.ENGLISH),
        SimpleDateFormat("dd-MM-yyyy H:mm", Locale.ENGLISH),

        ),
    LANGUAGE_RO(
        8,
        "ro",
        SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
        SimpleDateFormat("HH:mm", Locale.ENGLISH),
        SimpleDateFormat("dd/MM/yyyy H:mm", Locale.ENGLISH),
        SimpleDateFormat("dd/MM", Locale.ENGLISH),
        SimpleDateFormat("yyyy/MM", Locale.ENGLISH),
        SimpleDateFormat("dd-MM-yyyy H:mm", Locale.ENGLISH),

        ),
    LANGUAGE_TR(
        9,
        "tr",
        SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
        SimpleDateFormat("HH:mm", Locale.ENGLISH),
        SimpleDateFormat("dd/MM/yyyy H:mm", Locale.ENGLISH),
        SimpleDateFormat("dd/MM", Locale.ENGLISH),
        SimpleDateFormat("yyyy/MM", Locale.ENGLISH),
        SimpleDateFormat("dd-MM-yyyy H:mm", Locale.ENGLISH),

        ),
    LANGUAGE_ES(
        10,
        "es",
        SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
        SimpleDateFormat("HH:mm", Locale.ENGLISH),
        SimpleDateFormat("dd/MM/yyyy H:mm", Locale.ENGLISH),
        SimpleDateFormat("dd/MM", Locale.ENGLISH),
        SimpleDateFormat("yyyy/MM", Locale.ENGLISH),
        SimpleDateFormat("dd-MM-yyyy H:mm", Locale.ENGLISH),

        ),
    LANGUAGE_RU(
        11,
        "ru",
        SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
        SimpleDateFormat("HH:mm", Locale.ENGLISH),
        SimpleDateFormat("dd/MM/yyyy H:mm", Locale.ENGLISH),
        SimpleDateFormat("dd/MM", Locale.ENGLISH),
        SimpleDateFormat("yyyy/MM", Locale.ENGLISH),
        SimpleDateFormat("dd-MM-yyyy H:mm", Locale.ENGLISH),


        ),
    LANGUAGE_DE(
        12,
        "de",
        SimpleDateFormat("dd/MM/yyyy"),
        SimpleDateFormat("HH:mm"),
        SimpleDateFormat("dd/MM/yyyy H:mm"),
        SimpleDateFormat("dd/MM"),
        SimpleDateFormat("yyyy/MM", Locale.ENGLISH),
        SimpleDateFormat("dd-MM-yyyy H:mm"),

        ),
    LANGUAGE_SV(
        13,
        "sv",
        SimpleDateFormat("dd/MM/yyyy", Locale("sv")),
        SimpleDateFormat("HH:mm", Locale("sv")),
        SimpleDateFormat("dd/MM/yyyy H:mm", Locale("sv")),
        SimpleDateFormat("dd/MM", Locale("sv")),
        SimpleDateFormat("MM/yyyy", Locale("sv")),
        SimpleDateFormat("dd-MM-yyyy H:mm", Locale("sv")),
    )
}