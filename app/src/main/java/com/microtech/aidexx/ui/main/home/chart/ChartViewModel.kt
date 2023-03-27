package com.microtech.aidexx.ui.main.home.chart

import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.microtech.aidexx.R
import com.microtech.aidexx.common.getContext
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.DbRepository
import com.microtech.aidexx.db.entity.*
import com.microtech.aidexx.utils.*
import com.microtech.aidexx.widget.chart.GlucoseChart.Companion.CHART_LABEL_COUNT
import com.microtech.aidexx.widget.chart.MyChart.ChartGranularityPerScreen
import com.microtech.aidexx.widget.chart.MyChart.Companion.G_SIX_HOURS
import com.microtech.aidexx.widget.chart.XAxisUtils
import com.microtech.aidexx.widget.chart.dataset.*
import com.microtech.aidexx.widget.dialog.x.util.toGlucoseValue
import com.microtechmd.blecomm.constant.History
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs

class ChartViewModel: ViewModel() {

    /**
     * 图表数据总集
     */
    private val combinedData = CombinedData()

    private val currentSet = CurrentGlucoseDataSet()
    private val glucoseSets: MutableList<LineDataSet> = CopyOnWriteArrayList()
    private val calSet = CalDataSet()
    private val bgSet = BgDataSet()
    private val eventSet = IconDataSet()

    var timeMin: Float? = null
        private set
    var timeMax: Float? = null
        private set

    private val mGranularityFlow = MutableStateFlow<Int?>(null)
    private fun getGranularity() = mGranularityFlow.value ?: G_SIX_HOURS
    val granularityFlow = mGranularityFlow.asStateFlow()

    /** 启动加载下一页任务 */
    val startLoadNextPage = MutableStateFlow(false)

    val mDataChangedFlow = MutableStateFlow<Pair<Float?, Boolean>?>(null)

    /**
     * 切换每屏的图表时间跨度
     */
    fun updateGranularity(@ChartGranularityPerScreen granularity: Int) {
        mGranularityFlow.tryEmit(granularity)
    }

    init {
        viewModelScope.launch {
            startLoadNextPage.collect {
                if (it) {
                    var maxTime = Date()
                    val curMinTime = timeMin?.let { x ->
                        maxTime = Date(XAxisUtils.xToSecond(x))
                        getCurPageStartDate(XAxisUtils.xToSecond(x))
                    } ?: getCurPageStartDate(System.currentTimeMillis())

                    getCgmPageData(curMinTime, maxTime)?.let { d ->
                        updateGlucoseSets(d)
                    }

                    mDataChangedFlow.emit(Pair(timeMin, false))
                    //重置标记
                    startLoadNextPage.compareAndSet(true, update = false)
                }
            }
        }
    }

    /**
     * 首次 加载第一页数据
     */
    fun initData() = flow {

        val startDate = getCurPageStartDate()
        val endDate = Date()

        // 加载一页cgm数据
        val cgmData = getCgmPageData(startDate, endDate)

        cgmData?.let {
            updateGlucoseSets(it)
        }

        //todo 加载一页事件等数据

        val lineDataSets: ArrayList<ILineDataSet> = ArrayList()
        lineDataSets.addAll(generateLimitLines())

        val glucoseSets: List<LineDataSet> = getGlucoseSets()
        LogUtils.data("Glucose Set Size ${glucoseSets.size}")
        lineDataSets.addAll(glucoseSets)

        if (UserInfoManager.shareUserInfo == null) {
            val currentGlucose = getCurrentGlucose()
            currentGlucose.circleHoleColor =
                ThemeManager.getTypeValue(getContext(), R.attr.containerBackground)
            lineDataSets.add(currentGlucose)
        }

        val scatterDataSets: ArrayList<IScatterDataSet> = ArrayList()
        scatterDataSets.add(calSet)
        scatterDataSets.add(bgSet)
        scatterDataSets.add(eventSet)

        combinedData.setData(LineData(lineDataSets))
        combinedData.setData(ScatterData(scatterDataSets))

        emit(combinedData)

    }

    /** 是否需要加载下一页 */
    fun needLoadNextPage(isLtr: Boolean, visibleLeftX: Float, xAxisMin: Float): Boolean {

        val isLeftTwoDays = abs(
            XAxisUtils.xToSecond(visibleLeftX) - XAxisUtils.xToSecond(xAxisMin)
        ) <= TimeUtils.oneDaySeconds * 2

        return !isLtr && isLeftTwoDays
    }

    private suspend fun getCgmPageData(startDate: Date, endDate: Date) =
        DbRepository.queryCgmByPage(
            startDate,
            endDate,
            UserInfoManager.getCurShowUserId()
        )


    private fun getCurPageStartDate(curTime: Long = System.currentTimeMillis()): Date =
        Date(curTime - 1000 * 60 * 60 * 24 * 7)


    private fun generateLimitLines(): List<LineDataSet> {
        val l1 = LineDataSet(
            listOf(
                Entry(xMin(), upperLimit),
                Entry(xMax(), upperLimit)
            ),
            ""
        )
        l1.axisDependency = YAxis.AxisDependency.RIGHT
        l1.setDrawValues(false)
        l1.setDrawCircles(false)
        l1.color = Color.TRANSPARENT
        l1.lineWidth = 0f
        l1.isHighlightEnabled = false

        val l2 = LineDataSet(
            listOf(
                Entry(xMin(), lowerLimit),
                Entry(xMax(), lowerLimit)
            ),
            ""
        )
        l2.setDrawValues(false)
        l2.setDrawCircles(false)
        l2.color = Color.TRANSPARENT
        l2.lineWidth = 0f
        l2.isHighlightEnabled = false
        l1.setDrawFilled(false)

        return listOf(l1, l2)
    }

    private fun xRange(): Float {
        return getGranularity() * CHART_LABEL_COUNT.toFloat()
    }

    private fun xMargin(): Float {
        return getGranularity() / 2f
    }

    private fun xMin(): Float {
        val default = xMax() - xRange()
        return if (timeMin == null || timeMin!! > default) default
        else timeMin!!
    }

    private fun xMax(): Float {
        return XAxisUtils.secondToX(Date().time / 1000) + xMargin()
    }






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

    //todo 用户退出时调用
    fun clearEventSets() {
        bgSet.clear()
        eventSet.clear()
    }

    // todo 用户退出是调用
    fun clearGlucoseSets() {
        glucoseSets.clear()
        calSet.clear()
        timeMin = null
        timeMax = null
    }

    private suspend fun updateGlucoseSets(cgmHistories: List<CgmHistoryEntity>) {
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

    private fun getCurrentGlucose(): LineDataSet {
        currentSet.setCircleColorRanges(
            listOf(
                upperLimit,
                lowerLimit - 0.1f.toGlucoseValue(),
                0f
            )
        )
        return currentSet
    }

    // todo 解配成功后调用
    fun clearCurrentGlucose() {
        currentSet.clear()
    }

    // todo 设置当前血糖值后调用
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

    private fun getGlucoseSets(): List<LineDataSet> {
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