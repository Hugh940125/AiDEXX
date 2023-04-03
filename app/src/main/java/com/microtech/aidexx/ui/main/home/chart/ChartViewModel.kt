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
import com.microtech.aidexx.utils.eventbus.CgmDataChangedInfo
import com.microtech.aidexx.utils.eventbus.DataChangedType
import com.microtech.aidexx.widget.chart.ChartUtil
import com.microtech.aidexx.widget.chart.GlucoseChart.Companion.CHART_LABEL_COUNT
import com.microtech.aidexx.widget.chart.MyChart.ChartGranularityPerScreen
import com.microtech.aidexx.widget.chart.MyChart.Companion.G_SIX_HOURS
import com.microtech.aidexx.widget.chart.dataset.*
import com.microtech.aidexx.widget.dialog.lib.util.toGlucoseValue
import com.microtechmd.blecomm.constant.History
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs

class ChartViewModel: ViewModel() {

    // 国际版代码先放这里 后面做打包渠道配置
    private val isGp = false

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

    class ChartChangedInfo(
        var timeMin: Float?,
        var needScrollToLatest: Boolean
    )

    private val mGranularityFlow = MutableStateFlow<Int?>(null)
    private fun getGranularity(): Int = mGranularityFlow.value ?: G_SIX_HOURS
    val granularityFlow = mGranularityFlow.asStateFlow()

    /** 启动加载下一页任务 */
    val startLoadNextPage = MutableStateFlow(false)

    /**
     * 数据图表数据发生变化通知外面刷新
     * value-Pair<Float?, Boolean>
     *   first: [timeMin] 当前x轴最小值
     *   second: true-滚动到最右端 false-不滚动
     */
    val mDataChangedFlow = MutableStateFlow<ChartChangedInfo?>(null)

    /** 标记当前最小日期数据是否已经触发了下一页的加载 滚动时防止重复触发下一页 */
    private var loadedMinDate = -Float.MAX_VALUE

