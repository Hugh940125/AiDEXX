package com.microtech.aidexx.db.entity

import com.microtech.aidexx.common.getMutableListType
import com.microtech.aidexx.common.stripTrailingZeros
import java.lang.reflect.Type
import java.util.*


data class InsulinDetailEntity(

    var presetUuid: String? = null,

    var relUuid: String? = null,

    var categoryName: String = "",
    var tradeName: String = "",
    var comment: String = "",
    var manufacturer: String = "",
    var quantity: Double = 0.0,
    var unit: Int = 0,
    var delete_flag: Int = 0,

    var createTime: Date = Date(),
    @Transient
    var updateTime: Date = Date()
) : BaseEventDetailEntity() {

    override fun getCurrClassMutableListType(): Type = getMutableListType<InsulinDetailEntity>()

    override fun getEventDesc(splitter: String?): String {
        return splitter?.let {
            "$name$splitter${quantity.stripTrailingZeros()}U"
        } ?: let {
            val ext = tradeName.ifEmpty { manufacturer.ifEmpty { "" } }
            name.plus(if (ext.isNotEmpty()) "(${ext})" else "")
                .plus("(${quantity.stripTrailingZeros()}U)")
        }
    }

    override fun toString(): String {
        return "InsulinDetailEntity(presetUuid=$presetUuid, relUuid=$relUuid, categoryName='$categoryName', tradeName='$tradeName', comment='$comment', manufacturer='$manufacturer', quantity=$quantity, unit=$unit, delete_flag=$delete_flag, createTime=$createTime, updateTime=$updateTime)"
    }


}
