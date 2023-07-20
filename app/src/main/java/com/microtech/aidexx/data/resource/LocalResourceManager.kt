package com.microtech.aidexx.data.resource

import android.os.Environment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.microtech.aidexx.BuildConfig
import com.microtech.aidexx.common.net.entity.UpgradeInfo
import com.microtech.aidexx.common.net.repository.ApiRepository
import com.microtech.aidexx.db.entity.LanguageEntity
import com.microtech.aidexx.db.entity.event.UnitEntity
import com.microtech.aidexx.db.entity.event.preset.BaseSysPreset
import com.microtech.aidexx.db.entity.event.preset.DietSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.InsulinSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.MedicineSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.SportSysPresetEntity
import com.microtech.aidexx.db.repository.EventDbRepository
import com.microtech.aidexx.db.repository.LanguageDbRepository
import com.microtech.aidexx.utils.FileUtils
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.blankj.EncryptUtils
import com.microtech.aidexx.utils.blankj.ResourceUtils
import com.microtech.aidexx.utils.mmkv.MmkvManager
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
    private const val ASSETS_RESOURCE_FILE = "resource_${BuildConfig.resourceVersion}.zip"
    private var upgrading: Boolean = false

    private val syncExceptionHandler = CoroutineExceptionHandler { context, throwable ->
        LogUtil.xLogE( "resource升级异常 \n ${throwable.stackTraceToString()}", TAG)
        upgrading = false
    }

    /**
     * 由[AppUpgradeManager]控制一天最多执行一次
     */
    fun startUpgrade(upInfo: UpgradeInfo.VersionInfo) {
        if (upgrading) {
            LogUtil.xLogE( "启动资源升级-false", TAG)
            return
        }
        upgrading = true
        LogUtil.xLogE( "启动资源升级", TAG)
        resourceUpgradeScope.launch(syncExceptionHandler) {

            if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
                LogUtil.xLogE("resource upgrade fail no sdcard", TAG)
                return@launch
            }

            val fileName = "resource_${upInfo.info.version}.zip"
            val downloadPath = FileUtils.getDownloadDir(UNZIP_DIR_NAME)

            // 启动下载
            ApiRepository.downloadFile(upInfo.info.downloadpath, downloadPath, fileName).collect { ret ->
                when (ret) {
                    is ApiRepository.NetResult.Loading -> {
                    }
                    is ApiRepository.NetResult.Success -> {
                        delay(500)

                        if (upInfo.info.sha256.isNotEmpty()) {
                            val file = File(ret.result)
                            if (FileUtils.isFileExists(file)) {
                                //sha256校验
                                val realSha256Str = EncryptUtils.encryptSHA256ToString(file.readBytes())
                                LogUtil.d("ZIP SHA256=$realSha256Str", TAG)
                                if (upInfo.info.sha256 == realSha256Str) {
                                    downloadSuccess(upInfo, ret.result)
                                } else {
                                    LogUtil.xLogE("资源下载成功后文件指纹不对", TAG)
                                }
                            } else {
                                LogUtil.xLogE("资源下载成功后文件丢了？", TAG)
                            }
                        } else {
                            downloadSuccess(upInfo, ret.result)
                        }
                        upgrading = false
                    }
                    is ApiRepository.NetResult.Failure -> {
                        upgrading = false
                        LogUtil.xLogE( "download fail ${ret.code}-${ret.msg}", TAG)
                    }
                }
            }
        }
    }

    private fun downloadSuccess(upInfo: UpgradeInfo.VersionInfo, zipFilePath: String) {
        val info = upInfo.copy(info = upInfo.info.copy(downloadpath = zipFilePath))
        val infoStr = Gson().toJson(info)
        LogUtil.d("upgradeInfo=$infoStr", TAG)
        MmkvManager.setUpgradeResourceZipFileInfo(infoStr)
    }

    suspend fun upgradeFromAssets() {
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            LogUtil.xLogE("resource upgrade fail no sdcard", TAG)
            return
        }

        var fileName = "resource_${BuildConfig.resourceVersion}.zip"
        val downloadPath = FileUtils.getDownloadDir(UNZIP_DIR_NAME)
        if (!FileUtils.delete("$downloadPath$fileName")) {
            fileName = "resource_${BuildConfig.resourceVersion}_${System.currentTimeMillis()}.zip"
        }
        val zipFilePath = "$downloadPath$fileName"
        if (ResourceUtils.copyFileFromAssets(ASSETS_RESOURCE_FILE, zipFilePath)) {
            startUpgrade(zipFilePath, BuildConfig.resourceVersion)
        } else {
            LogUtil.xLogE("内置资源包释放失败", TAG)
        }
    }

    suspend fun startUpgrade(zipFilePath: String, version: String) {
        val unzipPath = FileUtils.getDownloadDir("$UNZIP_RESOURCE_DIR_PREFIX$version")

        fun upgradeFinish(isSuccess: Boolean = false) {
            MmkvManager.setUpgradeResourceZipFileInfo("")
            val unzipFileDeleteRet = FileUtils.delete(unzipPath)
            val zipFileDeleteRet = FileUtils.delete(zipFilePath)
            LogUtil.xLogI("unzipFileDeleteRet=$unzipFileDeleteRet, zipFileDeleteRet=$zipFileDeleteRet", TAG)
        }

        runCatching {
            net.lingala.zip4j.ZipFile(zipFilePath /*, ZIP_PASSWORD.toCharArray()*/).use {
                it.extractAll(unzipPath)
            }
            true
        }.exceptionOrNull()?.let {
            LogUtil.xLogE("资源文件解压失败 v=$version e=${it.message}")
            upgradeFinish()
        } ?:let {

            val versionMenu = readJsonFileToObj("$unzipPath$FILE_VERSION_MENU", clazz = VersionMenu::class.java)
            versionMenu?.let {
                withContext(Dispatchers.IO) {
                    val parseTaskList = listOf(
                        async { updateEventSysPreset(unzipPath, it) },
                        async { updateUnit(unzipPath, it.unit) },
                        async { updateLanguage(unzipPath, it.language) },
                    )
                    val result = parseTaskList.awaitAll().all { it }
                    if (result) {
                        MmkvManager.setResourceVersion(version)
                    }
                    upgradeFinish(result)
                    LogUtil.d("升级完成 result=$result")
                }

            } ?:let {
                LogUtil.xLogE("版本文件解析失败")
                upgradeFinish()
            }
        }
    }

    private suspend fun updateEventSysPreset(unzipPath: String, versionMenu: VersionMenu): Boolean =
        withContext(Dispatchers.IO) {
            val updateTasks = listOf(
                async { updatePreset(unzipPath, FILE_FOOD_SYS, versionMenu.food_sys, DietSysPresetEntity::class.java) },
                async { updatePreset(unzipPath, FILE_EXERCISE_SYS, versionMenu.exercise_sys, SportSysPresetEntity::class.java) },
                async { updatePreset(unzipPath, FILE_MEDICATION_SYS, versionMenu.medication_sys, MedicineSysPresetEntity::class.java) },
                async { updatePreset(unzipPath, FILE_INSULIN_SYS, versionMenu.insulin_sys, InsulinSysPresetEntity::class.java) },
            )
            val result = updateTasks.awaitAll().all { it }
            LogUtil.d("系统预设升级完成", TAG)
            result
        }

    private suspend fun <T: BaseSysPreset> updatePreset(
        unzipPath: String,
        fileName: String,
        newVersion: String?,
        clazz: Class<T>
    ): Boolean = newVersion?.let {
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
                            MmkvManager.setEventSysPresetVersion(it, clazz)
                            EventDbRepository.removeSysPresetOfOtherVersion(it, clazz)
                            true
                        } else {
                            LogUtil.xLogE("updateEventSysPreset 资源文件转 entity 为空 ${clazz.simpleName}", TAG)
                            false
                        }
                    } ?:let {
                        LogUtil.xLogE("updateEventSysPreset 资源文件转 entity 失败 ${clazz.simpleName}", TAG)
                        false
                    }
                } else {
                    LogUtil.xLogE("updateEventSysPreset 解压后资源文件不存在 ${clazz.simpleName}", TAG)
                    false
                }
            } else {
                LogUtil.d("updateEventSysPreset ${clazz.simpleName} ov=$oldVersion nv=$it", TAG)
                true
            }
        } ?:let {
            EventDbRepository.removeSysPresetData(clazz)
            MmkvManager.setEventSysPresetVersion("", clazz)
            LogUtil.xLogE("清空预设 ${clazz.simpleName}", TAG)
            true
        }

    private suspend fun updateUnit(unzipPath: String, newVersion: String?): Boolean {
        var result = false
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
                            MmkvManager.setUnitVersion(it)
                            EventDbRepository.removeUnitOfOtherVersion(it)
                            result = true
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
                result = true
            }
        } ?:let {
            // 删除单位文件
            EventDbRepository.removeAllUnit()
            MmkvManager.setUnitVersion("")
            LogUtil.xLogE("清空单位", TAG)
            result = true
        }
        return result
    }

    private suspend fun updateLanguage(unzipPath: String, newVersion: String?): Boolean {
        var result = false
        newVersion?.let {
            val oldVersion = MmkvManager.getLanguageVersion()
            if (it > oldVersion) {
                val jsonFilePath = "$unzipPath$FILE_LANGUAGE"
                if (FileUtils.isFileExists(jsonFilePath)) {

                    readJsonFileToObj(
                        jsonFilePath,
                        typeToken = object : TypeToken<MutableList<LanguageEntity>>() {}
                    )?.let { list ->


                        list.map { item ->
                            item.version = it
                        }

                        if (list.isNotEmpty()) {

                            val languageRepo = LanguageDbRepository()
                            languageRepo.insert(list)
                            MmkvManager.setLanguageVersion(it)
                            languageRepo.removeLanguageOfOtherVersion(it)
                            result = true
                        } else {
                            LogUtil.xLogE("updateLanguage 资源文件转 entity 为空", TAG)
                        }
                    } ?:let {
                        LogUtil.xLogE("updateLanguage 资源文件转 entity 失败 ", TAG)
                    }
                } else {
                    LogUtil.xLogE("updateLanguage 解压后资源文件不存在", TAG)
                }
            } else {
                LogUtil.d("updateLanguage ov=$oldVersion nv=$it", TAG)
                result = true
            }

        } ?:let {
            // 删除语言文件
            LanguageDbRepository().removeAll()
            MmkvManager.setLanguageVersion("")
            result = true
        }
        return result
    }

    // todo 语言配置文件更新

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