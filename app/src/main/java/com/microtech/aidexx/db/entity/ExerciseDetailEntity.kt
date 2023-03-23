package com.microtech.aidexx.db.entity

import com.microtech.aidexx.common.getMutableListType
import com.microtech.aidexx.common.stripTrailingZeros
import java.lang.reflect.Type
import java.util.*


data class ExerciseDetailEntity(

    var exec_preset_id: Long? = null,
    var intensity_category_name: String? = null, //强度分类
    var hour_kcal_per_kg: Double = 0.0, // 每小时单位公斤消费千卡数
    var quantity: Double = 0.0, //数量
    var unit: Int = 0, // 单位 0:分钟 1：小时
    var createTime: Date? = Date()

) : BaseEventDetailEntity() {
    override fun toString(): String {
        return "ExerciseDetailEntity(${super.toString()}, intensity_category_name='$intensity_category_name', quantity=$quantity, unit=$unit, hour_kcal_per_kg=$hour_kcal_per_kg)"
    }

    override fun getEventDesc(splitter: String?): String {
        return splitter?.let {
            "$name$splitter${quantity.stripTrailingZeros()}${unitStr}"
        } ?: let {
            "$name(${quantity.stripTrailingZeros()}${unitStr})"
        }
    }

    override fun getCurrClassMutableListType(): Type = getMutableListType<ExerciseDetailEntity>()
}
