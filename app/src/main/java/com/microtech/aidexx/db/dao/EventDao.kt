package com.microtech.aidexx.db.dao

import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.ObjectBox.awaitCallInTx
import com.microtech.aidexx.db.entity.BaseEventEntity
import com.microtech.aidexx.db.entity.BloodGlucoseEntity
import com.microtech.aidexx.db.entity.BloodGlucoseEntity_
import com.microtech.aidexx.db.entity.CalibrateEntity
import com.microtech.aidexx.db.entity.CalibrateEntity_
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity_
import com.microtech.aidexx.db.entity.event.DietEntity
import com.microtech.aidexx.db.entity.event.DietEntity_
import com.microtech.aidexx.db.entity.event.ExerciseEntity
import com.microtech.aidexx.db.entity.event.ExerciseEntity_
import com.microtech.aidexx.db.entity.event.InsulinEntity
import com.microtech.aidexx.db.entity.event.InsulinEntity_
import com.microtech.aidexx.db.entity.event.MedicationEntity
import com.microtech.aidexx.db.entity.event.MedicationEntity_
import com.microtech.aidexx.db.entity.event.OthersEntity
import com.microtech.aidexx.db.entity.event.OthersEntity_
import com.microtech.aidexx.db.entity.event.UnitEntity
import com.microtech.aidexx.db.entity.event.UnitEntity_
import com.microtech.aidexx.db.entity.event.preset.BasePresetEntity
import com.microtech.aidexx.db.entity.event.preset.BaseSysPreset
import com.microtech.aidexx.db.entity.event.preset.DietSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.DietSysPresetEntity_
import com.microtech.aidexx.db.entity.event.preset.DietUsrPresetEntity
import com.microtech.aidexx.db.entity.event.preset.DietUsrPresetEntity_
import com.microtech.aidexx.db.entity.event.preset.InsulinSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.InsulinSysPresetEntity_
import com.microtech.aidexx.db.entity.event.preset.InsulinUsrPresetEntity
import com.microtech.aidexx.db.entity.event.preset.InsulinUsrPresetEntity_
import com.microtech.aidexx.db.entity.event.preset.MedicineSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.MedicineSysPresetEntity_
import com.microtech.aidexx.db.entity.event.preset.MedicineUsrPresetEntity
import com.microtech.aidexx.db.entity.event.preset.MedicineUsrPresetEntity_
import com.microtech.aidexx.db.entity.event.preset.SportSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.SportSysPresetEntity_
import com.microtech.aidexx.db.entity.event.preset.SportUsrPresetEntity
import com.microtech.aidexx.db.entity.event.preset.SportUsrPresetEntity_
import io.objectbox.Property
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.query
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.util.Date

object EventDao {

    private val dietSysPresetBox by lazy { ObjectBox.store.boxFor<DietSysPresetEntity>() }
    private val dietUsrPresetBox by lazy { ObjectBox.store.boxFor<DietUsrPresetEntity>() }
    private val sportSysPresetBox by lazy { ObjectBox.store.boxFor<SportSysPresetEntity>() }
    private val sportUsrPresetBox by lazy { ObjectBox.store.boxFor<SportUsrPresetEntity>() }
    private val medicineSysPresetBox by lazy { ObjectBox.store.boxFor<MedicineSysPresetEntity>() }
    private val medicineUsrPresetBox by lazy { ObjectBox.store.boxFor<MedicineUsrPresetEntity>() }
    private val insulinSysPresetBox by lazy { ObjectBox.store.boxFor<InsulinSysPresetEntity>() }
    private val insulinUsrPresetBox by lazy { ObjectBox.store.boxFor<InsulinUsrPresetEntity>() }

