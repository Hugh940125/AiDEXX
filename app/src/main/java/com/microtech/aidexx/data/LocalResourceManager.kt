package com.microtech.aidexx.data

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
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.widget.SettingItemWidget


object LocalResourceManager {

    // https://www.jianshu.com/p/91fa3f62ad6c
    // 版本检测是否需要和app版本检测一起
    // 版本号存储方式及位置
    // 有新版本时处理流程
        //1. 下载
        //2. 带入密码解压
        //3. 解析language.json文件
            // 1. str转jsonarray
            // 2. 遍历 按照语言分组
            // 3. 对语言进行分开存储 提升加载速度 直接覆盖本地已有文件？
            // 4. 保存当前支持的所有语言信息 供设置中列表展示
    // 语言切换
    // 预加载

    private val TAG = LocalResourceManager::class.java.simpleName

    private lateinit var mResourcesInspector: Resources

    fun getAidexResourceInspector(resources: Resources): Resources {
        if (!::mResourcesInspector.isInitialized) {
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