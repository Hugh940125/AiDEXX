package com.microtech.aidexx.ui.main.history

import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.getContext
import com.microtech.aidexx.common.getEndOfTheDay
import com.microtech.aidexx.common.getStartOfTheDay
import com.microtech.aidexx.common.hourMinute
import com.microtech.aidexx.common.setScale
import com.microtech.aidexx.common.stripTrailingZeros
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.data.EventUnitManager
import com.microtech.aidexx.db.entity.BaseEventEntity
import com.microtech.aidexx.db.entity.BloodGlucoseEntity
import com.microtech.aidexx.db.entity.CalibrateEntity
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import com.microtech.aidexx.db.entity.event.DietEntity
import com.microtech.aidexx.db.entity.event.ExerciseEntity
import com.microtech.aidexx.db.entity.event.InsulinEntity
import com.microtech.aidexx.db.entity.event.MedicationEntity
import com.microtech.aidexx.db.entity.event.OthersEntity
import com.microtech.aidexx.db.repository.CgmCalibBgRepository
import com.microtech.aidexx.db.repository.EventDbRepository
import com.microtech.aidexx.ui.main.history.eventHistory.ChartModel
import com.microtech.aidexx.ui.main.history.eventHistory.CountModel
import com.microtech.aidexx.ui.main.history.eventHistory.ProportionModel
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.ThresholdManager
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.utils.toGlucoseValue
import com.microtech.aidexx.widget.chart.ChartUtil
import com.microtech.aidexx.widget.chart.GlucoseChart
import com.microtech.aidexx.widget.chart.MyChart.Companion.G_ONE_DAY
import com.microtech.aidexx.widget.chart.dataset.BgDataSet
import com.microtech.aidexx.widget.chart.dataset.CalDataSet
import com.microtech.aidexx.widget.chart.dataset.GlucoseDataSet
import com.microtech.aidexx.widget.chart.dataset.IconDataSet
import com.microtech.aidexx.widget.chart.dataset.toChartEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.floor
import kotlin.math.roundToInt

class HistoryViewModel: BaseViewModel() {

    companion object {
        private val TAG = HistoryViewModel::class.java.simpleName
        private const val HIGHEST_GLUCOSE_MG = 25F * 18F
        private const val LOWEST_GLUCOSE_MG = 2F * 18F
        private const val TIP_GLUCOSE_MG = 23F * 18F
    }

    //region 日期相关
    private val _curDate = MutableStateFlow<Date?>(null)
    val curDate = _curDate.asStateFlow()

    fun updateDate(selectedDate: Date) {
        if (selectedDate.time <= System.currentTimeMillis()) {
            viewModelScope.launch {
                _curDate.emit(selectedDate)
            }
        }
    }

    fun toNextDay() = jumpDays(1)
    fun toPreviousDay() = jumpDays(-1)

    private fun jumpDays(days: Int) {
        (_curDate.value ?: Date()).let {
            val instance = Calendar.getInstance()
            instance.timeInMillis = it.time
            instance.add(Calendar.DAY_OF_MONTH, days)
            val date = Date(instance.timeInMillis)
            updateDate(date)
        }
    }
    //endregion

    private val _countModel = MutableStateFlow<CountModel?>(null)
    val countModel = _countModel.asStateFlow()
    private val _proportionModel = MutableStateFlow<ProportionModel?>(null)
    val proportionModel = _proportionModel.asStateFlow()
    private val _recordsModel = MutableStateFlow<List<HistoryDetailModel>?>(null)
    val recordsModel = _recordsModel.asStateFlow()
    private val _chartModel = MutableStateFlow<ChartModel?>(null)
    val chartModel = _chartModel.asStateFlow()
    private val _alertModel = MutableStateFlow<List<HistoryDetailModel>?>(null)
    val alertModel = _alertModel.asStateFlow()

