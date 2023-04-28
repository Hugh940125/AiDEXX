package com.microtech.aidexx.db.entity

import android.content.res.Resources
import com.microtech.aidexx.R
import com.microtech.aidexx.common.getContext
import com.microtech.aidexx.utils.LanguageUnitManager
import io.objectbox.annotation.*
import java.util.*
import kotlin.jvm.Transient


@Entity
class DietEntity : EventEntity {

    @Id
    override var idx: Long? = null

    @Index
    override var state: Int = 0
    override var id: String? = null

    override var createTime: Date = Date()

    @Index
    override var recordIndex: Long? = null

    @Index
    override var deleteStatus: Int = 0

    @Index
    var mealTime: Date = Date()

    @Index(type = IndexType.HASH)
    var recordUuid: String? = UUID.randomUUID().toString().replace("-", "")
    var mealRemark: String? = null
    var carbohydrate: Int? = null

    var isPreset: Boolean = false

    override var recordId: String? = null

    @Convert(converter = DietDetailEntity::class, dbType = String::class)
    var relList: MutableList<DietDetailEntity> = ArrayList()
    var momentType: Int = 0

    @Index(type = IndexType.HASH)
    override var authorizationId: String? = null

    @Transient
    override var time: Date = mealTime
        get() {
            return mealTime
        }
        set(time) {
            field = time
            mealTime = time
        }

    override var language: String = ""

    constructor() {
        this.language = LanguageUnitManager.getCurrentLanguageCode()
    }

    constructor(mealRemark: String? = null, carbohydrate: Int? = null) : this() {
        this.mealRemark = mealRemark
        this.carbohydrate = carbohydrate
    }

    override fun getEventDescription(res: Resources): String {
        var description = ""
        if (relList.isNullOrEmpty()) {
            description =
                mealRemark + "(${carbohydrate}克)"
        } else {
            val timeSlot = getTypeText()
            if (!timeSlot.isNullOrBlank()) {
                description = "$timeSlot："
            }
            val foods = mutableListOf<String>()
            var fat = 0.0
            var protein = 0.0
            var carbohydrate = 0.0

            relList.forEach { dietDetailEntity ->
                fat += dietDetailEntity.fat
                protein += dietDetailEntity.protein
                carbohydrate += dietDetailEntity.carbohydrate
                foods.add(
                    dietDetailEntity.getEventDesc()
                )
            }
            description += foods.joinToString(";")
        }
        return description
    }

    override fun getValueDescription(res: Resources): String {
        return ""
    }

    fun getEventValue(res: Resources): String {
        return if (relList.isEmpty()) {
            carbohydrate.toString()
        } else {
            ""
        }

    }

    override fun toString(): String {
        return "DietEntity(idx=$idx, state=$state, id=$id, recordIndex=$recordIndex, deleteStatus=$deleteStatus, mealTime=$mealTime, recordUuid=$recordUuid, mealRemark=$mealRemark, carbohydrate=$carbohydrate, isPreset=$isPreset, relList=$relList, authorizationId=$authorizationId, time=$time)"
    }


    fun getTypeText(): String {
        return when (momentType) {
            1 -> return getContext().getString(R.string.breakfast)
            2 -> return getContext().getString(R.string.lunch)
            3 -> return getContext().getString(R.string.dinner)
            4 -> return getContext().getString(R.string.extra_meal)
            else -> ""
        }
    }

}