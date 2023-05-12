package com.microtech.aidexx.db.entity

import android.content.res.Resources
import com.microtech.aidexx.R
import com.microtech.aidexx.common.getContext
import com.microtech.aidexx.utils.LanguageUnitManager
import io.objectbox.annotation.*
import java.util.*
import kotlin.jvm.Transient


@Entity
class InsulinEntity : EventEntity {
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

    var injectionTime: Date = Date()
    var insulinName: String? = null

    @Index(type = IndexType.HASH)
    var recordUuid: String? = UUID.randomUUID().toString().replace("-", "")
    var insulinDosage: Float? = null
    var isPreset: Boolean = false

    @Index(type = IndexType.HASH)
    override var userId: String? = null

    @Convert(converter = InsulinDetailEntity::class, dbType = String::class)
    var relList: MutableList<InsulinDetailEntity> = ArrayList()
    var momentType: Int = 0

    @Transient
    override var time: Date = injectionTime
        get() {
            return injectionTime
        }
        set(time) {
            field = time
            injectionTime = time
        }

    override var recordId: String? = null

    override var language: String = ""
    override var uploadState: Int = 0

    constructor() {
        this.language = LanguageUnitManager.getCurrentLanguageCode()
    }

    constructor(insulinName: String? = null, insulinDosage: Float? = null) : this() {
        this.insulinName = insulinName
        this.insulinDosage = insulinDosage
    }


    override fun getEventDescription(res: Resources): String {
        var description = ""
        if (relList.isNullOrEmpty()) {
            description =
                insulinName + "(${insulinDosage}U)"
        } else {
            val injections = mutableListOf<String>()
            val timeSlot = getTypeText()
            if (!timeSlot.isNullOrBlank()) {
                description = "$timeSlot："
            }

            relList?.forEach { insulinDetailEntity ->
                injections.add(
                    insulinDetailEntity.getEventDesc()
                )
            }
            description += injections.joinToString(";")
        }
        return description
    }

    override fun getValueDescription(res: Resources): String = ""


    fun getEventValue(res: Resources): String {
        return insulinDosage.toString()
    }

    override fun toString(): String {
        return "InsulinEntity(idx=$idx, state=$state, id=$id, recordIndex=$recordIndex, deleteStatus=$deleteStatus, injectionTime=$injectionTime, insulinName=$insulinName, recordUuid=$recordUuid, insulinDosage=$insulinDosage, isPreset=$isPreset, authorizationId=$userId, relList=${
            relList.joinToString(
                ","
            )
        }, time=$time)"
    }

    private fun getTypeText(): String {
        return when (momentType) {
            1 -> return getContext().getString(R.string.breakfast)
            2 -> return getContext().getString(R.string.lunch)
            3 -> return getContext().getString(R.string.dinner)
            4 -> return getContext().getString(R.string.before_sleep)
            5 -> return getContext().getString(R.string.makeup)
            else -> ""
        }
    }


}