    private val insulinBox by lazy { ObjectBox.store.boxFor<InsulinEntity>() }
    private val medicineBox by lazy { ObjectBox.store.boxFor<MedicationEntity>() }
    private val sportBox by lazy { ObjectBox.store.boxFor<ExerciseEntity>() }
    private val dietBox by lazy { ObjectBox.store.boxFor<DietEntity>() }
    private val otherBox by lazy { ObjectBox.store.boxFor<OthersEntity>() }
    private val cgmBox by lazy { ObjectBox.store.boxFor<RealCgmHistoryEntity>() }
    private val bgBox by lazy { ObjectBox.store.boxFor<BloodGlucoseEntity>() }
    private val calBox by lazy { ObjectBox.store.boxFor<CalibrateEntity>() }

    private val unitBox by lazy { ObjectBox.store.boxFor<UnitEntity>() }

    /**
     * 查 某用户 一段时间内 有效的 的数据
     */
    suspend fun query(
        startDate: Date,
        endDate: Date,
        userId: String
    ): List<BaseEventEntity> =

        withContext(Dispatchers.IO) {
            val events = listOf(
                async {
                    dietBox.query {
                        between(DietEntity_.timestamp, startDate.time, endDate.time)
                        equal(DietEntity_.userId, userId, QueryBuilder.StringOrder.CASE_SENSITIVE)
                        equal(DietEntity_.deleteStatus, 0)
                    }.find()
                },
                async {
                    sportBox.query {
                        between(ExerciseEntity_.timestamp, startDate.time, endDate.time)
                        equal(ExerciseEntity_.userId, userId, QueryBuilder.StringOrder.CASE_SENSITIVE)
                        equal(ExerciseEntity_.deleteStatus, 0)
                    }.find()
                },
                async {
                    medicineBox.query {
                        between(MedicationEntity_.timestamp, startDate.time, endDate.time)
                        equal(MedicationEntity_.userId, userId, QueryBuilder.StringOrder.CASE_SENSITIVE)
                        equal(MedicationEntity_.deleteStatus, 0)
                    }.find()
                },
                async {
                    insulinBox.query {
                        between(InsulinEntity_.timestamp, startDate.time, endDate.time)
                        equal(InsulinEntity_.userId, userId, QueryBuilder.StringOrder.CASE_SENSITIVE)
                        equal(InsulinEntity_.deleteStatus, 0)
                    }.find()
                },
                async {
                    otherBox.query {
                        between(OthersEntity_.timestamp, startDate.time, endDate.time)
                        equal(OthersEntity_.userId, userId, QueryBuilder.StringOrder.CASE_SENSITIVE)
                        equal(OthersEntity_.deleteStatus, 0)
                    }.find()
                },
            )
            events.awaitAll().flatten()
        }


    suspend fun insertPresetData(list: List<BasePresetEntity>) {

        if (list.isEmpty()) return

        awaitCallInTx {
            ObjectBox.store.boxFor(list.first().javaClass).put(list)
        }
    }

    suspend fun insertSysPresetData(list: List<BaseSysPreset>) {

        if (list.isEmpty()) return

        awaitCallInTx {
            ObjectBox.store.boxFor(list.first().javaClass).put(list)
        }
    }

    suspend fun insertSysPresetData(entity: BasePresetEntity) =
        awaitCallInTx {
            ObjectBox.store.boxFor(entity.javaClass).put(entity)
        }

    suspend fun <T: BaseSysPreset> removeSysPresetOfOtherVersion(exceptVersion: String, clazz: Class<T>, property: Property<T>) =
        awaitCallInTx {
            ObjectBox.store.boxFor(clazz).query {
                notEqual(property, exceptVersion, QueryBuilder.StringOrder.CASE_SENSITIVE)
            }.remove()
        }

    suspend fun <T> removeSysPresetData(type: Class<T>) =
        awaitCallInTx {
            ObjectBox.store.boxFor(type).removeAll()
        }

