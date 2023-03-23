package com.microtech.aidexx.db.entity

import com.microtech.aidexx.common.getMutableListType
import com.microtech.aidexx.common.stripTrailingZeros
import java.lang.reflect.Type
import java.util.*


data class DietDetailEntity(
    var foodPresetId: Long? = null,
    var categoryName: String = "",
    var quantity: Double = 0.0,
    var unit: Int = 0,
    var protein: Double = 0.0,
    var fat: Double = 0.0,
    var carbohydrate: Double = 0.0,
    var createTime: Date? = Date(),
) : BaseEventDetailEntity() {

    override fun toString(): String {
        return "DietDetailEntity(id=$id, foodPresetId=$foodPresetId, type=$presetType, name='$name', categoryName='$categoryName', quantity=$quantity, unit=$unit, protein=$protein, fat=$fat, carbohydrate=$carbohydrate)"
    }

    override fun getEventDesc(spliter: String?): String {
        return "$name(${quantity.stripTrailingZeros()}${unitStr})"
    }

    override fun getCurrClassMutableListType(): Type = getMutableListType<DietDetailEntity>()


}
