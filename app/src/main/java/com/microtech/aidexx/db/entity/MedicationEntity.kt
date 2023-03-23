package com.microtech.aidexx.db.entity

import android.content.res.Resources
import com.microtech.aidexx.R
import com.microtech.aidexx.common.getContext
import com.microtech.aidexx.common.stripTrailingZeros
import com.microtech.aidexx.utils.LanguageUnitManager
import io.objectbox.annotation.*
import java.util.*
import kotlin.jvm.Transient


@Entity
class MedicationEntity :
    EventEntity {
    override var createTime: Date = Date()

    @Id
    override var idx: Long? = null

    @Index
    override var state: Int = 0
    override var id: String? = null

    @Index
    override var recordIndex: Long? = null

    @Index
    override var deleteStatus: Int = 0

    @Index
    var recordUuid: String? = UUID.randomUUID().toString().replace("-", "")
    var takenTime: Date = Date()

    @Index
    var medicineName: String? = null

//    @Uid(499736593101406176L)
    var medicineDosage: Float? = null
    var isPreset: Boolean = false


    @Convert(converter = MedicationDetailEntity::class, dbType = String::class)
    var relList: MutableList<MedicationDetailEntity> = ArrayList()
    var momentType: Int = 0

    @Index
    override var authorizationId: String? = null

    override var recordId: String? = null

    @Transient
    override var time: Date = takenTime
        get() {
            return takenTime
        }
        set(time) {
            field = time
            takenTime = time
        }
    override var language: String = ""
    constructor() {
        this.language = LanguageUnitManager.getCurrentLanguageCode()
    }
    constructor(medicineName: String? = null, medicineDosage: Float? = null): this() {
        this.medicineName = medicineName
        this.medicineDosage = medicineDosage
    }

    override fun getEventDescription(res: Resources): String {
        var description = ""
        if (relList.isEmpty()) {
            description = "${medicineName}(${
                medicineDosage?.stripTrailingZeros(
                    3
                )
            }mg)"
        } else {
            val medications = mutableListOf<String>()
            val timeSlot = getTypeText()
            if (!timeSlot.isNullOrBlank()) {
                description = "$timeSlot："
            }
            relList?.forEach { exerciseEntity ->
                medications.add(exerciseEntity.getEventDesc())
            }
            description += medications.joinToString(";")
        }
        return description
    }

    override fun getValueDescription(res: Resources): String = ""

    fun getEventValue(res: Resources): String = ""
    fun getTypeText(): String {
        return when (momentType) {
            1 -> return getContext().getString(R.string.breakfast)
            2 -> return getContext().getString(R.string.lunch)
            3 -> return getContext().getString(R.string.dinner)
            4 -> return getContext().getString(R.string.before_sleep)
            else -> ""
        }
    }
}