    fun getPresetIdPropertyByClazz(clazz: Class<*>): Property<out BasePresetEntity>? {
        return when (clazz) {
            DietSysPresetEntity::class.java -> DietSysPresetEntity_.foodSysPresetId
            DietUsrPresetEntity::class.java -> DietUsrPresetEntity_.autoIncrementColumn
            SportSysPresetEntity::class.java -> SportSysPresetEntity_.exerciseSysPresetId
            SportUsrPresetEntity::class.java -> SportUsrPresetEntity_.autoIncrementColumn
            MedicineSysPresetEntity::class.java -> MedicineSysPresetEntity_.medicationSysPresetId
            MedicineUsrPresetEntity::class.java -> MedicineUsrPresetEntity_.autoIncrementColumn
            InsulinSysPresetEntity::class.java -> InsulinSysPresetEntity_.insulinSysPresetId
            InsulinUsrPresetEntity::class.java -> InsulinUsrPresetEntity_.autoIncrementColumn
            else -> null
        }
    }

    suspend inline fun <reified T: BasePresetEntity> findMaxPresetId(): Long? {

        val property: Property<out BasePresetEntity>? = getPresetIdPropertyByClazz(T::class.java)
        property?:let {
            return null
        }
        return awaitCallInTx {
            ObjectBox.store.boxFor(T::class.java).query().build().property(property as Property<T>).max()
        }
    }

    suspend inline fun <reified T: BasePresetEntity> findMinPresetId(): Long? {

        val property: Property<out BasePresetEntity>? = getPresetIdPropertyByClazz(T::class.java)
        property?:let {
            return null
        }
        return awaitCallInTx {
            ObjectBox.store.boxFor(T::class.java).query().build().property(property as Property<T>).min()
        }
    }


    fun getEventIdPropertyByClazz(clazz: Class<*>): Property<out BaseEventEntity>? {
        return when (clazz) {
            RealCgmHistoryEntity::class.java -> RealCgmHistoryEntity_.autoIncrementColumn
            BloodGlucoseEntity::class.java -> BloodGlucoseEntity_.autoIncrementColumn
            CalibrateEntity::class.java -> CalibrateEntity_.autoIncrementColumn
            DietEntity::class.java -> DietEntity_.autoIncrementColumn
            MedicationEntity::class.java -> MedicationEntity_.autoIncrementColumn
            InsulinEntity::class.java -> InsulinEntity_.autoIncrementColumn
            ExerciseEntity::class.java -> ExerciseEntity_.autoIncrementColumn
            OthersEntity::class.java -> OthersEntity_.autoIncrementColumn
            else -> null
        }
    }
    fun getUserIdPropertyByClazz(clazz: Class<*>): Property<out BaseEventEntity>? {
        return when (clazz) {
            RealCgmHistoryEntity::class.java -> RealCgmHistoryEntity_.userId
            BloodGlucoseEntity::class.java -> BloodGlucoseEntity_.userId
            CalibrateEntity::class.java -> CalibrateEntity_.userId
            DietEntity::class.java -> DietEntity_.userId
            MedicationEntity::class.java -> MedicationEntity_.userId
            InsulinEntity::class.java -> InsulinEntity_.userId
            ExerciseEntity::class.java -> ExerciseEntity_.userId
            OthersEntity::class.java -> OthersEntity_.userId
            else -> null
        }
    }
    suspend inline fun <reified T: BaseEventEntity> findMaxEventId(userId: String): Long? {

        val property: Property<out BaseEventEntity>? = getEventIdPropertyByClazz(T::class.java)
        val userIdProperty: Property<out BaseEventEntity>? = getUserIdPropertyByClazz(T::class.java)
        property ?: return null
        userIdProperty ?: return null
        return awaitCallInTx {
            ObjectBox.store.boxFor(T::class.java).query{
                equal(userIdProperty as Property<T>, userId, QueryBuilder.StringOrder.CASE_INSENSITIVE)
            }.property(property as Property<T>).max()
        }
    }
    suspend fun <T: BaseEventEntity> findMaxEventId(userId: String, clazz: Class<T>): Long? {
        val property: Property<out BaseEventEntity>? = getEventIdPropertyByClazz(clazz)
        val userIdProperty: Property<out BaseEventEntity>? = getUserIdPropertyByClazz(clazz)
        property ?: return null
        userIdProperty ?: return null
        return awaitCallInTx {
            ObjectBox.store.boxFor(clazz).query{
                equal(userIdProperty as Property<T>, userId, QueryBuilder.StringOrder.CASE_INSENSITIVE)
            }.property(property as Property<T>).max()
        }
    }

