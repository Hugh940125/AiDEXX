package com.microtech.aidexx.ui.main.home.chart

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.entity.*
import com.microtech.aidexx.ui.main.home.chart.dataset.*
import com.microtech.aidexx.utils.CalibrateManager
import com.microtech.aidexx.utils.LogUtils
import com.microtech.aidexx.utils.ThresholdManager
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.widget.dialog.x.util.toGlucoseValue
import com.microtechmd.blecomm.constant.History
import com.microtech.aidexx.db.entity.DietEntity
import com.microtech.aidexx.db.entity.ExerciseEntity
import com.microtech.aidexx.db.entity.InsulinEntity
import com.microtech.aidexx.db.entity.MedicationEntity
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList


class ChartManager private constructor() {


    companion object {
        private val instance: ChartManager = ChartManager()

        fun instance(): ChartManager {
            return instance
        }
    }

    private val currentSet = CurrentGlucoseDataSet()
    private val glucoseSets: MutableList<LineDataSet> = CopyOnWriteArrayList()
    val calSet = CalDataSet()
    val bgSet = BgDataSet()
    val eventSet = IconDataSet()
    var onUpdate: (() -> Unit)? = null

    var granularity: Long = 1L
    var upperLimit = 12f.toGlucoseValue()
        get() {
            return ThresholdManager.hyper.toGlucoseValue()
        }
        private set
    var lowerLimit = 4f.toGlucoseValue()
        get() {
            return ThresholdManager.hypo.toGlucoseValue()
        }
        private set

    internal var timeMin: Float? = null
        private set
    internal var timeMax: Float? = null
        private set

    fun getMinSecond(): Long {
        return XAxisUtils.xToSecond(timeMin ?: 0f)
    }

    fun getMaxSecond(): Long {
        return XAxisUtils.xToSecond(timeMax ?: 0f)
    }

    fun clearEventSets() {
        bgSet.clear()
        eventSet.clear()
    }

    fun clearGlucoseSets() {
        glucoseSets.clear()
        calSet.clear()
        timeMin = null
        timeMax = null
    }

    suspend fun updateGlucoseSets(cgmHistories: List<CgmHistoryEntity>) {
        if (glucoseSets.isEmpty()) glucoseSets.add(GlucoseDataSet())
        if (cgmHistories.isEmpty()) return
        loop@ for (history in cgmHistories) {
            when (history.eventType) {
                History.HISTORY_GLUCOSE, History.HISTORY_GLUCOSE_RECOMMEND_CAL -> {
                    if (history.eventData == null || history.eventWarning == -1) continue@loop
                    val dateTime = XAxisUtils.dateToX(history.deviceTime)
                    val entry = Entry(dateTime, history.eventData!!.toFloat().toGlucoseValue())
                    if (entry.y < 2f.toGlucoseValue()) {
                        entry.y = 2f.toGlucoseValue()
                    }// 小于2的数值 都当2处理
                    glucoseSets.last().addEntryOrdered(entry)
                    xMaxMin(dateTime)
                }
            }
        }

        LogUtils.data("glucoseSets last : ${glucoseSets.last().entries.size}")

        var all: MutableList<CalerateEntity> = mutableListOf()
        all = CalibrateManager.getCalibrateHistorys()
        calSet.clear()
        for (item in all) {
            updateCalibrationSet(item)
        }

//        ObjectBox.store.runInTxAsync({
//            all = CalibrateManager.getCalibrateHistorys()
//        }) { _, _ ->
//            calSet.clear()
//            for (item in all) {
//                updateCalibrationSet(item)
//            }
//        }

    }

    fun initBgSet(bgs: List<BloodGlucoseEntity>) {
        LogUtils.error("initBgSet")
        bgSet.clear()
        for (bg in bgs) {
            val dateTime = XAxisUtils.dateToX(bg.testTime)
//            val entry = Entry(
//                dateTime,
//                bg.bloodGlucose.toGlucoseValue()
//            )
            val entry = getGlucoseEntity(dateTime, bg)

            entry.data = bg
            entry.icon = BgDataSet.icon
            bgSet.addEntryOrdered(entry)

            xMaxMin(dateTime)
        }
    }

    fun updateBgSet(bgs: List<BloodGlucoseEntity>) {
        for (bg in bgs) {
            val dateTime = XAxisUtils.dateToX(bg.testTime)
//            val entry = Entry(
//                dateTime,
//                bg.bloodGlucose.toGlucoseValue()
//            )
            val entry = getGlucoseEntity(dateTime, bg)
            entry.data = bg
            entry.icon = BgDataSet.icon
            bgSet.addEntryOrdered(entry)

            xMaxMin(dateTime)
        }
    }

    private fun getGlucoseEntity(
        dateTime: Float,
        bg: BloodGlucoseEntity,
    ): Entry {
        return Entry(
            dateTime,
            getGlucoseValue(bg)
        )
    }

