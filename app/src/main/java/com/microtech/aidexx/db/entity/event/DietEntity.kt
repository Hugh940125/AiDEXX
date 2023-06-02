package com.microtech.aidexx.db.entity.event

import android.content.res.Resources
import com.microtech.aidexx.R
import com.microtech.aidexx.common.formatWithoutZone
import com.microtech.aidexx.common.getContext
import com.microtech.aidexx.common.getMutableListType
import com.microtech.aidexx.common.stripTrailingZeros
import com.microtech.aidexx.db.entity.BaseEventEntity
import com.microtech.aidexx.utils.LanguageUnitManager
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Index
import io.objectbox.annotation.IndexType
import java.lang.reflect.Type
import java.util.Date
import java.util.TimeZone
import java.util.UUID


@Entity
class DietEntity : BaseEventEntity {

    @Index
    var mealTime: Date = Date()
        set(value) {
            field = value
            appTime = value.formatWithoutZone() // yyyy-MM-dd HH:mm:ss
            appTimeZone = TimeZone.getDefault().id //
            dstOffset = TimeZone.getDefault().dstSavings //
        }

    @Index(type = IndexType.HASH)
    var foodId: String? = UUID.randomUUID().toString().replace("-", "")
    var mealRemark: String? = null
    var carbohydrate: Int? = null

    var isPreset: Boolean = false

    @Convert(converter = DietDetail::class, dbType = String::class)
    var expandList: MutableList<DietDetail> = ArrayList()
    var momentType: Int = 0

    constructor() {
        this.language = LanguageUnitManager.getCurrentLanguageCode()
    }

    constructor(mealRemark: String? = null, carbohydrate: Int? = null) : this() {
        this.mealRemark = mealRemark
        this.carbohydrate = carbohydrate
    }

    override fun getEventDescription(res: Resources): String {
        var description = ""
        if (expandList.isNullOrEmpty()) {
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

            expandList.forEach { dietDetailEntity ->
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
        return if (expandList.isEmpty()) {
            carbohydrate.toString()
        } else {
            ""
        }

    }

    override fun toString(): String {
        return "DietEntity(idx=$idx, state=$state, id=$id, recordIndex=$recordIndex, deleteStatus=$deleteStatus, mealTime=$mealTime, foodId=$foodId, mealRemark=$mealRemark, carbohydrate=$carbohydrate, isPreset=$isPreset, relList=$expandList, authorizationId=$userId, timestamp=$timestamp)"
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

    override fun hashCode(): Int {
        return foodId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other?.let {
            it is DietEntity && it.foodId == this.foodId
        } ?: false
    }

}


data class DietDetail(
    var foodPresetId: Long? = null,
    var categoryName: String = "",
    var quantity: Double = 0.0,
    var unit: Int = 0,
    var protein: Double = 0.0,
    var fat: Double = 0.0,
    var carbohydrate: Double = 0.0,
    var foodId: String? = null
) : BaseEventDetail() {

    override fun toString(): String {
        return "DietDetailEntity(id=$id, foodPresetId=$foodPresetId, type=$presetType, name='$name', categoryName='$categoryName', quantity=$quantity, unit=$unit, protein=$protein, fat=$fat, carbohydrate=$carbohydrate)"
    }

    override fun getEventDesc(spliter: String?): String {
        return "$name(${quantity.stripTrailingZeros()}${unitStr})"
    }

    override fun getCurrClassMutableListType(): Type = getMutableListType<DietDetail>()


}