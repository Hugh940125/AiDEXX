package com.microtech.aidexx.data

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.os.Environment
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.LayoutInflaterCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.microtech.aidexx.common.net.entity.UpgradeInfo
import com.microtech.aidexx.common.net.repository.ApiRepository
import com.microtech.aidexx.db.entity.event.UnitEntity
import com.microtech.aidexx.db.entity.event.preset.BaseSysPreset
import com.microtech.aidexx.db.entity.event.preset.DietSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.InsulinSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.MedicineSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.SportSysPresetEntity
import com.microtech.aidexx.db.repository.EventDbRepository
import com.microtech.aidexx.utils.FileUtils
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.mmkv.MmkvManager
import com.microtech.aidexx.widget.SettingItemWidget
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private val resourceUpgradeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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

    private const val ZIP_PASSWORD = "weitai2020!"
    private const val UNZIP_DIR_NAME = "downloads"
    private const val UNZIP_RESOURCE_DIR_PREFIX = "$UNZIP_DIR_NAME/resource_"
    private const val FILE_VERSION_MENU = "version_menu.json"
    private const val FILE_LANGUAGE = "language.json"
    private const val FILE_UNIT = "unit.json"
    private const val FILE_EXERCISE_SYS = "exercise_sys.json"
    private const val FILE_MEDICATION_SYS = "medication_sys.json"
    private const val FILE_FOOD_SYS = "food_sys.json"
    private const val FILE_INSULIN_SYS = "insulin_sys.json"
    private const val FILE_OTHER_SYS = "other_sys.json"

    private val syncExceptionHandler = CoroutineExceptionHandler { context, throwable ->
        LogUtil.xLogE( "resource升级异常 \n ${throwable.stackTraceToString()}", TAG)
    }

    fun startUpgrade(upInfo: UpgradeInfo.VersionInfo) {

        resourceUpgradeScope.launch(syncExceptionHandler) {

            if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
                LogUtil.xLogE("resource upgrade fail no sdcard", TAG)
                return@launch
            }

            val fileName = "resource_${upInfo.info.version}.zip"
            val downloadPath = FileUtils.getDownloadDir(UNZIP_DIR_NAME)

//            processByVersionFile("$downloadPath/$fileName", upInfo.info.version)
//            return@launch

            // 启动下载
            ApiRepository.downloadFile(upInfo.info.downloadpath, downloadPath, fileName).collect { ret ->
                when (ret) {
                    is ApiRepository.NetResult.Loading -> {
                    }
                    is ApiRepository.NetResult.Success -> {
                        delay(500)
                        // 下载成功 密码解压
                        processByVersionFile(ret.result, upInfo.info.version)
                    }
                    is ApiRepository.NetResult.Failure -> {
                        LogUtil.xLogE( "download fail ${ret.code}-${ret.msg}", TAG)
                    }
                }
            }
        }
    }

    private suspend fun processByVersionFile(zipFilePath: String, version: String) {
        val unzipPath = FileUtils.getDownloadDir("$UNZIP_RESOURCE_DIR_PREFIX$version")

        runCatching {
            net.lingala.zip4j.ZipFile(zipFilePath, ZIP_PASSWORD.toCharArray()).use {
                it.extractAll(unzipPath)
            }
            true
        }.exceptionOrNull()?.let {
            LogUtil.xLogE("资源文件解压失败 v=$version e=${it.message}")
        } ?:let {

            val versionMenu = readJsonFileToObj("$unzipPath$FILE_VERSION_MENU", clazz = VersionMenu::class.java)
            versionMenu?.let {
                withContext(Dispatchers.IO) {
                    val parseTaskList = listOf(
                        async { updateEventSysPreset(unzipPath, it) },
                        async { updateUnit(unzipPath, it.unit) },
                        async { updateLanguage(unzipPath, it.language) },
                    )
                    parseTaskList.awaitAll()
                    LogUtil.d("升级完成")
                }

            } ?:let {
                LogUtil.xLogE("版本文件解析失败")
            }
        }
    }

    private suspend fun updateEventSysPreset(unzipPath: String, versionMenu: VersionMenu) {
        withContext(Dispatchers.IO) {
            val updateTasks = listOf(
                async { updatePreset(unzipPath, FILE_FOOD_SYS, versionMenu.food_sys, DietSysPresetEntity::class.java) },
                async { updatePreset(unzipPath, FILE_EXERCISE_SYS, versionMenu.exercise_sys, SportSysPresetEntity::class.java) },
                async { updatePreset(unzipPath, FILE_MEDICATION_SYS, versionMenu.medication_sys, MedicineSysPresetEntity::class.java) },
                async { updatePreset(unzipPath, FILE_INSULIN_SYS, versionMenu.insulin_sys, InsulinSysPresetEntity::class.java) },
            )
            updateTasks.awaitAll()
            LogUtil.d("系统预设升级完成", TAG)
        }
    }

    private suspend fun <T: BaseSysPreset> updatePreset(unzipPath: String, fileName: String, newVersion: String?, clazz: Class<T>) {

        newVersion?.let {
            val oldVersion = MmkvManager.getEventSysPresetVersion(clazz)
            if (it > oldVersion) {
                val jsonFilePath = "$unzipPath$fileName"
                if (FileUtils.isFileExists(jsonFilePath)) {

                    fun getTypeToken() = when(clazz) {
                        DietSysPresetEntity::class.java -> object : TypeToken<MutableList<DietSysPresetEntity>>() {}
                        SportSysPresetEntity::class.java -> object : TypeToken<MutableList<SportSysPresetEntity>>() {}
                        MedicineSysPresetEntity::class.java -> object : TypeToken<MutableList<MedicineSysPresetEntity>>() {}
                        InsulinSysPresetEntity::class.java -> object : TypeToken<MutableList<InsulinSysPresetEntity>>() {}
                        else -> null
                    }

                    val dataList = readJsonFileToObj(
                        jsonFilePath,
                        typeToken = getTypeToken()
                    )

                    dataList?.let { list ->
                        list.forEach { item ->
                            if (item is BaseSysPreset) {
                                item.version = it
                            }
                        }
                        if (list.isNotEmpty()) {
                            EventDbRepository.insertSysPresetData(list as List<BaseSysPreset>)
                            EventDbRepository.removeSysPresetOfOtherVersion(it, clazz)
                            MmkvManager.setEventSysPresetVersion(it, clazz)
                        } else {
                            LogUtil.xLogE("updateEventSysPreset 资源文件转 entity 为空 ${clazz.simpleName}", TAG)
                        }
                    } ?:let {
                        LogUtil.xLogE("updateEventSysPreset 资源文件转 entity 失败 ${clazz.simpleName}", TAG)
                    }
                } else {
                    LogUtil.xLogE("updateEventSysPreset 解压后资源文件不存在 ${clazz.simpleName}", TAG)
                }
            } else {
                LogUtil.d("updateEventSysPreset ${clazz.simpleName} ov=$oldVersion nv=$it", TAG)
            }
        } ?:let {
            EventDbRepository.removeSysPresetData(clazz)
            MmkvManager.setEventSysPresetVersion("", clazz)
            LogUtil.xLogE("清空预设 ${clazz.simpleName}", TAG)
        }
    }

    private suspend fun updateUnit(unzipPath: String, newVersion: String?) {
        newVersion?.let {
            val oldVersion = MmkvManager.getUnitVersion()
            if (it > oldVersion) {
                val jsonFilePath = "$unzipPath$FILE_UNIT"
                if (FileUtils.isFileExists(jsonFilePath)) {
                    readJsonFileToObj(
                        jsonFilePath,
                        typeToken = object : TypeToken<MutableList<UnitEntity>>() {}
                    )?.let { list ->

                        list.forEach { item ->
                            item.version = it
                        }
                        if (list.isNotEmpty()) {
                            EventDbRepository.insertUnit(list)
                            EventDbRepository.removeUnit(it)
                            MmkvManager.setUnitVersion(it)
                        } else {
                            LogUtil.xLogE("updateUnit 资源文件转 entity 为空", TAG)
                        }

                    } ?:let {
                        LogUtil.xLogE("updateUnit 资源文件转 entity 失败 ", TAG)
                    }
                } else {
                    LogUtil.xLogE("updateUnit 解压后资源文件不存在", TAG)
                }
            } else {
                LogUtil.d("updateUnit ov=$oldVersion nv=$it", TAG)
            }
        } ?:let {
            // 删除单位文件
            EventDbRepository.removeAllUnit()
            MmkvManager.setUnitVersion("")
            LogUtil.xLogE("清空单位", TAG)
        }
    }

    private fun updateLanguage(unzipPath: String, newVersion: String?) {
        newVersion?.let {

        } ?:let {
            // 删除语言文件
        }
    }

    private fun <R> readJsonFileToObj(jsonFilePath: String, clazz: Class<R>? = null, typeToken: TypeToken<R>? = null): R? {
        clazz ?: typeToken ?: return null
        if (FileUtils.isFileExists(jsonFilePath)) {
            val gson = Gson()
            return runCatching {
                File(jsonFilePath).reader().use { reader ->
                    clazz?.let {
                        gson.fromJson(reader, it)
                    } ?:let {
                        gson.fromJson(reader, typeToken!!.type)
                    }
                }
            }.getOrNull()
        }
        return null
    }


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

//{"unit":"2023-06-02 11:09:25",
// "insulin_sys":"2023-06-01 15:31:40",
// "other_sys":"2023-06-01 15:04:21",
// "language":"2023-05-26 14:52:07",
// "exercise_sys":"2023-06-01 15:28:35",
// "food_sys":"2023-06-01 15:32:01",
// "medication_sys":"2023-06-01 15:31:09"}
data class VersionMenu(
    val language: String?,
    val unit: String?,
    val insulin_sys: String?,
    val other_sys: String?,
    val exercise_sys: String?,
    val food_sys: String?,
    val medication_sys: String?,
)