    suspend inline fun <reified T: BaseEventEntity> findMinEventId(): Long? {

        val property: Property<out BaseEventEntity>? = getEventIdPropertyByClazz(T::class.java)
        property?:let {
            return null
        }
        return awaitCallInTx {
            ObjectBox.store.boxFor(T::class.java).query().build().property(property as Property<T>).min()
        }
    }

    fun <T> getDeleteStatusProperty(clazz: Class<T>): Property<T>? {
        return when (clazz) {
            BloodGlucoseEntity::class.java -> BloodGlucoseEntity_.deleteStatus as Property<T>
            DietEntity::class.java -> DietEntity_.deleteStatus as Property<T>
            ExerciseEntity::class.java -> ExerciseEntity_.deleteStatus as Property<T>
            MedicationEntity::class.java -> MedicationEntity_.deleteStatus as Property<T>
            InsulinEntity::class.java -> InsulinEntity_.deleteStatus as Property<T>
            OthersEntity::class.java -> OthersEntity_.deleteStatus as Property<T>
            else -> null
        }
    }
    suspend inline fun <reified T> queryLatestHistory(timestampProperty: Property<T>, limit: Long = 15): MutableList<T>? =
        awaitCallInTx {
            getDeleteStatusProperty(T::class.java)?.let {
                ObjectBox.store.boxFor(T::class.java).query {
                    equal(it, 0)
                    orderDesc(timestampProperty)
                }.find(0, limit)
            }

        }

