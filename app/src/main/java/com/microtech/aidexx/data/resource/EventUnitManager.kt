package com.microtech.aidexx.data.resource

import com.google.gson.Gson
import com.microtech.aidexx.BuildConfig
import com.microtech.aidexx.R
import com.microtech.aidexx.common.getContext
import com.microtech.aidexx.db.entity.event.UnitConfig
import com.microtech.aidexx.db.entity.event.UnitEntity
import com.microtech.aidexx.db.repository.EventDbRepository
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.LogUtils
import com.microtech.aidexx.utils.mmkv.MmkvManager
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.BufferedReader

data class SpecificationModel(
    var specification: String = "",
    val ratio: Double = 1.0,
    val isDefault: Boolean = false,
    val code: Int = 0,
    var check: Boolean = false
)

object EventUnitManager {

    private val TAG = EventUnitManager::class.java.simpleName
    private val updateScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private const val GET_UNIT_DATA = "/eventBizUnitMultiLangMap"
    private const val UNIT_CONFIG_FILE_NAME = "unit.json"

    private const val EVENT_TYPE_DIET = 1
    private const val EVENT_TYPE_MEDICINE = 2
    private const val EVENT_TYPE_EXERCISE = 3
    private const val EVENT_TYPE_INSULIN = 4

    // debug 1min 间隔检测升级 release 24小时间隔
    private val UPDATE_INTERVAL = if (BuildConfig.DEBUG) 60 * 1000 else 24 * 60 * 60 * 1000

    private var isUpdating = false
    private val updateExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        LogUtil.e("context=$coroutineContext, throwable=${throwable.stackTraceToString()}", TAG)
        stopUpdate()
    }

    private val mUnitMap = mutableMapOf<Int, MutableList<SpecificationModel>>()

    /**
     * 语言单位检测升级
     */
    @Synchronized
    fun update(coroutineScope: CoroutineScope = updateScope) {
        if (canStartTask()) {
            isUpdating = true
            coroutineScope.launch(updateExceptionHandler) {
//                checkAndUpdate()
            }
        }
    }

    /**
     * 语言切换时调用
     */
    fun onLanguageChanged(lang: String) {
        loadUnit(lang)
    }

    /**
     * 加载语言单位
     */
    fun loadUnit(lang: String, coroutineScope: CoroutineScope = updateScope) {
        coroutineScope.launch {
            val ret = EventDbRepository.loadUnit(lang)
            ret?.let {
                updateMemo(ret)
            }
        }
    }

    private fun canStartTask(): Boolean =
        when {
            isUpdating -> {
                LogUtils.debug(TAG, "正在升级...")
                false
            }
            System.currentTimeMillis() - MmkvManager.getUnitLatestUpdateTime() < UPDATE_INTERVAL -> {
                LogUtils.debug(TAG, "不在升级时间区间")
                false
            }
            else -> true
        }

    private fun stopUpdate() {
        isUpdating = false
    }

    /**
     * 当前apk版本是否加载过配置
     */
    private fun isLoadedCurrentApkVer(): Boolean =
        MmkvManager.getUnitLoadedApkVersion() >= BuildConfig.VERSION_CODE

    /**
     * ！！！由于服务端不加版本接口 所以下面这个逻辑没用了
     * 是否有释放过当前版本的内置数据
     *  有释放
     *      拉天上版本 如果有更新就执行更新逻辑
     *      更新成功后 本地版本
     *
     *  没释放过 先加载内置数据
     *      比较本地和内置版本
     *          本地版本大 -> 和天上版本对比 有更新就调用更新接口拉最新配置
     *      本地版本小 -> 去拿天上版本
     *          天上版本比内置版本大 直接去拉天上版本
     *          天上版本与内置版本相同 直接把内置数据更新入库
     *          不会出现天上版本比内置小的情况
     *      更新成功后 更新内置数据释放标记 以及 本地版本
     */
//    private suspend fun checkAndUpdate() {
//
//        if(isLoadedCurrentApkVer()) {
//            LogUtils.debug(TAG,"内置数据释放过")
//            checkAndUpdateFromServer()
//        } else {
//
//            val uc = loadFromAssets()
//
//            LogUtils.debug(TAG,"内置数据未释放过 localUc.ver=${uc.version}")
//            checkAndUpdateFromServer(
//                if(uc.version > MmkvManager.getUnitVersion())
//                    uc
//                else
//                    null
//            )
//
//        }
//        stopUpdate()
//    }

    private fun loadFromAssets(): UnitConfig {

        val content = getContext().resources.assets.open(UNIT_CONFIG_FILE_NAME)
            .bufferedReader().use(BufferedReader::readText)

        return Gson().fromJson(content, UnitConfig::class.java)
    }

    /**
     *
     */
