package com.microtech.aidexx.db.entity

import com.microtech.aidexx.common.getMutableListType
import com.microtech.aidexx.common.stripTrailingZeros
import java.lang.reflect.Type
import java.util.*


data class MedicationDetailEntity(

    var medicine_preset_id: Long? = null,
    var category_name: String? = null, //类别名称
    var tradeName: String = "", // 商品名
    var manufacturer: String = "", // 厂商
    var english_name: String? = null, // 英文名称
    var quantity: Double = 0.0, //数量
    var unit: Int = 0, // 单位，0：毫克，1：克，2：片，3：粒
    var createTime: Date? = Date()

) : BaseEventDetailEntity() {
    override fun toString(): String {
        return "ExerciseDetailEntity(${super.toString()}, category_name='$category_name', " +
                "quantity=$quantity, unit=$unit, tradeName='$tradeName', " +
                "manufacturer='$manufacturer', english_name='$english_name')"
    }

    override fun getEventDesc(splitter: String?): String {
        return splitter?.let {
            "$name$splitter${quantity.stripTrailingZeros()}${unitStr}"
        } ?: let {
            val ext = tradeName.ifEmpty {
                manufacturer
            }
            name.plus(if (ext.isNotEmpty()) "(${ext})" else "")
                .plus("(${quantity.stripTrailingZeros()}${unitStr})")
        }
    }

    override fun getCurrClassMutableListType(): Type = getMutableListType<MedicationDetailEntity>()

}
