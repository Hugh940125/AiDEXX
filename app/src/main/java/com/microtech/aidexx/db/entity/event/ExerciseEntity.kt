package com.microtech.aidexx.db.entity.event

import android.content.res.Resources
import com.microtech.aidexx.R
import com.microtech.aidexx.common.formatWithoutZone
import com.microtech.aidexx.common.getContext
import com.microtech.aidexx.common.getMutableListType
import com.microtech.aidexx.common.stripTrailingZeros
import com.microtech.aidexx.data.resource.LanguageResourceManager
import com.microtech.aidexx.db.entity.BaseEventEntity
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Index
import io.objectbox.annotation.IndexType
import java.lang.reflect.Type
import java.util.Date
import java.util.TimeZone
import java.util.UUID


@Entity
class ExerciseEntity : BaseEventEntity {



    @Index(type = IndexType.HASH)
    var exerciseId: String? = UUID.randomUUID().toString().replace("-", "")

    @Index
    var startTime: Date = Date()
        set(value) {
            field = value
            appTime = value.formatWithoutZone() // yyyy-MM-dd HH:mm:ss
            appTimeZone = TimeZone.getDefault().id //
            dstOffset = calDstOffset() //
        }


    var duration: Int? = null
    var intensity: Int? = null
    var isPreset: Boolean = false

    @Convert(converter = ExerciseDetail::class, dbType = String::class)
    var expandList: MutableList<ExerciseDetail> = ArrayList()

    constructor() {
        this.language = LanguageResourceManager.getCurLanguageTag()
    }

    constructor(intensity: Int? = null, duration: Int? = null) : this() {
        this.intensity = intensity
        this.duration = duration
    }

    override fun getEventDescription(res: Resources): String {
        return if (expandList.isEmpty()) {
            "${
                (getExerciseMap()[(intensity ?: 1).coerceIn(
                    1,
                    3
                )])
            }" + "(${duration}${getContext().getString(R.string.min)})"
        } else {
            val sports = mutableListOf<String>()
            expandList?.forEach { exerciseDetailEntity ->
                sports.add(exerciseDetailEntity.getEventDesc())
            }
            sports.joinToString(";")
        }
    }

    fun getEventDescription2(res: Resources): String = ""


    override fun getValueDescription(res: Resources): String = ""


    fun getEventValue(res: Resources): String {
        return duration.toString()
    }

    override fun hashCode(): Int {
        return exerciseId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other?.let {
            it is ExerciseEntity && it.exerciseId == this.exerciseId
        } ?: false
    }

    override fun toString(): String {
        return "ExerciseEntity(idx=$idx, state=$state, id=$id, recordIndex=$recordIndex, deleteStatus=$deleteStatus, isPreset=$isPreset, authorizationId=$userId, relList=${
            expandList.joinToString(
                ","
            )
        }, timestamp=$timestamp)"
    }

    companion object {
        fun getExerciseMap(): Map<Int, String> {
            val result = mutableMapOf<Int, String>()
            getContext().resources.getStringArray(R.array.exerciseLevel).asList()
                .forEachIndexed { index, s ->
                    result[index + 1] = s
                }
            return result
        }
    }

}


data class ExerciseDetail(

    var exec_preset_id: Long? = null,
    var intensity_category_name: String? = null, //强度分类
    var hour_kcal_per_kg: Double = 0.0, // 每小时单位公斤消费千卡数
    var quantity: Double = 0.0, //数量
    var unit: Int = 0, // 单位 0:分钟 1：小时
    var exerciseId: String? = null,
    var exercisePresetId: String? = null

) : BaseEventDetail() {
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

    override fun getCurrClassMutableListType(): Type = getMutableListType<ExerciseDetail>()
}