    //region 数据加载
    private suspend fun loadAndCalculateData() {
        withContext(Dispatchers.IO) {
            (_curDate.value ?: Date()).let { curDate ->

                val allData = loadCurDateAllData(curDate.getStartOfTheDay(), curDate.getEndOfTheDay())



                val countDataModel = CountModel()
                val proportionDataModel = ProportionModel()

                if (!::combinedData.isInitialized) {
                    combinedData = CombinedData()
                } else {
                    resetChartData()
                }
                val chartModel = ChartModel(combinedData = combinedData)

                val records = CopyOnWriteArrayList<HistoryDetailModel>()
                var alerts = false to mutableListOf<HistoryDetailModel>()

                allData.map {
                    async {
                        it?.let { list ->

                            list.firstOrNull()?.let { e ->
                                if (UserInfoManager.getCurShowUserId() != e.userId) {
                                    LogUtil.d("用户变了 终止处理 $e", TAG)
                                    return@async
                                }

                                when (e) {
                                    is RealCgmHistoryEntity -> {
                                        chartModel.hasWave = list.size > 2
                                        list.forEach { event ->
                                            calculateForChart(event, chartModel)
                                            calculateForProportion(proportionDataModel, event as RealCgmHistoryEntity)
                                            calculateForAlert(alerts.second, event)
                                        }
                                        calculateCgmWave(chartModel)
                                        calculateForProportion(proportionDataModel, null)
                                        proportionDataModel.dirty = true
                                        _proportionModel.emit(proportionDataModel)

                                        alerts = true to alerts.second
                                        _alertModel.emit(alerts.second)

                                    }
                                    is BloodGlucoseEntity -> {
                                        list.forEach { event ->
                                            calculateForChart(event)
                                            calculateForRecords(records, event)
                                        }
                                    }
                                    is CalibrateEntity -> {
                                        list.forEach { event ->
                                            calculateForChart(event)
                                            calculateForRecords(records, event)
                                        }
                                    }
                                    else -> {
                                        list.forEach { event ->
                                            calculateForCount(countDataModel, event)
                                            calculateForChart(event)
                                            calculateForRecords(records, event)
                                        }
                                        countDataModel.dirty = true
                                        _countModel.emit(countDataModel)
                                    }
                                }
                            }
                        }
                    }
                }.awaitAll()

                if (!countDataModel.dirty)
                    _countModel.emit(countDataModel)

                notifyRefreshChart(chartModel)

                if (!proportionDataModel.dirty)
                    _proportionModel.emit(proportionDataModel)

                _recordsModel.emit(records.sortedByDescending {
                    it.time
                })

                if (alerts.first) _alertModel.emit(alerts.second)
            }
        }
    }

    private suspend fun notifyRefreshChart(chartModel: ChartModel) {
        val lineDataSets: ArrayList<ILineDataSet> = ArrayList()
        lineDataSets.addAll(GlucoseChart.generateLimitLines(xMin(), xMax(), lowerLimit, upperLimit))

        GlucoseChart.formatGlucoseSetAfterInitData(glucoseSets, lowerLimit, upperLimit)
        lineDataSets.addAll(glucoseSets)

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

        chartModel.combinedData = combinedData

        _chartModel.emit(chartModel)

    }

    private fun calculateForCount(
        model: CountModel,
        event: BaseEventEntity,
        plusOrMinus: (Double, Double) -> Double = { a, b -> a.plus(b) }
    ) {
        when (event) {
            is DietEntity -> {
                event.expandList.forEach {detail ->
                    model.carb = plusOrMinus(model.carb ?: 0.0, detail.carbohydrate)
                    model.protein = plusOrMinus(model.protein ?: 0.0, detail.protein)
                    model.fat = plusOrMinus(model.fat ?: 0.0, detail.fat)
                }
            }
            is ExerciseEntity -> {
                event.expandList.forEach {
                    model.exerciseTime = plusOrMinus(
                        model.exerciseTime ?: 0.0,
                        it.quantity * (EventUnitManager.getTimeUnitList()
                            .find { s -> s.code == it.unit }?.ratio ?: 1.0)
                    )
                }
            }
            is MedicationEntity -> model.medicationTimes = plusOrMinus(model.medicationTimes ?: 0.0, 1.toDouble())
            is InsulinEntity -> {
                event.expandList.forEach {
                    model.insulinTotal = plusOrMinus(model.insulinTotal ?: 0.0, it.quantity)
                }
            }
        }
    }