    suspend fun queryDietSysPresetByName(
        name: String,
        language: String,
        sysPresetVersion: String
    ): MutableList<DietSysPresetEntity>? =
        awaitCallInTx {
            dietSysPresetBox.query {
                contains(DietSysPresetEntity_.name, name, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                equal(DietSysPresetEntity_.deleteFlag, 0)
                equal(DietSysPresetEntity_.language, language, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                equal(DietSysPresetEntity_.version, sysPresetVersion, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                order(DietSysPresetEntity_.name)
            }.find()
        }

    suspend fun queryDietUsrPresetByName(
        name: String,
        userId: String,
    ): MutableList<DietUsrPresetEntity>? =
        awaitCallInTx {
            dietUsrPresetBox.query {
                contains(DietUsrPresetEntity_.name, name, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                equal(DietUsrPresetEntity_.userId, userId, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                equal(DietUsrPresetEntity_.deleteFlag, 0)
                order(DietUsrPresetEntity_.name)
            }.find()
        }

    suspend fun queryMedicineSysPresetByName(
        name: String,
        language: String,
        sysPresetVersion: String
    ): MutableList<MedicineSysPresetEntity>? =
        awaitCallInTx {
            medicineSysPresetBox.query {
                contains(MedicineSysPresetEntity_.name, name, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                equal(MedicineSysPresetEntity_.deleteFlag, 0)
                equal(MedicineSysPresetEntity_.language, language, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                equal(MedicineSysPresetEntity_.version, sysPresetVersion, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                order(MedicineSysPresetEntity_.name)
            }.find()
        }

    suspend fun queryMedicineUsrPresetByName(
        name: String,
        userId: String,
    ): MutableList<MedicineUsrPresetEntity>? =
        awaitCallInTx {
            medicineUsrPresetBox.query {
                contains(MedicineUsrPresetEntity_.name, name, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                equal(MedicineUsrPresetEntity_.userId, userId, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                equal(MedicineUsrPresetEntity_.deleteFlag, 0)
                order(MedicineUsrPresetEntity_.name)
            }.find()
        }

    suspend fun queryInsulinSysPresetByName(
        name: String,
        language: String,
        sysPresetVersion: String
    ): MutableList<InsulinSysPresetEntity>? =
        awaitCallInTx {
            insulinSysPresetBox.query {
                contains(InsulinSysPresetEntity_.name, name, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                equal(InsulinSysPresetEntity_.deleteFlag, 0)
                equal(InsulinSysPresetEntity_.language, language, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                equal(InsulinSysPresetEntity_.version, sysPresetVersion, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                order(InsulinSysPresetEntity_.name)
            }.find()
        }

    suspend fun queryInsulinUsrPresetByName(
        name: String,
        userId: String,
    ): MutableList<InsulinUsrPresetEntity>? =
        awaitCallInTx {
            insulinUsrPresetBox.query {
                contains(InsulinUsrPresetEntity_.name, name, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                equal(InsulinUsrPresetEntity_.userId, userId, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                equal(InsulinUsrPresetEntity_.deleteFlag, 0)
                order(InsulinUsrPresetEntity_.name)
            }.find()
        }

    suspend fun querySportSysPresetByName(
        name: String,
        language: String,
        sysPresetVersion: String
    ): MutableList<SportSysPresetEntity>? =
        awaitCallInTx {
            sportSysPresetBox.query {
                contains(SportSysPresetEntity_.name, name, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                equal(SportSysPresetEntity_.deleteFlag, 0)
                equal(SportSysPresetEntity_.language, language, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                equal(SportSysPresetEntity_.version, sysPresetVersion, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                order(SportSysPresetEntity_.name)
            }.find()
        }

    suspend fun querySportUsrPresetByName(
        name: String,
        userId: String,
    ): MutableList<SportUsrPresetEntity>? =
        awaitCallInTx {
            sportUsrPresetBox.query {
                contains(SportUsrPresetEntity_.name, name, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                equal(SportUsrPresetEntity_.userId, userId, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                equal(SportUsrPresetEntity_.deleteFlag, 0)
                order(SportUsrPresetEntity_.name)
            }.find()
        }


    suspend fun insertEvent(event: BaseEventEntity) =
        awaitCallInTx {
            ObjectBox.store.boxFor(event.javaClass).put(event)
        }

    suspend fun insertEvents(events: List<BaseEventEntity>) =
        awaitCallInTx {
            ObjectBox.store.boxFor(events.first().javaClass).put(events)
        }


    suspend fun loadUnit(language: String): MutableList<UnitEntity>? =
        awaitCallInTx {
            unitBox.query {
                equal(UnitEntity_.language, language, QueryBuilder.StringOrder.CASE_INSENSITIVE)
            }.find()
        }

    suspend fun insertUnit(data: List<UnitEntity>) = awaitCallInTx {
        unitBox.put(data)
    }

    suspend fun removeUnitOfOtherVersion(exceptVersion: String) = awaitCallInTx {
        unitBox.query {
            notEqual(UnitEntity_.version, exceptVersion, QueryBuilder.StringOrder.CASE_INSENSITIVE)
        }.remove()
    }

    suspend fun removeAllUnit() = awaitCallInTx {
        unitBox.removeAll()
    }

    suspend fun getDietNeedUploadEvent(userId: String): MutableList<DietEntity>? {
        return awaitCallInTx {
            dietBox.query { 
                notEqual(DietEntity_.uploadState, 2)
                equal(DietEntity_.deleteStatus, 0)
                equal(DietEntity_.userId, userId, QueryBuilder.StringOrder.CASE_SENSITIVE)
                order(DietEntity_.idx)
            }.find()
        }
    }
    suspend fun getExerciseNeedUploadEvent(userId: String): MutableList<ExerciseEntity>? {
        return awaitCallInTx {
            sportBox.query {
                notEqual(ExerciseEntity_.uploadState, 2)
                equal(ExerciseEntity_.deleteStatus, 0)
                equal(ExerciseEntity_.userId, userId, QueryBuilder.StringOrder.CASE_SENSITIVE)
                order(ExerciseEntity_.idx)
            }.find()
        }
    }
    suspend fun getMedicineNeedUploadEvent(userId: String): MutableList<MedicationEntity>? {
        return awaitCallInTx {
            medicineBox.query {
                notEqual(MedicationEntity_.uploadState, 2)
                equal(MedicationEntity_.deleteStatus, 0)
                equal(MedicationEntity_.userId, userId, QueryBuilder.StringOrder.CASE_SENSITIVE)
                order(MedicationEntity_.idx)
            }.find()
        }
    }
    suspend fun getInsulinNeedUploadEvent(userId: String): MutableList<InsulinEntity>? {
        return awaitCallInTx {
            insulinBox.query {
                notEqual(InsulinEntity_.uploadState, 2)
                equal(InsulinEntity_.deleteStatus, 0)
                equal(InsulinEntity_.userId, userId, QueryBuilder.StringOrder.CASE_SENSITIVE)
                order(InsulinEntity_.idx)
            }.find()
        }
    }
    suspend fun getOthersNeedUploadEvent(userId: String): MutableList<OthersEntity>? {
        return awaitCallInTx {
            otherBox.query {
                notEqual(OthersEntity_.uploadState, 2)
                equal(OthersEntity_.deleteStatus, 0)
                equal(OthersEntity_.userId, userId, QueryBuilder.StringOrder.CASE_SENSITIVE)
                order(OthersEntity_.idx)
            }.find()
        }
    }

    suspend fun getDietNeedUploadPreset(): MutableList<DietUsrPresetEntity>? {
        return awaitCallInTx {
            dietUsrPresetBox.query {
                isNull(DietUsrPresetEntity_.autoIncrementColumn)
            }.find()
        }
    }
    suspend fun getExerciseNeedUploadPreset(): MutableList<SportUsrPresetEntity>? {
        return awaitCallInTx {
            sportUsrPresetBox.query {
                isNull(SportUsrPresetEntity_.autoIncrementColumn)
            }.find()
        }
    }
    suspend fun getMedicineNeedUploadPreset(): MutableList<MedicineUsrPresetEntity>? {
        return awaitCallInTx {
            medicineUsrPresetBox.query {
                isNull(MedicineUsrPresetEntity_.autoIncrementColumn)
            }.find()
        }
    }
    suspend fun getInsulinNeedUploadPreset(): MutableList<InsulinUsrPresetEntity>? {
        return awaitCallInTx {
            insulinUsrPresetBox.query {
                isNull(InsulinUsrPresetEntity_.autoIncrementColumn)
            }.find()
        }
    }

    private fun <T> getIdProperty(clazz: Class<T>): Property<T>? {
        return when (clazz) {
            RealCgmHistoryEntity::class.java -> RealCgmHistoryEntity_.__ID_PROPERTY as Property<T>
            BloodGlucoseEntity::class.java -> BloodGlucoseEntity_.__ID_PROPERTY as Property<T>
            DietEntity::class.java -> DietEntity_.__ID_PROPERTY as Property<T>
            ExerciseEntity::class.java -> ExerciseEntity_.__ID_PROPERTY as Property<T>
            MedicationEntity::class.java -> MedicationEntity_.__ID_PROPERTY as Property<T>
            InsulinEntity::class.java -> InsulinEntity_.__ID_PROPERTY as Property<T>
            OthersEntity::class.java -> OthersEntity_.__ID_PROPERTY as Property<T>
            else -> null
        }
    }
    suspend fun <T: BaseEventEntity> removeEventById(id: Long, clazz: Class<T>) = awaitCallInTx {
        val box = ObjectBox.store.boxFor(clazz)
        getIdProperty(clazz)?.let {
            val removed = box.query {
                equal(it, id)
            }.findFirst()

            removed?.let {
                if (it.deleteStatus == 0) {
                    it.deleteStatus = 1
                    box.put(it)
                }
                it
            }
        }
    }

    suspend fun <T: BaseEventEntity> removeEventByFrontId(frontId: String, clazz: Class<T>) = awaitCallInTx {
        val box = ObjectBox.store.boxFor(clazz)
        getFrontIdProperty(clazz)?.let {
            val removed = box.query {
                equal(it, frontId, QueryBuilder.StringOrder.CASE_INSENSITIVE)
            }.findFirst()

            removed?.let {
                if (it.deleteStatus == 0) {
                    it.deleteStatus = 1
                    box.put(it)
                }
                it
            }
        }
    }

    suspend fun <T: BaseEventEntity> queryDeletedData(
        userId: String,
        clazz: Class<T>
    ): List<String>? = awaitCallInTx {
        when (clazz) {
            DietEntity::class.java -> {
                dietBox.query {
                    equal(DietEntity_.deleteStatus, 1)
                    equal(DietEntity_.uploadState, 2)
                    equal(DietEntity_.userId, userId, QueryBuilder.StringOrder.CASE_SENSITIVE)
                }.property(DietEntity_.foodId).findStrings().toList()
            }
            ExerciseEntity::class.java -> {
                sportBox.query {
                    equal(ExerciseEntity_.deleteStatus, 1)
                    equal(ExerciseEntity_.uploadState, 2)
                    equal(ExerciseEntity_.userId, userId, QueryBuilder.StringOrder.CASE_SENSITIVE)
                }.property(ExerciseEntity_.exerciseId).findStrings().toList()
            }
            MedicationEntity::class.java -> {
                medicineBox.query {
                    equal(MedicationEntity_.deleteStatus, 1)
                    equal(MedicationEntity_.uploadState, 2)
                    equal(MedicationEntity_.userId, userId, QueryBuilder.StringOrder.CASE_SENSITIVE)
                }.property(MedicationEntity_.medicationId).findStrings().toList()
            }
            InsulinEntity::class.java -> {
                insulinBox.query {
                    equal(InsulinEntity_.deleteStatus, 1)
                    equal(InsulinEntity_.uploadState, 2)
                    equal(InsulinEntity_.userId, userId, QueryBuilder.StringOrder.CASE_SENSITIVE)
                }.property(InsulinEntity_.insulinId).findStrings().toList()
            }
            OthersEntity::class.java -> {
                otherBox.query {
                    equal(OthersEntity_.deleteStatus, 1)
                    equal(OthersEntity_.uploadState, 2)
                    equal(OthersEntity_.userId, userId, QueryBuilder.StringOrder.CASE_SENSITIVE)
                }.property(OthersEntity_.otherId).findStrings().toList()
            }
            BloodGlucoseEntity::class.java -> {
                bgBox.query {
                    equal(BloodGlucoseEntity_.deleteStatus, 1)
                    equal(BloodGlucoseEntity_.uploadState, 2)
                    equal(BloodGlucoseEntity_.userId, userId, QueryBuilder.StringOrder.CASE_SENSITIVE)
                }.property(BloodGlucoseEntity_.bloodGlucoseId).findStrings().toList()
            }
            else -> null
        }
    }


    private fun <T> getFrontIdProperty(clazz: Class<T>): Property<T>? {
        return when (clazz) {
            BloodGlucoseEntity::class.java -> BloodGlucoseEntity_.bloodGlucoseId as Property<T>
            DietEntity::class.java -> DietEntity_.foodId as Property<T>
            ExerciseEntity::class.java -> ExerciseEntity_.exerciseId as Property<T>
            MedicationEntity::class.java -> MedicationEntity_.medicationId as Property<T>
            InsulinEntity::class.java -> InsulinEntity_.insulinId as Property<T>
            OthersEntity::class.java -> OthersEntity_.otherId as Property<T>
            else -> null
        }
    }
    suspend fun <T: BaseEventEntity> updateDeleteStatusByIds(
        ids: List<String>,
        clazz: Class<T>
    ): Boolean? = awaitCallInTx {
        getFrontIdProperty(clazz)?.let {
            ObjectBox.store.boxFor(clazz).query {
                `in`(it, ids.toTypedArray(), QueryBuilder.StringOrder.CASE_SENSITIVE)
            }.find().map {
                it.deleteStatus = 2
                it
            }.ifEmpty { null }?.let {
                ObjectBox.store.boxFor(clazz).put(it)
            }
            true
        }?: false
    }

}