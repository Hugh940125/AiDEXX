package com.microtech.aidexx.db.repository

import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.dao.EventDao
import com.microtech.aidexx.db.entity.BaseEventEntity
import com.microtech.aidexx.db.entity.event.UnitEntity
import com.microtech.aidexx.db.entity.event.preset.BasePresetEntity
import com.microtech.aidexx.db.entity.event.preset.BaseSysPreset
import com.microtech.aidexx.db.entity.event.preset.DietPresetEntity
import com.microtech.aidexx.db.entity.event.preset.DietSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.DietSysPresetEntity_
import com.microtech.aidexx.db.entity.event.preset.InsulinPresetEntity
import com.microtech.aidexx.db.entity.event.preset.InsulinSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.InsulinSysPresetEntity_
import com.microtech.aidexx.db.entity.event.preset.MedicinePresetEntity
import com.microtech.aidexx.db.entity.event.preset.MedicineSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.MedicineSysPresetEntity_
import com.microtech.aidexx.db.entity.event.preset.SportPresetEntity
import com.microtech.aidexx.db.entity.event.preset.SportSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.SportSysPresetEntity_
import com.microtech.aidexx.utils.LanguageUnitManager
import io.objectbox.Property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.util.Date

object EventDbRepository {

    suspend fun queryEventByPage(
        startDate: Date,
        endDate: Date,
        uid: String = UserInfoManager.instance().userId()
    ) = EventDao.query(startDate, endDate, uid)

    suspend fun insertPresetData(data: List<BasePresetEntity>) {
        if (data.isEmpty()) return
        EventDao.insertPresetData(data)
    }

    suspend fun insertSysPresetData(data: List<BaseSysPreset>) {
        if (data.isEmpty()) return
        EventDao.insertSysPresetData(data)
    }

    suspend fun <T: BaseSysPreset> removeSysPresetOfOtherVersion(exceptVersion: String, clazz: Class<T>) {
        val pro = when (clazz) {
            DietSysPresetEntity::class.java -> DietSysPresetEntity_.version
            SportSysPresetEntity::class.java -> SportSysPresetEntity_.version
            MedicineSysPresetEntity::class.java -> MedicineSysPresetEntity_.version
            InsulinSysPresetEntity::class.java -> InsulinSysPresetEntity_.version
            else -> null
        } ?: return

        EventDao.removeSysPresetOfOtherVersion(exceptVersion, clazz, pro as Property<T>)
    }

    suspend fun <T: BaseSysPreset> removeSysPresetData(clazz: Class<T>) {
        EventDao.removeSysPresetData(clazz)
    }

    suspend fun insertPresetData(data: BasePresetEntity) =
        EventDao.insertSysPresetData(data)

    suspend inline fun <reified T: BasePresetEntity> findMaxPresetId(): Long? = EventDao.findMaxPresetId<T>()
    suspend inline fun <reified T: BasePresetEntity> findMinPresetId(): Long? = EventDao.findMinPresetId<T>()

    suspend inline fun <reified T: BaseEventEntity> findMaxEventId(): Long? = EventDao.findMaxEventId<T>()
    suspend inline fun <reified T: BaseEventEntity> findMinEventId(): Long? = EventDao.findMinEventId<T>()

    suspend fun queryDietPresetByName(
        name: String,
        userId: String = UserInfoManager.instance().userId(),
        language: String = LanguageUnitManager.getCurrentLanguageCode()
    ): List<DietPresetEntity> = withContext(Dispatchers.IO) {

        listOf(
            async {
                EventDao.queryDietSysPresetByName(name, language) ?: mutableListOf<DietPresetEntity>()
            },
            async {
                EventDao.queryDietUsrPresetByName(name, userId) ?: mutableListOf()
            }
        ).awaitAll().flatten()

    }

    suspend fun queryMedicinePresetByName(
        name: String,
        userId: String = UserInfoManager.instance().userId(),
        language: String = LanguageUnitManager.getCurrentLanguageCode()
    ): List<MedicinePresetEntity> = withContext(Dispatchers.IO) {

        listOf(
            async {
                EventDao.queryMedicineSysPresetByName(name, language) ?: mutableListOf<MedicinePresetEntity>()
            },
            async {
                EventDao.queryMedicineUsrPresetByName(name, userId) ?: mutableListOf()
            }
        ).awaitAll().flatten()

    }

    suspend fun queryInsulinPresetByName(
        name: String,
        userId: String = UserInfoManager.instance().userId(),
        language: String = LanguageUnitManager.getCurrentLanguageCode()
    ): List<InsulinPresetEntity> = withContext(Dispatchers.IO) {

        listOf(
            async {
                EventDao.queryInsulinSysPresetByName(name, language) ?: mutableListOf<InsulinPresetEntity>()
            },
            async {
                EventDao.queryInsulinUsrPresetByName(name, userId) ?: mutableListOf()
            }
        ).awaitAll().flatten()

    }

    suspend fun querySportPresetByName(
        name: String,
        userId: String = UserInfoManager.instance().userId(),
        language: String = LanguageUnitManager.getCurrentLanguageCode()
    ): List<SportPresetEntity> = withContext(Dispatchers.IO) {

        listOf(
            async {
                EventDao.querySportSysPresetByName(name, language) ?: mutableListOf<SportPresetEntity>()
            },
            async {
                EventDao.querySportUsrPresetByName(name, userId) ?: mutableListOf()
            }
        ).awaitAll().flatten()

    }

    suspend inline fun <reified T> queryHistory(timestampProperty: Property<T>) = EventDao.queryLatestHistory(timestampProperty)

    suspend fun insertEvent(event: BaseEventEntity) = EventDao.insertEvent(event)
    suspend fun insertEvents(events: List<BaseEventEntity>) = EventDao.insertEvents(events)

    suspend fun loadUnit(language: String): MutableList<UnitEntity>? = EventDao.loadUnit(language)
    suspend fun insertUnit(data: List<UnitEntity>) = EventDao.insertUnit(data)

    suspend fun getDietNeedUploadEvent(userId: String = UserInfoManager.instance().userId()) =
        EventDao.getDietNeedUploadEvent(userId)
    suspend fun getExerciseNeedUploadEvent(userId: String = UserInfoManager.instance().userId()) =
        EventDao.getExerciseNeedUploadEvent(userId)
    suspend fun getMedicineNeedUploadEvent(userId: String = UserInfoManager.instance().userId()) =
        EventDao.getMedicineNeedUploadEvent(userId)
    suspend fun getInsulinNeedUploadEvent(userId: String = UserInfoManager.instance().userId()) =
        EventDao.getInsulinNeedUploadEvent(userId)
    suspend fun getOthersNeedUploadEvent(userId: String = UserInfoManager.instance().userId()) =
        EventDao.getOthersNeedUploadEvent(userId)

    suspend fun getDietNeedUploadPreset() = EventDao.getDietNeedUploadPreset()
    suspend fun getExerciseNeedUploadPreset() = EventDao.getExerciseNeedUploadPreset()
    suspend fun getMedicineNeedUploadPreset() = EventDao.getMedicineNeedUploadPreset()
    suspend fun getInsulinNeedUploadPreset() = EventDao.getInsulinNeedUploadPreset()

}