    private fun getGlucoseValue(bg: BloodGlucoseEntity): Float {
        if (bg.bloodGlucose < 2) {
            return 2f.toGlucoseValue()
        }
        if (UnitManager.glucoseUnit == UnitManager.GlucoseUnit.MMOL_PER_L) {
            if (bg.bloodGlucose > 30) {
                return 30f.toGlucoseValue()
            } else {
                bg.bloodGlucose.toGlucoseValue()
            }
        } else {
            return if (bg.bloodGlucose >= 600) {
                33.3f.toGlucoseValue()
            } else {
                bg.bloodGlucose.toGlucoseValue()
            }
        }
        return bg.bloodGlucose.toGlucoseValue()
    }


    fun updateCalibrationSet(history: CalerateEntity) {
        val dateTime = XAxisUtils.dateToX(history.calTime)
        val bg = BloodGlucoseEntity(history.calTime, history.referenceGlucose!!.toFloat())
        bg.calibration = true
        val entry = Entry(dateTime, bg.bloodGlucose.toGlucoseValue())
        entry.data = bg
        entry.icon = CalDataSet.icon
        calSet.addEntryOrdered(entry)
        xMaxMin(dateTime)
    }


    fun <T : EventEntity> initIconSet(es: List<T>) {
        LogUtils.error("initIconSet")
        eventSet.clear()
        for (e in es) {
            val entry = Entry(XAxisUtils.secondToX(e.time.time / 1000), 5f.toGlucoseValue())
            entry.data = e
            entry.icon = when (e.javaClass) {
                InsulinEntity::class.java -> IconDataSet.insulinIcon
                DietEntity::class.java -> IconDataSet.dietIcon
                MedicationEntity::class.java -> IconDataSet.medicineIcon
                ExerciseEntity::class.java -> IconDataSet.exerciseIcon
                OthersEntity::class.java -> IconDataSet.otherMarkIcon
                else -> null
            }
            eventSet.addEntryOrdered(entry)
        }
        LogUtils.debug("eventSet :" + eventSet.entries?.size)
    }

    fun <T : EventEntity> updateIconSet(es: List<T>) {
        for (e in es) {
            val entry = Entry(XAxisUtils.secondToX(e.time.time / 1000), 5f.toGlucoseValue())
            entry.data = e
            entry.icon = when (e.javaClass) {
                InsulinEntity::class.java -> IconDataSet.insulinIcon
                DietEntity::class.java -> IconDataSet.dietIcon
                MedicationEntity::class.java -> IconDataSet.medicineIcon
                ExerciseEntity::class.java -> IconDataSet.exerciseIcon
                OthersEntity::class.java -> IconDataSet.otherMarkIcon
                else -> null
            }
            eventSet.addEntryOrdered(entry)
        }
    }

    fun getCurrentGlucose(): LineDataSet {
        currentSet.setCircleColorRanges(
            listOf(
                upperLimit,
                lowerLimit - 0.1f.toGlucoseValue(),
                0f
            )
        )
        return currentSet
    }

    fun clearCurrentGlucose() {
        currentSet.clear()
    }

    fun setCurrentGlucose(time: Date?, glucose: Float?) {
        if (time != null && glucose != null && UserInfoManager.shareUserInfo == null) {
            currentSet.clear()
            currentSet.addEntry(
                Entry(
                    XAxisUtils.dateToX(time),
                    if (glucose > 2f) glucose.toGlucoseValue() else 2f.toGlucoseValue()
                    // 小于2的数值 都当2处理
                )
            )
        }
    }

    fun getGlucoseSets(): List<LineDataSet> {
        for (glucoseSet in glucoseSets) {
            glucoseSet.gradientPositions = listOf(
                upperLimit,
                upperLimit,
                lowerLimit,
                lowerLimit
            )

            glucoseSet.fillFormatter = object : IFillFormatter {
                override fun getFillLinePosition(
                    dataSet: ILineDataSet?,
                    dataProvider: LineDataProvider?,
                ): Float {
                    return (upperLimit + lowerLimit) / 2
                }

                override fun getFillLine(
                    dataSet: ILineDataSet?,
                    dataProvider: LineDataProvider?,
                ): ILineDataSet? {
                    return null
                }
            }

            glucoseSet.fillGradientPositions = listOf(
                upperLimit + 10f.toGlucoseValue(),
                upperLimit,
                upperLimit,
                lowerLimit,
                lowerLimit,
                lowerLimit - 1f.toGlucoseValue()
            )

            glucoseSet.setCircleColorRanges(
                listOf(
                    upperLimit,
                    lowerLimit - 0.1f.toGlucoseValue(),
                    0f
                )
            )
        }
        return glucoseSets
    }

    private fun xMaxMin(dateTime: Float) {
        if (timeMin == null || timeMin!! > dateTime) {
            timeMin = dateTime
        }

        if (timeMax == null || timeMax!! < dateTime) {
            timeMax = dateTime
        }
    }
}