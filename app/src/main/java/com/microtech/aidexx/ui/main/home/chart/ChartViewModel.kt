package com.microtech.aidexx.ui.main.home.chart

import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.microtech.aidexx.R
import com.microtech.aidexx.common.getContext
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.entity.BloodGlucoseEntity
import com.microtech.aidexx.db.entity.CalibrateEntity
import com.microtech.aidexx.db.entity.DietEntity
import com.microtech.aidexx.db.entity.EventEntity
import com.microtech.aidexx.db.entity.ExerciseEntity
import com.microtech.aidexx.db.entity.InsulinEntity
import com.microtech.aidexx.db.entity.MedicationEntity
import com.microtech.aidexx.db.entity.OthersEntity
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import com.microtech.aidexx.db.repository.CgmCalibBgRepository
import com.microtech.aidexx.utils.CalibrateManager
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.LogUtils
import com.microtech.aidexx.utils.ThemeManager
import com.microtech.aidexx.utils.ThresholdManager
import com.microtech.aidexx.utils.TimeUtils
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.utils.eventbus.BgDataChangedInfo
import com.microtech.aidexx.utils.eventbus.CgmDataChangedInfo
import com.microtech.aidexx.utils.eventbus.DataChangedType
import com.microtech.aidexx.utils.toGlucoseValue
import com.microtech.aidexx.widget.chart.ChartUtil
import com.microtech.aidexx.widget.chart.GlucoseChart.Companion.CHART_LABEL_COUNT
import com.microtech.aidexx.widget.chart.MyChart.ChartGranularityPerScreen
import com.microtech.aidexx.widget.chart.MyChart.Companion.G_SIX_HOURS
import com.microtech.aidexx.widget.chart.dataset.BgDataSet
import com.microtech.aidexx.widget.chart.dataset.CalDataSet
import com.microtech.aidexx.widget.chart.dataset.CurrentGlucoseDataSet
import com.microtech.aidexx.widget.chart.dataset.GlucoseDataSet
import com.microtech.aidexx.widget.chart.dataset.IconDataSet
import com.microtechmd.blecomm.constant.History
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs

class ChartViewModel: ViewModel() {

    // 国际版代码先放这里 后面做打包渠道配置
    private val isGp = false

    /**
     * 图表数据总集
     */
    private lateinit var combinedData: CombinedData

    private val currentSet = CurrentGlucoseDataSet()
    private val glucoseSets: MutableList<LineDataSet> = CopyOnWriteArrayList()
    private val calSet = CalDataSet()
    private val bgSet = BgDataSet()
    private val eventSet = IconDataSet()

    private var timeMin: Float? = null
    private var timeMax: Float? = null

    @Volatile
    private var isDataInit = false

    class ChartChangedInfo(
        var timeMin: Float?,
        var needScrollToLatest: Boolean
    )

    private val mGranularityFlow = MutableStateFlow<Int?>(null)
    private fun getGranularity(): Int = mGranularityFlow.value ?: G_SIX_HOURS
    val granularityFlow = mGranularityFlow.asStateFlow()

    /** 启动加载下一页任务 */
    val startLoadNextPage = MutableStateFlow(false)
    val startApplyNextPageData = MutableStateFlow(false)

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
            launch {
                startLoadNextPage.collect {
                    if (it) {
                        LogUtil.d("===CHART===开始加载下一页")
                        var needNotify: Boolean
                        withContext(Dispatchers.IO) {

                            LogUtil.d("===CHART=== 当前数据最小日期$curPageMinDate")
                            var maxTime = curPageMinDate // ob between是前闭后闭
                            //获取当前最小日期前的最近一条数据时间 因为可能有断层 导致图表无法滚动
                            val latestOne = CgmCalibBgRepository.queryCgmLatestOne(
                                UserInfoManager.getCurShowUserId(),
                                maxTime
                            )
                            maxTime = latestOne?.let { rche ->
                                rche.deviceTime
                            } ?: maxTime

                            val curMinTime = getCurPageStartDate(curPageMinDate.time)
                            LogUtil.d("===CHART=== timeMin=$timeMin start=${curMinTime} end=${maxTime}")

                            needNotify = loadNextPageData(curMinTime, maxTime, false)

                            LogUtil.d("===CHART=== 加载之后 timeMin=$timeMin start=${curMinTime} end=${maxTime}")
                        }
                        // 如果这个期间没发生用户切换就外部
                        if (needNotify) {
//                        mDataChangedFlow.emit(ChartChangedInfo(timeMin, false))
                        } else {
                            LogUtil.e("===CHART=== 分页查询结束 发现换人了 终止通知")
                        }
                        //重置标记
                        val ret = startLoadNextPage.compareAndSet(true, update = false)
                        LogUtil.d("===CHART=== 通知及标记重置了 ret=$ret")
                    }
                }
            }

