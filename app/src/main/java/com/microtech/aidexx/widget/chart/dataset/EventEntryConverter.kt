package com.microtech.aidexx.widget.chart.dataset

import com.github.mikephil.charting.data.Entry
import com.microtech.aidexx.db.entity.BaseEventEntity
import com.microtech.aidexx.db.entity.BloodGlucoseEntity
import com.microtech.aidexx.db.entity.CalibrateEntity
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import com.microtech.aidexx.db.entity.event.DietEntity
import com.microtech.aidexx.db.entity.event.ExerciseEntity
import com.microtech.aidexx.db.entity.event.InsulinEntity
import com.microtech.aidexx.db.entity.event.MedicationEntity
import com.microtech.aidexx.db.entity.event.OthersEntity
import com.microtech.aidexx.utils.toGlucoseValue
import com.microtech.aidexx.widget.chart.ChartUtil

class ChartEntry(xValue: Float, yValue: Float, data: Any) : Entry(xValue, yValue, data) {
    override fun hashCode(): Int {
        return data.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other?.let {
            it is ChartEntry && it.data is String && data is String && data.equals(it.data)
        } ?: false
    }
}

fun BaseEventEntity.toChartEntry(getY: (()->Float)? = null): ChartEntry =
    when (this) {
        is RealCgmHistoryEntity -> {
            val xValue = ChartUtil.millSecondToX(timestamp)
            val entry = ChartEntry(xValue, glucose!!.toFloat().toGlucoseValue(), "CGM-${idx}")
            if (entry.y < 2f.toGlucoseValue()) {
                entry.y = 2f.toGlucoseValue()
            }// 小于2的数值 都当2处理
            entry
        }
        is BloodGlucoseEntity -> {
            val xValue = ChartUtil.millSecondToX(timestamp)
            val entry = ChartEntry(xValue, getGlucoseValue(), "BG-${idx}")
//            entry.data = this
            entry.icon = BgDataSet.icon
            entry
        }
        is CalibrateEntity -> {
            val xValue = ChartUtil.millSecondToX(timestamp)
            val bg = BloodGlucoseEntity(calTime, referenceGlucose)
            bg.calibration = true
            val entry = ChartEntry(xValue, bg.bloodGlucoseMg.toGlucoseValue(), "CAL-${idx}")
//            entry.data = bg
            entry.icon = CalDataSet.icon
            entry
        }
        else -> {
            val entry = ChartEntry(
                ChartUtil.millSecondToX(timestamp),
                getY?.invoke() ?: (5f * 18).toGlucoseValue(),
                "${this.javaClass.simpleName}-$idx"
            )
//            entry.data = this
            entry.icon = when (this.javaClass) {
                InsulinEntity::class.java -> IconDataSet.insulinIcon
                DietEntity::class.java -> IconDataSet.dietIcon
                MedicationEntity::class.java -> IconDataSet.medicineIcon
                ExerciseEntity::class.java -> IconDataSet.exerciseIcon
                OthersEntity::class.java -> IconDataSet.otherMarkIcon
                else -> null
            }
            entry
        }
    }