//    private suspend fun checkAndUpdateFromServer(localUc: UnitConfig? = null) {
//        val serverUc = loadFromServer()
//        serverUc?.let {
//            if(it.version > MmkvManager.getUnitVersion()) {
//                updateDbAndMemo(serverUc)
//                LogUtils.debug(TAG,"写入天上最新数据")
//            } else {
//                LogUtils.debug(TAG,"天上数据未更新")
//            }
//
//            MmkvManager.setUnitLatestUpdateTime(System.currentTimeMillis())
//
//        } ?: localUc?.let {
//            updateDbAndMemo(localUc)
//            LogUtils.debug(TAG,"写入内置数据")
//        }
//
//    }


    private suspend fun updateDbAndMemo(list: List<UnitEntity>){
        // 依赖key自动替换
        EventDbRepository.insertUnit(list)

        updateMemo(list)

        LogUtils.debug(TAG, "unit loadedApkVersion=${BuildConfig.VERSION_CODE}")
        MmkvManager.setUnitLoadedApkVersion(BuildConfig.VERSION_CODE)
    }

    /**
     * 更新内存中当前语言对应的单位数据
     */
    private fun updateMemo(list: List<UnitEntity>) {
        val newestMap = mutableMapOf<Int, MutableList<SpecificationModel>>()
        list.filter {
            it.language == LanguageResourceManager.getCurLanguageTag() && it.version == MmkvManager.getUnitVersion()
        }.forEach {
            val sm = SpecificationModel(it.name, it.ratio,  it.isDefault == 1, it.value)
            if (newestMap.containsKey(it.eventType)) {
                assert(newestMap[it.eventType] != null)
                newestMap[it.eventType]?.add(sm)
            } else {
                newestMap[it.eventType] = mutableListOf(sm)
            }
        }
        if(newestMap.isNotEmpty()) {
            mUnitMap.clear()
            mUnitMap.putAll(newestMap)
        }

    }

    fun getDietUnitList(): MutableList<SpecificationModel> = mUnitMap[EVENT_TYPE_DIET] ?: arrayListOf(
        SpecificationModel(getContext().getString(R.string.unit_g), 1.0, true, 0),
        SpecificationModel(getContext().getString(R.string.unit_kg), ratio = 1000.0, false, 1),
        SpecificationModel(getContext().getString(R.string.unit_ml), ratio = 1.0, false, 2),
        SpecificationModel(getContext().getString(R.string.unit_l), ratio = 1000.0, false, 3),
    )
    fun getDietUnit(unit: Int): String {
        var default: SpecificationModel? = null
        return getDietUnitList().find {
            if (it.isDefault) default = it
            unit == it.code
        }?.specification ?: ""
    }


    fun getMedicationUnitList(): MutableList<SpecificationModel> = mUnitMap[EVENT_TYPE_MEDICINE] ?: arrayListOf(
        SpecificationModel(getContext().getString(R.string.unit_mg), ratio = 1.0, true, 0),
        SpecificationModel(getContext().getString(R.string.unit_g), ratio = 1000.0, false, 1),
        SpecificationModel(getContext().getString(R.string.unit_piece), ratio = 1.0, false, 2),
        SpecificationModel(getContext().getString(R.string.unit_capsule), ratio = 1.0, false, 3),
    )

    fun getMedicationUnit(unit: Int): String {
        var default: SpecificationModel? = null
        return getMedicationUnitList().find {
            if (it.isDefault) default = it
            unit == it.code
        }?.specification ?: ""
    }

    fun getTimeUnitList(): MutableList<SpecificationModel> = mUnitMap[EVENT_TYPE_EXERCISE] ?: arrayListOf(
        SpecificationModel(getContext().getString(R.string.min), ratio = 1.0, true, 0),
        SpecificationModel(getContext().getString(R.string.unit_hour), ratio = 60.0, false, 1),
    )

    fun getTimeUnit(unit: Int): String {
        var default: SpecificationModel? = null
        return getTimeUnitList().find {
            if (it.isDefault) default = it
            unit == it.code
        }?.specification ?: ""
    }


    fun getInsulinUnit(): MutableList<SpecificationModel> = mUnitMap[EVENT_TYPE_INSULIN] ?: arrayListOf(
        SpecificationModel("U", ratio = 1.0, true, 0),
    )
}