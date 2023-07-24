package com.microtech.aidexx.data

import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.repository.EventRepository
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.entity.event.preset.BasePresetEntity
import com.microtech.aidexx.db.entity.event.preset.DietUsrPresetEntity
import com.microtech.aidexx.db.entity.event.preset.InsulinUsrPresetEntity
import com.microtech.aidexx.db.entity.event.preset.MedicineUsrPresetEntity
import com.microtech.aidexx.db.entity.event.preset.SportUsrPresetEntity
import com.microtech.aidexx.db.repository.EventDbRepository
import com.microtech.aidexx.utils.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.lang.reflect.ParameterizedType

abstract class EventPresetSync<T : BasePresetEntity> {

    private val tClazz =
        (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>

    private val limitController = SyncFrequencyLimitController()
    private fun canSync(xLogInfo: String = ""): Boolean {
        var ret = UserInfoManager.instance().isLogin()
        if (!ret) {
            LogUtil.xLogE("$xLogInfo 未登录，${tClazz.simpleName} 停止")
            return false
        }
        return true
    }


    private suspend fun saveOrUpload(data: List<T>): MutableList<T>? {
        if (!limitController.canDo("上传-${tClazz.simpleName}")) return null
        return when (val apiResult = EventRepository.saveOrUpdateRecords(data)) {
            is ApiResult.Success -> {
                limitController.resetNextDoTime()
                apiResult.result.data as MutableList<T>
            }
            is ApiResult.Failure -> null
        }
    }



    open suspend fun upload() {
        if (!canSync("上传数据")) return
        val needUploadData = getNeedUploadData()
        needUploadData?.ifEmpty { null }?.let {
            saveOrUpload(it)?.let { rspData ->
                updateEventDataAfterUpload(it, rspData)
            }
        }
    }

    abstract suspend fun getNeedUploadData(): MutableList<T>?

    private suspend fun updateEventDataAfterUpload(
        origin: MutableList<T>,
        responseList: MutableList<T>
    ) {
        for (entity in origin) {
            if (entity.getServerPresetId() == null) {
                responseList.find {
                    entity == it
                }?.let {
                    entity.setServerPresetId(it.getServerPresetId())
                    responseList.remove(it)
                }
            }
        }
        EventDbRepository.insertPresetData(origin)
    }

    companion object {

        private val dietPresetSync = object: EventPresetSync<DietUsrPresetEntity>() {
            override suspend fun getNeedUploadData(): MutableList<DietUsrPresetEntity>? =
                EventDbRepository.getDietNeedUploadPreset()
        }
        private val exercisePresetSync = object: EventPresetSync<SportUsrPresetEntity>() {
            override suspend fun getNeedUploadData(): MutableList<SportUsrPresetEntity>? =
                EventDbRepository.getExerciseNeedUploadPreset()
        }
        private val medicinePresetSync = object: EventPresetSync<MedicineUsrPresetEntity>() {
            override suspend fun getNeedUploadData(): MutableList<MedicineUsrPresetEntity>? =
                EventDbRepository.getMedicineNeedUploadPreset()
        }
        private val insulinPresetSync = object: EventPresetSync<InsulinUsrPresetEntity>() {
            override suspend fun getNeedUploadData(): MutableList<InsulinUsrPresetEntity>? =
                EventDbRepository.getInsulinNeedUploadPreset()
        }

        private var isSyncing = false

        suspend fun uploadPreset() {
            withContext(Dispatchers.IO) {

                if (isSyncing) {
                    LogUtil.xLogE("用户预设正在同步...")
                    return@withContext
                }
                isSyncing = true
                val tasks = listOf(
                    async { dietPresetSync.upload() },
                    async { exercisePresetSync.upload() },
                    async { medicinePresetSync.upload() },
                    async { insulinPresetSync.upload() },
                )
                tasks.awaitAll()
                isSyncing = false
            }

        }

    }

}