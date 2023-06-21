package com.microtech.aidexx.ui.main.event

import android.content.Context
import androidx.annotation.IntDef
import com.microtech.aidexx.R
import com.microtech.aidexx.common.getContext
import java.util.*


const val TYPE_SLOT_DIET = 1
const val TYPE_SLOT_MEDICINE = 2
const val TYPE_SLOT_INSULIN = 3

@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FIELD,
    AnnotationTarget.LOCAL_VARIABLE
)
@IntDef(TYPE_SLOT_DIET, TYPE_SLOT_MEDICINE, TYPE_SLOT_INSULIN)
annotation class EventSlotType


class EventParameterManager private constructor(val ctx: Context = getContext()) {


    private val breakfast = ctx.resources.getString(R.string.breakfast)
    private val lunch = ctx.resources.getString(R.string.lunch)
    private val dinner = ctx.resources.getString(R.string.dinner)
    private val beforeSleep = ctx.resources.getString(R.string.before_sleep)
    private val makeup = ctx.resources.getString(R.string.makeup) // 胰岛素补打
    private val extraMeal = ctx.resources.getString(R.string.extra_meal) // 加餐
    private val other = ctx.resources.getString(R.string.other) // 用药其他

    val slots = mutableListOf(breakfast, lunch, dinner)
    val slotsDiet = mutableListOf(extraMeal)
    val slotsMedicine = mutableListOf(beforeSleep, other)
    val slotsInsulin = mutableListOf(beforeSleep, makeup)


    companion object {

        // 非单例 value反查索引 多语言切换查找失败问题
        fun instance(): EventParameterManager {
            return EventParameterManager()
        }
    }

    fun getTypes(@EventSlotType type: Int): List<String> {
        val list = mutableListOf<String>()
        list.addAll(slots)
        when (type) {
            TYPE_SLOT_DIET -> {
                list.addAll(slotsDiet)
            }
            TYPE_SLOT_MEDICINE -> {
                list.addAll(slotsMedicine)
            }
            TYPE_SLOT_INSULIN -> {
                list.addAll(slotsInsulin)
            }
        }

        return list
    }

    fun getEventSlot(@EventSlotType type: Int): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 6 until 10 -> slots[0]
            in 10 until 14 -> slots[1]
            in 16 until 20 -> slots[2]
            in 20 until 24 -> slotForCondition(type, true)
            else -> slotForCondition(type)
        }
    }

    fun getEventSlotByTime(@EventSlotType type: Int, time: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = time
        return when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 6 until 10 -> slots[0]
            in 10 until 14 -> slots[1]
            in 16 until 20 -> slots[2]
            in 20 until 24 -> slotForCondition(type, true)
            else -> slotForCondition(type)
        }
    }

    private fun slotForCondition(@EventSlotType type: Int, isIn2024: Boolean = false): String {

        return when (type) {
            TYPE_SLOT_DIET -> extraMeal
            TYPE_SLOT_MEDICINE -> if (isIn2024) beforeSleep else other
            TYPE_SLOT_INSULIN -> if (isIn2024) beforeSleep else makeup
            else -> slots[0]
        }
    }

    fun getEventSlotByIndex(@EventSlotType type: Int, index: Int): String {
        val list = getTypes(type)
        if (index in list.indices) {
            return list[index]
        }
        return ""
    }

    fun getEventSlotIndex(@EventSlotType type: Int): Int {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 6 until 10 -> slots.indexOf(breakfast)
            in 10 until 14 -> slots.indexOf(lunch)
            in 16 until 20 -> slots.indexOf(dinner)
            in 20 until 24 -> slotIndexForCondition(type, true)
            else -> slotIndexForCondition(type)
        }
    }

    private fun slotIndexForCondition(@EventSlotType type: Int, isIn2024: Boolean = false): Int {

        return slots.size + when (type) {
            TYPE_SLOT_DIET -> slotsDiet.indexOf(extraMeal)
            TYPE_SLOT_MEDICINE -> if (isIn2024) slotsMedicine.indexOf(beforeSleep)
                                    else slotsMedicine.indexOf(other)
            TYPE_SLOT_INSULIN -> if (isIn2024) slotsInsulin.indexOf(beforeSleep)
                                    else slotsInsulin.indexOf(makeup)
            else -> 0
        }
    }
}