    private fun calculateForChart(event: BaseEventEntity, chartModel: ChartModel? = null) {
        when (event) {
            is RealCgmHistoryEntity -> {
                glucoseSets.last().addEntryOrdered(event.toChartEntry())
                chartModel?.let {
                    //计算波动
                    event.glucose?.let { glucose ->
                        if (it.cgmHighestGlucose == 0f || it.cgmHighestGlucose <= glucose) {
                            it.cgmHighestGlucose = glucose
                            it.cgmHighestTime = event.timestamp
                        }
                        if (it.cgmLowestGlucose == 0f || it.cgmLowestGlucose >= glucose) {
                            it.cgmLowestGlucose = glucose
                            it.cgmLowestTime = event.timestamp
                        }
                    }
                }
            }
            is CalibrateEntity -> calSet.addEntryOrdered(event.toChartEntry())
            is BloodGlucoseEntity -> bgSet.addEntryOrdered(event.toChartEntry())
            else -> eventSet.addEntryOrdered(event.toChartEntry())
        }
    }
    private fun calculateCgmWave(chartModel: ChartModel) {
        if (chartModel.hasWave) {

            val maxGlucose = chartModel.cgmHighestGlucose
            val minGlucose = chartModel.cgmLowestGlucose
            val scale = if (UnitManager.glucoseUnit == UnitManager.GlucoseUnit.MMOL_PER_L) 1 else 0
            val highestValue = maxGlucose.coerceAtMost(HIGHEST_GLUCOSE_MG)
                .toGlucoseValue().setScale(scale)
            val lowestValue = minGlucose.coerceAtLeast(LOWEST_GLUCOSE_MG).
            toGlucoseValue().setScale(scale)

            chartModel.cgmHighestTitleText =
                "${chartModel.cgmHighestTitleText}(${chartModel.cgmHighestTime.hourMinute()})"

            chartModel.cgmHighestText =
                if (maxGlucose > HIGHEST_GLUCOSE_MG) ">$highestValue" else highestValue

            chartModel.cgmLowestTitleText =
                "${chartModel.cgmLowestTitleText}(${chartModel.cgmLowestTime.hourMinute()})"

            chartModel.cgmLowestText =
                if (minGlucose < LOWEST_GLUCOSE_MG) "<$lowestValue" else lowestValue

            val maxWave = (highestValue.toDouble() - lowestValue.toDouble())

            chartModel.cgmWaveText = when {
                maxGlucose <= HIGHEST_GLUCOSE_MG && minGlucose >= LOWEST_GLUCOSE_MG -> maxWave.setScale(scale)

                maxGlucose >= HIGHEST_GLUCOSE_MG && minGlucose < LOWEST_GLUCOSE_MG ->
                    ">${TIP_GLUCOSE_MG.toGlucoseValue().setScale(scale)}"

                maxGlucose > HIGHEST_GLUCOSE_MG && minGlucose >= LOWEST_GLUCOSE_MG ->
                    ">" + maxWave.coerceAtMost(TIP_GLUCOSE_MG.toGlucoseValue().toDouble()).setScale(scale)

                maxGlucose <= HIGHEST_GLUCOSE_MG && minGlucose < LOWEST_GLUCOSE_MG ->
                    ">" + maxWave.coerceAtMost(TIP_GLUCOSE_MG.toGlucoseValue().toDouble()).setScale(scale)

                else -> chartModel.cgmWaveText
            }

        }
    }

    private fun calculateForProportion(model: ProportionModel, event: RealCgmHistoryEntity?) {
        event?.let {e ->
            e.glucose?.let {
                if (it > ThresholdManager.hyper) {
                    model.highCount++
                } else if (it < ThresholdManager.hypo) {
                    model.lowCount++
                } else {
                    model.normalCount++
                }
            }
        } ?:let {
            val totalCount = (model.highCount + model.lowCount + model.normalCount).toDouble()
            if (totalCount > 0) {
                model.totalCount = totalCount.toInt()

                model.highCountPercent = (model.highCount / totalCount * 100).roundToInt()
                model.highCountPercentStr = "${model.highCountPercent}%"

                model.lowCountPercent = (model.lowCount / totalCount * 100).roundToInt()
                model.lowCountPercentStr = "${model.lowCountPercent}%"

                model.normalCountPercent = 100 - model.highCountPercent - model.lowCountPercent
                model.normalCountPercentStr = "${model.normalCountPercent}%"

                val totalTime = totalCount.coerceAtMost(24 * 60.0)
                val lowMinute = model.lowCountPercent / 100.0 * totalTime
                val highMinute = model.highCountPercent / 100.0 * totalTime
                val normalMinute = model.normalCountPercent / 100.0 * totalTime
                model.highCountMinutesStr = formatTimeText(highMinute)
                model.normalCountMinutesStr = formatTimeText(normalMinute)
                model.lowCountMinutesStr = formatTimeText(lowMinute)
            }
        }
    }

