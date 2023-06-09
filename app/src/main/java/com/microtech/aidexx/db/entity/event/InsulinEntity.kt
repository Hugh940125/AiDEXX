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
class InsulinEntity : BaseEventEntity {

    var injectionTime: Date = Date()
        set(value) {
            field = value
            appTime = value.formatWithoutZone() // yyyy-MM-dd HH:mm:ss
            appTimeZone = TimeZone.getDefault().id //
            dstOffset = TimeZone.getDefault().dstSavings //
        }
    var insulinName: String? = null

    @Index(type = IndexType.HASH)
    var insulinId: String? = UUID.randomUUID().toString().replace("-", "")

    var insulinDosage: Float? = null
    var isPreset: Boolean = false

    @Convert(converter = InsulinDetail::class, dbType = String::class)
    var expandList: MutableList<InsulinDetail> = ArrayList()
    var momentType: Int = 0

    constructor() {
        this.language = LanguageUnitManager.getCurrentLanguageCode()
    }

    constructor(insulinName: String? = null, insulinDosage: Float? = null) : this() {
        this.insulinName = insulinName
        this.insulinDosage = insulinDosage
    }


    override fun getEventDescription(res: Resources): String {
        var description = ""
        if (expandList.isNullOrEmpty()) {
            description =
                insulinName + "(${insulinDosage}U)"
        } else {
            val injections = mutableListOf<String>()
            val timeSlot = getTypeText()
            if (!timeSlot.isNullOrBlank()) {
                description = "$timeSlot："
            }

            expandList?.forEach { insulinDetailEntity ->
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
        return "InsulinEntity(idx=$idx, state=$state, id=$id, recordIndex=$recordIndex, deleteStatus=$deleteStatus, injectionTime=$injectionTime, insulinName=$insulinName, insulinId=$insulinId, insulinDosage=$insulinDosage, isPreset=$isPreset, authorizationId=$userId, relList=${
            expandList.joinToString(
                ","
            )
        }, timestamp=$timestamp)"
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

    override fun hashCode(): Int {
        return insulinId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other?.let {
            it is InsulinEntity && it.insulinId == this.insulinId
        } ?: false
    }
}

data class InsulinDetail(

    var relUuid: String? = null,
    var categoryName: String = "",
    var tradeName: String = "",
    var comment: String = "",
    var manufacturer: String = "",
    var quantity: Double = 0.0,
    var unit: Int = 0,
    var delete_flag: Int = 0,
    var insulinPresetId: String? = null,

    @Transient
    var updateTime: Date = Date(),

    var insulinId: String? = ""
) : BaseEventDetail() {

    override fun getCurrClassMutableListType(): Type = getMutableListType<InsulinDetail>()

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
        return "InsulinDetailEntity(insulinPresetId=$insulinPresetId, relUuid=$relUuid, categoryName='$categoryName', tradeName='$tradeName', comment='$comment', manufacturer='$manufacturer', quantity=$quantity, unit=$unit, delete_flag=$delete_flag, createTime=$createTime, updateTime=$updateTime)"
    }


}