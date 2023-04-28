package com.microtech.aidexx.db.entity

import android.content.res.Resources
import com.microtech.aidexx.R
import com.microtech.aidexx.common.getContext
import com.microtech.aidexx.utils.LanguageUnitManager
import io.objectbox.annotation.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.jvm.Transient


@Entity
class ExerciseEntity : EventEntity {
    override var createTime: Date = Date()

    @Id
    override var idx: Long? = null

    @Index
    override var state: Int = 0

    @Index(type = IndexType.HASH)
    override var id: String? = null

    @Index
    override var recordIndex: Long? = null

    @Index
    override var deleteStatus: Int = 0

    @Index(type = IndexType.HASH)
    var recordUuid: String? = UUID.randomUUID().toString().replace("-", "")

    @Index
    var startTime: Date = Date()
    var duration: Int? = null
    var intensity: Int? = null
    var isPreset: Boolean = false

    override var recordId: String? = null

    @Convert(converter = ExerciseDetailEntity::class, dbType = String::class)
    var relList: MutableList<ExerciseDetailEntity> = ArrayList()

    @Index(type = IndexType.HASH)
    override var authorizationId: String? = null

    @Transient
    override var time: Date = startTime
        get() {
            return startTime
        }
        set(time) {
            field = time
            startTime = time
        }

    override var language: String = ""

    constructor() {
        this.language = LanguageUnitManager.getCurrentLanguageCode()
    }

    constructor(intensity: Int? = null, duration: Int? = null) : this() {
        this.intensity = intensity
        this.duration = duration
    }

    override fun getEventDescription(res: Resources): String {
        return if (relList.isEmpty()) {
            "${
                (getExerciseMap()[(intensity ?: 1).coerceIn(
                    1,
                    3
                )])
            }" + "(${duration}${getContext().getString(R.string.min)})"
        } else {
            val sports = mutableListOf<String>()
            relList?.forEach { exerciseDetailEntity ->
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