    private fun calculateForRecords(records: CopyOnWriteArrayList<HistoryDetailModel>, event: BaseEventEntity) {
        when (event) {
            is BloodGlucoseEntity -> records.add(event.toHistoryDetailModel())
            is DietEntity -> records.add(event.toHistoryDetailModel())
            is ExerciseEntity -> records.add(event.toHistoryDetailModel())
            is MedicationEntity -> records.add(event.toHistoryDetailModel())
            is InsulinEntity -> records.add(event.toHistoryDetailModel())
            is OthersEntity -> records.add(event.toHistoryDetailModel())
        }
    }

    private fun calculateForAlert(records: MutableList<HistoryDetailModel>, event: RealCgmHistoryEntity) {
        if (event.eventWarning in (1..2)) {
            records.add(event.toHistoryDetailModel())
        }
    }

    private suspend fun loadCurDateAllData(startDate: Date, endDate: Date) =
        withContext(Dispatchers.IO) {
            listOf(
                async { CgmCalibBgRepository.queryCgmByPage(startDate, endDate) },
                async { CgmCalibBgRepository.queryBgByPage(startDate, endDate) },
                async { CgmCalibBgRepository.queryCalByPage(startDate, endDate) },
                async { EventDbRepository.queryEventByPage(startDate, endDate) }
            ).awaitAll()
        }

    private fun formatTimeText(minute: Double): String {
        return if (minute < 60) {
            minute.stripTrailingZeros(0) + getContext().getString(R.string.unit_min)
        } else {
            val hourString = floor(minute / 60).stripTrailingZeros() + getContext().getString(R.string.unit_hour)
            val minuteString = if (minute % 60 > 0) {
                floor(minute % 60).stripTrailingZeros() + getContext().getString(R.string.min)
            } else {
                ""
            }
            return hourString + minuteString
        }
    }

    //endregion


    // region 图表相关

    private lateinit var combinedData: CombinedData
    private val glucoseSets: MutableList<LineDataSet> = CopyOnWriteArrayList()
    private val calSet = CalDataSet()
    private val bgSet = BgDataSet()
    private val eventSet = IconDataSet()

    private fun resetChartData() {
        bgSet.clear()
        eventSet.clear()
        glucoseSets.last().clear()
        calSet.clear()
        combinedData.lineData.clearValues()
        combinedData.scatterData.clearValues()
        combinedData.clearValues()
    }

    fun xRange(): Float {
        return G_ONE_DAY * GlucoseChart.CHART_LABEL_COUNT.toFloat()
    }

    fun xMargin(): Float {
        return G_ONE_DAY / 2f
    }

    fun xMin(): Float {
        return xMax() - xRange()
    }

    fun xMax(): Float {
        return ChartUtil.secondToX((_curDate.value ?: Date()).getEndOfTheDay().time / 1000) //+ xMargin()
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

    //endregion

    suspend fun onDelete(event: BaseEventEntity) {

        val countModel = _countModel.value?.copy()?.also {
            it.id = System.currentTimeMillis()
        }
        val minus:(Double, Double)->Double = { a, b -> a.minus(b)}
        countModel?.let {
            calculateForCount(it, event, minus)
            _countModel.emit(it)
        }

        when (event) {
            is DietEntity,
            is ExerciseEntity,
            is MedicationEntity,
            is InsulinEntity,
            is OthersEntity -> eventSet.removeEntry(event.toChartEntry())
            is BloodGlucoseEntity -> bgSet.removeEntry(event.toChartEntry())
        }
        _chartModel.emit(_chartModel.value?.copy().also {
            it?.id = System.currentTimeMillis()
        })
    }

    fun refresh() {
        viewModelScope.launch {
            loadAndCalculateData()
        }
    }

    init {
        glucoseSets.add(GlucoseDataSet())
        viewModelScope.launch {
            _curDate.debounce(300).collectLatest {
                it?.let { loadAndCalculateData() }
            }
        }
    }

}