    /** 当前页面数据最小日期 */
    private var curPageMinDate = Date()

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
                    LogUtil.d("===CHART===开始加载下一页")
                    withContext(Dispatchers.IO) {
                        var maxTime = Date(curPageMinDate.time - 1000) // ob between是前闭后闭
                        val curMinTime = getCurPageStartDate(curPageMinDate.time)
                        LogUtil.d("===CHART=== timeMin=$timeMin start=${curMinTime} end=${maxTime}")
                        getCgmPageData(curMinTime, maxTime)?.let { d ->
                            updateGlucoseSets(d)
                            LogUtil.d("===CHART=== 有数据")
                        }
                        LogUtil.d("===CHART=== 加载之后 timeMin=$timeMin start=${curMinTime} end=${maxTime}")
                    }
                    mDataChangedFlow.emit(ChartChangedInfo(timeMin, false))
                    //重置标记
                    val ret = startLoadNextPage.compareAndSet(true, update = false)
                    LogUtil.d("===CHART=== 通知及标记重置了 ret=$ret")
                }
            }
        }
    }

    /**
     * 首次 加载第一页数据
     */
    fun initData() = flow {

        val latestOne = DbRepository.queryLatestOne(UserInfoManager.getCurShowUserId()) //"f03550ef07a7b2164f06deaef597ce37"
        val endDate = latestOne?.let {
            it.deviceTime
        } ?: Date()

        // 加载一页cgm数据
        val cgmData = getCgmPageData(getCurPageStartDate(endDate.time), endDate)

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

        if (timeMax == null) {
            timeMax = ChartUtil.dateToX(Date())
        }
        if (timeMin == null) {
            timeMin = ChartUtil.dateToX(
                Date(Date().time - getGranularity() * 6 * TimeUtils.oneHourSeconds * 1000))
        }

        emit(combinedData)

    }.flowOn(Dispatchers.IO)

    /**
     *  todo 重新加载数据
     *      清空当前数据集
     *      重置所有标记位
     *      --通知外部刷页面
     *      加载第一页数据
     *      --通知外部刷页面
     */
    fun reload() {
    }

    /** 是否需要加载下一页 */
    fun needLoadNextPage(isLtr: Boolean, visibleLeftX: Float, xAxisMin: Float): Boolean {

        if (isLtr) return false

        if(loadedMinDate >= xAxisMin) {
            LogUtil.d("===CHART=== 滚动过程已经触发了下一页加载 不再触发")
            return false
        }

        val isLeftTwoDays = abs(
            ChartUtil.xToSecond(visibleLeftX) - ChartUtil.xToSecond(xAxisMin)
        ) <= TimeUtils.oneDaySeconds * 2

        if (isLeftTwoDays) {
            LogUtil.d("===CHART=== 滚动过程触发加载下一页")
            loadedMinDate = xAxisMin
        }
        return isLeftTwoDays
    }

    /**
     * 外部有数据变动通知过来后进行清洗合并
     */
    suspend fun onCgmDataChanged(data: CgmDataChangedInfo) {
        withContext(Dispatchers.IO) {
            when (data.first) {
                DataChangedType.ADD -> {
                    val rets = data.second.filter {
                        checkCgmHistory(it)
                                && it.deviceTime.time > (ChartUtil.xToSecond(timeMin?:0f) * 1000)
                    }
                    if (rets.isNotEmpty()) {
                        updateGlucoseSets(rets)
                        mDataChangedFlow.emit(ChartChangedInfo(timeMin, false))
                    }
                }
                else -> {}
            }
        }
    }


    private suspend fun getCgmPageData(startDate: Date, endDate: Date) =
        DbRepository.queryCgmByPage(
            startDate,
            endDate,
//            "f03550ef07a7b2164f06deaef597ce37"
            UserInfoManager.getCurShowUserId()
        )


    private fun getCurPageStartDate(curTime: Long = System.currentTimeMillis()): Date =
        Date(curTime - TimeUtils.oneDayMillis * 7)

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

    fun xRange(): Float {
        return getGranularity() * CHART_LABEL_COUNT.toFloat()
    }

    private fun xMargin(): Float {
        return getGranularity() / 2f
    }

    fun xMin(): Float {
        val default = xMax() - xRange()
        return if (timeMin == null || timeMin!! > default) default
        else timeMin!!
    }

    fun xMax(): Float {
        return ChartUtil.secondToX(Date().time / 1000) + xMargin()
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

    private suspend fun updateGlucoseSets(cgmHistories: List<RealCgmHistoryEntity>) {
        if (glucoseSets.isEmpty()) glucoseSets.add(GlucoseDataSet())
        if (cgmHistories.isEmpty()) {
            return
        }

        loop@ for (history in cgmHistories) {
            when (history.eventType) {
                History.HISTORY_GLUCOSE, History.HISTORY_GLUCOSE_RECOMMEND_CAL -> {
                    if (history.eventData == null || history.eventWarning == -1) continue@loop
                    val dateTime = ChartUtil.dateToX(history.deviceTime)
                    val entry = Entry(dateTime, history.eventData!!.toFloat().toGlucoseValue())
                    if (entry.y < 2f.toGlucoseValue()) {
                        entry.y = 2f.toGlucoseValue()
                    }// 小于2的数值 都当2处理
                    if (glucoseSets.isEmpty()) glucoseSets.add(GlucoseDataSet())
                    glucoseSets.last().addEntryOrdered(entry)
                    xMaxMin(dateTime)
                    calDateMaxMin(history.deviceTime)
                }
                History.HISTORY_CALIBRATION -> {
                    if (!isGp) {
                        updateCnCalibrationSet(history)
                    }
                }
            }
        }

        LogUtils.data("glucoseSets last : ${glucoseSets.last().entries.size}")

        if (isGp) {
            var all: MutableList<CalerateEntity> = mutableListOf()
            all = CalibrateManager.getCalibrateHistorys()
            calSet.clear()
            for (item in all) {
                updateGpCalibrationSet(item)
            }
        }

    }

    private fun checkCgmHistory(cgm: RealCgmHistoryEntity) =
        (History.HISTORY_GLUCOSE == cgm.eventType
                || History.HISTORY_GLUCOSE_RECOMMEND_CAL == cgm.eventType
                || History.HISTORY_CALIBRATION == cgm.eventType )
                && (cgm.eventData != null && cgm.eventWarning != -1)

    fun initBgSet(bgs: List<BloodGlucoseEntity>) {
        LogUtils.error("initBgSet")
        bgSet.clear()
        for (bg in bgs) {
            val dateTime = ChartUtil.dateToX(bg.testTime)
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
            val dateTime = ChartUtil.dateToX(bg.testTime)
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

    private fun updateCnCalibrationSet(history: RealCgmHistoryEntity) {
        if (history.eventData != null) {
            val dateTime = ChartUtil.dateToX(history.deviceTime)
            val bg = BloodGlucoseEntity(history.deviceTime, history.eventData!!.toFloat())
            bg.calibration = true
            val entry = Entry(dateTime, bg.bloodGlucose.toGlucoseValue())
            entry.data = bg
            entry.icon = CalDataSet.icon
            calSet.addEntryOrdered(entry)
            xMaxMin(dateTime)
            calDateMaxMin(history.deviceTime)
        }
    }

    // 国际版用
    private fun updateGpCalibrationSet(history: CalerateEntity) {
        val dateTime = ChartUtil.dateToX(history.calTime)
        val bg = BloodGlucoseEntity(history.calTime, history.referenceGlucose!!.toFloat())
        bg.calibration = true
        val entry = Entry(dateTime, bg.bloodGlucose.toGlucoseValue())
        entry.data = bg
        entry.icon = CalDataSet.icon
        calSet.addEntryOrdered(entry)
        xMaxMin(dateTime)
        // todo 国际版是直接从库里加载 应该也需要给个校准数据的最小日期标记 国内版是混合在cgm表中
//        calDateMaxMin(history.calTime)
    }


    fun <T : EventEntity> initIconSet(es: List<T>) {
        LogUtils.error("initIconSet")
        eventSet.clear()
        for (e in es) {
            val entry = Entry(ChartUtil.secondToX(e.time.time / 1000), 5f.toGlucoseValue())
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
        LogUtil.d("eventSet :" + eventSet.entries?.size)
    }

    fun <T : EventEntity> updateIconSet(es: List<T>) {
        for (e in es) {
            val entry = Entry(ChartUtil.secondToX(e.time.time / 1000), 5f.toGlucoseValue())
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

    // 解配成功后调用
    fun clearCurrentGlucose() {
        currentSet.clear()
    }

    // 设置当前血糖值后调用
    fun setCurrentGlucose(time: Date?, glucose: Float?) {
        if (time != null && glucose != null && UserInfoManager.shareUserInfo == null) {
            currentSet.clear()
            currentSet.addEntry(
                Entry(
                    ChartUtil.dateToX(time),
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

    private fun calDateMaxMin(dateTime: Date) {
        //记录最小时间
        if (curPageMinDate.time > dateTime.time) {
            curPageMinDate = dateTime
        }
    }

}