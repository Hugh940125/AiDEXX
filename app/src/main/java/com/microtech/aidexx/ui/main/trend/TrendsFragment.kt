package com.microtech.aidexx.ui.main.trend

import android.graphics.Color
import android.graphics.DashPathEffect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseFragment
import com.microtech.aidexx.ble.device.model.GLUCOSE_NUM_ONE_DAY
import com.microtech.aidexx.common.setDebounceClickListener
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.databinding.FragmentTrendBinding
import com.microtech.aidexx.db.repository.CgmCalibBgRepository
import com.microtech.aidexx.ui.main.trend.view.PieChartView
import com.microtech.aidexx.utils.LanguageUnitManager
import com.microtech.aidexx.utils.ThemeManager
import com.microtech.aidexx.utils.ThresholdManager
import com.microtech.aidexx.utils.TimeUtils
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.utils.toGlucoseValue
import com.microtech.aidexx.views.calendar.CalendarDialog
import com.microtech.aidexx.views.dialog.Dialogs
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import kotlin.math.abs
import kotlin.math.ceil

class TrendsFragment : BaseFragment<TrendsViewModel, FragmentTrendBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        updateTrends(Dialogs.DateInfo.dateLastWeek, Dialogs.DateInfo.dateToday)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrendBinding.inflate(layoutInflater)
        binding.rlDateSpace.setDebounceClickListener {
            openCalendar()
        }
        lifecycleScope.launch {
            viewModel.cgatFlow.debounce(3000).collectLatest { trendInfo ->
                trendInfo?.let {
                    binding.tvCoverTimeValue.text = it.coverTime
                    binding.tvMonitorTimesValue.text = it.monitorTimes
                    binding.ehba1cValue.text = it.eHbA1c
                    binding.ivEhba1cTrends.isVisible = it.showEhbA1cTrend
                    binding.tvMbgValue.text = it.mbg
                    binding.ivMbgTrends.isVisible = it.showMbgTrend
                    val normalHolder = buildDataHolder(it.normalPercent.toFloat(), R.color.colorGlucoseNormal)
                    val highHolder = buildDataHolder(it.highPercent.toFloat(), R.color.colorGlucoseHigh)
                    val lowHolder = buildDataHolder(it.lowPercent.toFloat(), R.color.colorGlucoseLow)
                    binding.pieChart.setData(listOf(lowHolder, normalHolder, highHolder))
                    binding.highPercent.text = it.highPercentDisplay
                    binding.descHigh.text = it.highPercentDesc
                    binding.ivHighTrends.isVisible = it.showHighPercentTrend
                    binding.normalPercent.text = it.normalPercentDisplay
                    binding.descNormal.text = it.normalPercentDesc
                    binding.lowPercent.text = it.lowPercentDisplay
                    binding.descLow.text = it.lowPercentDesc
                    binding.ivLowTrends.isVisible = it.showLowPercentTrend
                    binding.txtEmptyPies.isVisible = it.showPieNoData
                    updateAgpChart(it.dailyP50, it.dailyP75, it.dailyP25, it.dailyP90, it.dailyP10)
                }
            }
        }
        return binding.root
    }

    private fun updateAgpChart(
        dailyMean: DoubleArray?,
        daily75: DoubleArray?,
        daily25: DoubleArray?,
        daily90: DoubleArray?,
        daily10: DoubleArray?,
    ) {
        val textColor = ContextCompat.getColor(requireContext(), R.color.gray1)
        binding.agpChart.description.isEnabled = false
        binding.agpChart.legend.isEnabled = false
        binding.agpChart.setTouchEnabled(false)
        binding.agpChart.setBackgroundColor(Color.TRANSPARENT)
        binding.agpChart.setDrawGridBackground(true)
        binding.agpChart.setGridBackgroundColor(0x40989898)
        binding.agpChart.gridBackgroundStart =
            (ThresholdManager.hypo).toGlucoseValue()
        binding.agpChart.gridBackgroundEnd =
            (ThresholdManager.hyper).toGlucoseValue()
        val xAxis = binding.agpChart.xAxis
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawLabels(true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = 24f
        xAxis.textSize = 12f
        xAxis.textColor = textColor
        xAxis.setDrawGridLines(true)
        val lineColor = ThemeManager.getTypeValue(context, R.attr.colorTrendLine)
        xAxis.gridColor = lineColor
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value.toInt() < 10) "0${value.toInt()}:00" else "${value.toInt()}:00"
            }
        }
        val yAxis: YAxis = binding.agpChart.axisLeft
        yAxis.setDrawAxisLine(false)
        yAxis.setDrawGridLines(true)
        yAxis.setDrawLabels(true)
        yAxis.gridColor = lineColor
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        val limitLineLow = LimitLine(
            (ThresholdManager.hypo).toGlucoseValue(), ""
        )
        yAxis.removeAllLimitLines()
        limitLineLow.enableDashedLine(15f, 3f, 0f);
        limitLineLow.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
        limitLineLow.lineColor = ContextCompat.getColor(requireContext(), R.color.colorTrend_1)
        //设置警告线的宽度
        limitLineLow.lineWidth = 0.5f
        yAxis.addLimitLine(limitLineLow)
        val limitLineHigh = LimitLine(
            (ThresholdManager.hyper).toGlucoseValue(), ""
        )
        limitLineHigh.enableDashedLine(15f, 3f, 0f);
        limitLineHigh.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
        limitLineHigh.lineColor = ContextCompat.getColor(requireContext(), R.color.colorTrend_2)
        //设置警告线的宽度
        limitLineHigh.lineWidth = 0.5f
        yAxis.addLimitLine(limitLineHigh)

        when (UnitManager.glucoseUnit) {
            UnitManager.GlucoseUnit.MMOL_PER_L -> {
                yAxis.axisMinimum = 0f
                yAxis.axisMaximum = 20f
            }

            UnitManager.GlucoseUnit.MG_PER_DL -> {
                yAxis.axisMinimum = 0f
                yAxis.axisMaximum = 360f
            }
        }
        val lines = floatArrayOf(20f, 1000f)
        xAxis.setGridDashedLine(DashPathEffect(lines, 0f));
        xAxis.gridLineWidth = 1f
        yAxis.setLabelCount(5, true)
        yAxis.textSize = 12f
        yAxis.textColor = textColor
        binding.agpChart.axisLeft.isEnabled = true
        binding.agpChart.axisRight.isEnabled = false
        val lineData = LineData()
        if (dailyMean != null && daily90 != null && daily10 != null) {
            val p90Entries: MutableList<Entry> = ArrayList()
            for (i in 0..287) {
                p90Entries.add(
                    Entry(
                        24f / 288 * (i.toFloat() + 0.5f),
                        daily90[i].toFloat().toGlucoseValue()
                    )
                )
            }
            val p90DataSet = LineDataSet(p90Entries, "")
            p90DataSet.axisDependency = YAxis.AxisDependency.LEFT
            p90DataSet.setDrawCircles(false)
            val color90 = ThemeManager.getTypeValue(context, R.attr.colorTrendChart90)
            p90DataSet.color = color90
            p90DataSet.lineWidth = 0f
            val p10Entries: MutableList<Entry> = ArrayList()
            for (i in 0..287) {
                p10Entries.add(
                    Entry(
                        24f / 288 * (i.toFloat() + 0.5f),
                        daily10[i].toFloat().toGlucoseValue()
                    )
                )
            }
            val p10DataSet = LineDataSet(p10Entries, "")
            p10DataSet.axisDependency = YAxis.AxisDependency.LEFT
            p10DataSet.setDrawCircles(false)
            p10DataSet.color = resources.getColor(R.color.transparent)
            p10DataSet.lineWidth = 0f
            p90DataSet.setDrawFilled(true)
            p90DataSet.fillColor = color90
            p90DataSet.fillFormatter = object : IFillFormatter {
                override fun getFillLinePosition(
                    dataSet: ILineDataSet?,
                    dataProvider: LineDataProvider?,
                ): Float {
                    return 0f
                }

                override fun getFillLine(
                    dataSet: ILineDataSet?,
                    dataProvider: LineDataProvider?,
                ): ILineDataSet {
                    return p10DataSet
                }
            }
            lineData.addDataSet(p90DataSet)
            lineData.addDataSet(p10DataSet)
        }
        if (dailyMean != null && daily75 != null && daily25 != null) {
            val p75Entries: MutableList<Entry> = ArrayList()
            for (i in 0..287) {
                p75Entries.add(
                    Entry(
                        24f / 288 * (i.toFloat() + 0.5f),
                        daily75[i].toFloat().toGlucoseValue()
                    )
                )
            }
            val p75DataSet = LineDataSet(p75Entries, "")
            p75DataSet.axisDependency = YAxis.AxisDependency.LEFT
            p75DataSet.setDrawCircles(false)
            val color75 = ThemeManager.getTypeValue(context, R.attr.colorTrendChart75)
            p75DataSet.color = color75
            p75DataSet.lineWidth = 0f

            val p25Entries: MutableList<Entry> = ArrayList()
            for (i in 0..287) {
                p25Entries.add(
                    Entry(
                        24f / 288 * (i.toFloat() + 0.5f),
                        daily25[i].toFloat().toGlucoseValue()
                    )
                )
            }
            val p25DataSet = LineDataSet(p25Entries, "")
            p25DataSet.axisDependency = YAxis.AxisDependency.LEFT
            p25DataSet.setDrawCircles(false)
            p25DataSet.color = resources.getColor(R.color.transparent)
            p25DataSet.lineWidth = 0f

            p75DataSet.setDrawFilled(true)
            p75DataSet.fillColor = color75
            p75DataSet.fillAlpha = 100
            p75DataSet.fillFormatter = object : IFillFormatter {
                override fun getFillLinePosition(
                    dataSet: ILineDataSet?,
                    dataProvider: LineDataProvider?,
                ): Float {
                    return 0f
                }

                override fun getFillLine(
                    dataSet: ILineDataSet?,
                    dataProvider: LineDataProvider?,
                ): ILineDataSet {
                    return p25DataSet
                }
            }

            lineData.addDataSet(p75DataSet)
            lineData.addDataSet(p25DataSet)
        }

        if (dailyMean != null) {
            val meanEntries: MutableList<Entry> = ArrayList()
            var b = false
            for (i in 0..287) {
                if (dailyMean[i].isNaN()) {
                    if (b) break else continue
                } else {
                    b = true
                    meanEntries.add(
                        Entry(
                            24f / 288 * (i.toFloat() + 0.5f),
                            dailyMean[i].toFloat().toGlucoseValue()
                        )
                    )
                }
            }
            val meanDataSet = LineDataSet(meanEntries, "")
            meanDataSet.axisDependency = YAxis.AxisDependency.LEFT
            meanDataSet.setDrawCircles(false)
            meanDataSet.color = ThemeManager.getTypeValue(context, R.attr.colorTrendChartMain)
            meanDataSet.lineWidth = 1.6f
            lineData.addDataSet(meanDataSet)
        }

        val combinedData = CombinedData()
        combinedData.setData(lineData)
        binding.agpChart.data = combinedData
        binding.agpChart.invalidate()
    }

    fun buildDataHolder(value: Float, color: Int): PieChartView.PieceDataHolder {
        return PieChartView.PieceDataHolder(
            value,
            ContextCompat.getColor(requireContext(), color),
            "$value%"
        )
    }

    private fun openCalendar() {
        CalendarDialog(requireContext(), { position ->
            var startDate: Date = Dialogs.DateInfo.dateLastWeek!!
            when (position) {
                1 -> startDate = Dialogs.DateInfo.dateLastWeek!!
                2 -> startDate = Dialogs.DateInfo.dateLast14days!!
                3 -> startDate = Dialogs.DateInfo.dateLastMonth!!
            }
            updateTrends(startDate, Dialogs.DateInfo.dateToday)
        }, { startDate, endDate ->
            updateTrends(startDate, endDate)
        }).show()
    }

    private fun updateTrends(startDate: Date?, endDate: Date?) {
        if (startDate == null || endDate == null) {
            return
        }
        val formatter =
            LanguageUnitManager.getCurLanguageConf(requireContext()).dmyFormat
        binding.timeBegin.text = formatter.format(startDate)
        val calendar = Calendar.getInstance()
        calendar.time = endDate
        calendar.add(Calendar.DATE, -1)
        binding.timeEnd.text = formatter.format(calendar.time)
        lifecycleScope.launch {
            val beginTime = startDate.time
            val cgmList =
                CgmCalibBgRepository.queryCgmByPage(startDate, endDate, UserInfoManager.getCurShowUserId())
            val dayCount = ceil((endDate.time - startDate.time) / 1000.0 / 86400.0).toInt()
            val glucoseArray = Array(dayCount) { DoubleArray(GLUCOSE_NUM_ONE_DAY) }
            cgmList?.let {
                it.sortBy { cgm -> cgm.deviceTime }
                var lastHistoryTime = 0L
                var historyCount = 0
                for (dbHistory in cgmList) {
                    val time: Long = dbHistory.deviceTime.time
                    if (abs(time - lastHistoryTime) < 5 * TimeUtils.oneMinuteMillis) {
                        continue
                    }
                    lastHistoryTime = time
                    historyCount++
                    val x = ((time - beginTime) / TimeUtils.oneDayMillis).toInt()
                    val y = ((time - beginTime) / (5 * TimeUtils.oneMinuteMillis)).toInt() - x * GLUCOSE_NUM_ONE_DAY
                    if (x >= dayCount || y > GLUCOSE_NUM_ONE_DAY) {
                        continue
                    }
                    dbHistory.glucose?.let { history ->
                        val glucoseValue = history.toGlucoseValue()
                        if (glucoseValue.toDouble() < 2) {
                            glucoseArray[x][y] = 2.0
                        } else if (glucoseValue.toDouble() > 25) {
                            glucoseArray[x][y] = 25.0
                        } else {
                            glucoseArray[x][y] = glucoseValue.toDouble()
                        }
                    }
                }
                viewModel.runCgat(historyCount, glucoseArray, dayCount)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = TrendsFragment()
    }
}