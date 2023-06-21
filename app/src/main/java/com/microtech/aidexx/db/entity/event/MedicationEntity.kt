package com.microtech.aidexx.db.entity.event

import android.content.res.Resources
import com.microtech.aidexx.common.formatWithoutZone
import com.microtech.aidexx.common.getMutableListType
import com.microtech.aidexx.common.stripTrailingZeros
import com.microtech.aidexx.data.LocalManager
import com.microtech.aidexx.db.entity.BaseEventEntity
import com.microtech.aidexx.ui.main.event.EventParameterManager
import com.microtech.aidexx.ui.main.event.TYPE_SLOT_MEDICINE
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Index
import io.objectbox.annotation.IndexType
import java.lang.reflect.Type
import java.util.Date
import java.util.TimeZone
import java.util.UUID


@Entity
class MedicationEntity : BaseEventEntity {

    @Index(type = IndexType.HASH)
    var medicationId: String? = UUID.randomUUID().toString().replace("-", "")

    var takenTime: Date = Date()
        set(value) {
            field = value
            appTime = value.formatWithoutZone() // yyyy-MM-dd HH:mm:ss
            appTimeZone = TimeZone.getDefault().id //
            dstOffset = TimeZone.getDefault().dstSavings //
        }

    @Index(type = IndexType.HASH)
    var medicineName: String? = null

    //    @Uid(499736593101406176L)
    var medicineDosage: Float? = null
    var isPreset: Boolean = false


    @Convert(converter = MedicationDetail::class, dbType = String::class)
    var expandList: MutableList<MedicationDetail> = ArrayList()

    constructor() {
        this.language = LocalManager.getCurLanguageTag()
    }

    constructor(medicineName: String? = null, medicineDosage: Float? = null) : this() {
        this.medicineName = medicineName
        this.medicineDosage = medicineDosage
    }

    override fun getEventDescription(res: Resources): String {
        var description = ""
        if (expandList.isEmpty()) {
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
            expandList?.forEach { exerciseEntity ->
                medications.add(exerciseEntity.getEventDesc())
            }
            description += medications.joinToString(";")
        }
        return description
    }

    override fun getValueDescription(res: Resources): String = ""

    fun getEventValue(res: Resources): String = ""
    private fun getTypeText(): String =
        EventParameterManager.instance().getEventSlotByIndex(TYPE_SLOT_MEDICINE, moment-1)

    override fun hashCode(): Int {
        return medicationId.hashCode()
    }
    override fun equals(other: Any?): Boolean {
        return other?.let {
            it is MedicationEntity && it.medicationId == this.medicationId
        } ?: false
    }

    override fun toString(): String {
        return "InsulinEntity(idx=$idx, state=$state, id=$id, recordIndex=$recordIndex, deleteStatus=$deleteStatus, isPreset=$isPreset, authorizationId=$userId, relList=${
            expandList.joinToString(
                ","
            )
        }, timestamp=$timestamp)"
    }

}

data class MedicationDetail(

    var category_name: String? = null, //类别名称
    var tradeName: String = "", // 商品名
    var manufacturer: String = "", // 厂商
    var english_name: String? = null, // 英文名称
    var quantity: Double = 0.0, //数量
    var unit: Int = 0, // 单位，0：毫克，1：克，2：片，3：粒
    var medicationId: String? = null,
    var medicationPresetId: String? = null

) : BaseEventDetail() {
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

    override fun getCurrClassMutableListType(): Type = getMutableListType<MedicationDetail>()

}