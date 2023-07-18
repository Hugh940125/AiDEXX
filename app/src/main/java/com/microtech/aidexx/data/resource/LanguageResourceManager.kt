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
import com.microtech.aidexx.db.entity.LanguageConfEntity
import com.microtech.aidexx.db.repository.LanguageDbRepository
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.mmkv.MmkvManager
import com.microtech.aidexx.views.SettingItemWidget

object LanguageResourceManager {

    private const val TAG = "LocalManager"

    private lateinit var mResourcesInspector: Resources

    // 初次进入时系统语言


    suspend fun onLanguageChanged(languageConf: LanguageConfEntity) {

    }

    /**
     * 加载当前语言资源到内存
     */
    suspend fun loadLanguageInfo() {

    }

    suspend fun getSupportLanguages(): MutableList<LanguageConfEntity> {
        val conf = LanguageDbRepository().querySupportLanguages()
        return conf?.ifEmpty { null } ?: getDefaultSupportLanguages()
    }

    fun getCurLanguageTag() = MmkvManager.getCurrentLanguageTag()

    suspend fun getCurLanguageConfEntity() =
        LanguageDbRepository().queryConfById(getCurLanguageTag())

    private fun getDefaultSupportLanguages(): MutableList<LanguageConfEntity> {
        return mutableListOf(
            LanguageConfEntity(name = "简体中文", langId = "zh-Hans-CN"),
            LanguageConfEntity(name = "English", langId = "en-us")
        )
    }

    fun getAidexResourceInspector(resources: Resources): Resources {
        if (!LanguageResourceManager::mResourcesInspector.isInitialized) {
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