            launch {
                startApplyNextPageData.collect {
                    if (it) {
                        var needNotify = false
                        if (nextPageCgmData.isNotEmpty()) {
                            LogUtil.d("===CHART=== 下一页数据 size=${nextPageCgmData.size}")
                            withContext(Dispatchers.IO) {
                                updateGlucoseSets(nextPageCgmData)
                                nextPageCgmData.clear()
                                needNotify = true
                            }
                        }
                        if (needNotify) {
                            LogUtil.d("===CHART=== 下一页数据已添加到图表待刷新")
                            mDataChangedFlow.emit(ChartChangedInfo(timeMin, false))
                        }
                        //重置标记
                        val ret = startApplyNextPageData.compareAndSet(true, update = false)
                        LogUtil.d("===CHART=== 应用分页数据标记重置 ret=$ret")
                    }
                }
            }

        }
    }

    /**
     * 首次 加载第一页数据
     */
    fun initData(needReloadData: Boolean = false) = flow {

        if (::combinedData.isInitialized && !needReloadData) {
            emit(combinedData)
            return@flow
        }
        if (!::combinedData.isInitialized) {
            combinedData = CombinedData()
        }
        val latestOne = CgmCalibBgRepository.queryCgmLatestOne(
            UserInfoManager.getCurShowUserId(),
            Date()
        ) //"f03550ef07a7b2164f06deaef597ce37"
        val cgmMaxDate = latestOne?.let {
            it.deviceTime
        } ?: Date()

        val minDate = getCurPageStartDate(cgmMaxDate.time)

        // 加载所有该日期区间的数据
        loadNextPageData(minDate, Date())

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

        combinedData.setData(LineData(lineDataSets).also {
            // 不需要渲染数值及icon 提升性能
            it.dataSets.forEach { ld ->
                ld.setDrawIcons(false)
                ld.setDrawValues(false)
            }
        })
        combinedData.setData(ScatterData(scatterDataSets))

        if (timeMax == null) {
            timeMax = ChartUtil.dateToX(Date())
        }
        if (timeMin == null) {
            timeMin = ChartUtil.dateToX(
                Date(Date().time - getGranularity() * 6 * TimeUtils.oneHourSeconds * 1000))
        }

        emit(combinedData)
        isDataInit = true

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
        if (!isDataInit) return
        reset()
        viewModelScope.launch {
            initData(true).collect {
                mDataChangedFlow.emit(ChartChangedInfo(timeMin, true))
            }
        }
    }

    /** 是否需要加载下一页 */
    fun needLoadNextPage(isLtr: Boolean, visibleLeftX: Float, xAxisMin: Float): Boolean {

        if (isLtr || !isDataInit) return false
//        LogUtil.d("===CHART=== 滚动过程已经触发了下一页加载 不再触发 loadedmin=$loadedMinDate xAxisMin=$xAxisMin vf=$visibleLeftX" )
        if(loadedMinDate == xAxisMin) {
//            LogUtil.d("===CHART=== 滚动过程已经触发了下一页加载 不再触发")
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
                                && it.deviceTime.time > curXMinTimeMillis()
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

    /**
     * 外部有数据变动通知过来后进行清洗合并
     */
    suspend fun onBgDataChanged(data: BgDataChangedInfo) {
        withContext(Dispatchers.IO) {
            when (data.first) {
                DataChangedType.ADD -> {
                    val rets = data.second.filter {
                        it.testTime.time >= curXMinTimeMillis()
                    }
                    if (rets.isNotEmpty()) {
                        addBgSet(rets)
                        mDataChangedFlow.emit(ChartChangedInfo(timeMin, false))
                    }
                }
                else -> {}
            }
        }
    }

    private val nextPageCgmData = mutableListOf<RealCgmHistoryEntity>()
    /**
     * 加载下一页数据到图表集合
     * @return false-正在加载时 发现切换用户了
     */
    private suspend fun loadNextPageData(startDate: Date, endDate: Date, needApply: Boolean = true): Boolean =
        withContext(Dispatchers.IO) {
            timeMin = ChartUtil.dateToX(startDate)
            var isSuccess = true

            val cgmDataTask = async {
                getCgmPageData(startDate, endDate)?.let { d ->
                    if (d.size > 0 && d[0].authorizationId != UserInfoManager.getCurShowUserId()) {
                        isSuccess = false
                    } else {
                        if (needApply) {
                            updateGlucoseSets(d)
                        } else {
                            LogUtil.d("===CHART=== 下一页数据 size=${d.size} 准备完毕")
                            if (d.size > 2) {
                                LogUtil.d("===CHART=== 下一页数据 first=${d.first().deviceTime} last=${d.last().deviceTime}")
                            }

                            nextPageCgmData.addAll(d)
                        }
                    }
                }
            }

            val bgDataTask = async {
                CgmCalibBgRepository.queryBgByPage(startDate, endDate)?.let { d ->
                    if (d.size > 0 && d[0].authorizationId != UserInfoManager.getCurShowUserId()) {
                        isSuccess = false
                    } else {
                        addBgSet(d)
                    }
                }
            }
            // todo 分页加载其他事件相关数据
            awaitAll(cgmDataTask, bgDataTask)
            isSuccess
        }


    /**
     * 当前x轴显示的最小的日期 毫秒
     */
    private fun curXMinTimeMillis() = ChartUtil.xToSecond(timeMin?:0f) * 1000

    private suspend fun getCgmPageData(startDate: Date, endDate: Date) =
        CgmCalibBgRepository.queryCgmByPage(
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

    var upperLimit = ThresholdManager.DEFAULT_HYPER.toGlucoseValue()
        get() {
            return ThresholdManager.hyper.toGlucoseValue()
        }
        private set
    var lowerLimit = ThresholdManager.DEFAULT_HYPO.toGlucoseValue()
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

    // todo 解配成功后调用
    fun clearCurrentGlucose() {
        currentSet.clear()
    }

    /**
     * 切换用户重置数据
     */
    private fun reset() {
        //重置数据
        clearEventSets()
        clearGlucoseSets()
        clearCurrentGlucose()
        combinedData.lineData.clearValues()
        combinedData.scatterData.clearValues()
        combinedData.clearValues()

        //重置分页标记
        val ret = startLoadNextPage.compareAndSet(true, update = false)
        loadedMinDate = -Float.MAX_VALUE
        curPageMinDate = Date()

    }

    /**
     * 新增更新血糖数据
     */
    private suspend fun updateGlucoseSets(cgmHistories: List<RealCgmHistoryEntity>) {
        if (glucoseSets.isEmpty()) glucoseSets.add(GlucoseDataSet())
        if (cgmHistories.isEmpty()) {
            return
        }

        loop@ for (history in cgmHistories) {
            when (history.eventType) {
                History.HISTORY_GLUCOSE, History.HISTORY_GLUCOSE_RECOMMEND_CAL -> {
                    if (history.glucose == null || history.eventWarning == -1) continue@loop
                    val dateTime = ChartUtil.dateToX(history.deviceTime)
                    val entry = Entry(dateTime, history.glucose!!.toFloat().toGlucoseValue())
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
            val all: MutableList<CalibrateEntity> = CalibrateManager.getCalibrateHistorys()
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
                && (cgm.glucose != null && cgm.eventWarning != -1)

    /**
     * 新增指血数据
     */
    private fun addBgSet(bgs: List<BloodGlucoseEntity>) {
        for (bg in bgs) {
            val dateTime = ChartUtil.dateToX(bg.testTime)
            val entry = Entry(dateTime, getGlucoseValue(bg))
            entry.data = bg
            entry.icon = BgDataSet.icon
            bgSet.addEntryOrdered(entry)
            xMaxMin(dateTime)
            calDateMaxMin(bg.testTime)
        }
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
        if (history.glucose != null) {
            val dateTime = ChartUtil.dateToX(history.deviceTime)
            val bg = BloodGlucoseEntity(history.deviceTime, history.glucose!!.toFloat())
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
    private fun updateGpCalibrationSet(history: CalibrateEntity) {
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