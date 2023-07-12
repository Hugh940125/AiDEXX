package com.microtech.aidexx.ui.main.trend

import android.graphics.Color
import android.graphics.DashPathEffect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
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
import com.microtech.aidexx.common.formatToYMd
import com.microtech.aidexx.common.millisToMinutes
import com.microtech.aidexx.common.setDebounceClickListener
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.databinding.FragmentTrendBinding
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import com.microtech.aidexx.ui.main.trend.view.PieChartView
import com.microtech.aidexx.utils.ThemeManager
import com.microtech.aidexx.utils.ThresholdManager
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.utils.toGlucoseValue
import com.microtech.aidexx.views.calendar.CalendarDialog
import com.microtech.aidexx.views.dialog.Dialogs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.util.Calendar
import java.util.Date
import kotlin.math.abs
import kotlin.math.roundToInt

class TrendsFragment : BaseFragment<TrendsViewModel, FragmentTrendBinding>(), OnClickListener {
    var rangeChanged = true
    var userIdCurrentShow = UserInfoManager.instance().userId()
    var currentStartDate = Dialogs.DateInfo.dateLastWeek!!
    var currentEndDate = Dialogs.DateInfo.dateToday!!
    private var oneDigitFormat: DecimalFormat = DecimalFormat("0.0")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        currentStartDate = Dialogs.DateInfo.dateLastWeek!!
        currentEndDate = Dialogs.DateInfo.dateToday!!
        binding.trendRefreshLayout.autoRefresh()
        rangeChanged = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrendBinding.inflate(layoutInflater)
        binding.rlDateSpace.setDebounceClickListener {
            openCalendar()
        }
        initView()
        return binding.root
    }

    private fun initView() {
        binding.trendRefreshLayout.setOnRefreshListener {
            updateTrends()
        }
        binding.txtTitleGlucose.setOnClickListener(this)
        binding.tvAverage.setOnClickListener(this)
        binding.txtTitleTir.setOnClickListener(this)
        binding.txtTitleAgp.setOnClickListener(this)
        binding.txtTitleLBGI.setOnClickListener(this)
        binding.expandableGrid.onDataChange = { mutableList: MutableList<MultiDayBgItem> ->
            lifecycleScope.launch {
                updateMultiChart(mutableList)
            }
        }
        lifecycleScope.launch {
            viewModel.cgatFlow.collectLatest { trendInfo ->
                binding.trendRefreshLayout.finishRefresh()
                trendInfo?.let {
                    binding.tvCoverTimeValue.text = it.coverTime
                    binding.tvMonitorTimesValue.text = it.monitorTimes
                    binding.ehba1cValue.text = it.eHbA1c
                    binding.ivEhba1cTrends.isVisible = it.showEhbA1cTrend
                    binding.tvMbgValue.text = it.mbg
                    binding.tvMbgUnit.text = UnitManager.glucoseUnit.text
                    binding.ivMbgTrends.isVisible = it.showMbgTrend
                    val normalHolder =
                        buildDataHolder(it.normalPercent.toFloat(), R.color.colorGlucoseNormal)
                    val highHolder =
                        buildDataHolder(it.highPercent.toFloat(), R.color.colorGlucoseHigh)
                    val lowHolder =
                        buildDataHolder(it.lowPercent.toFloat(), R.color.colorGlucoseLow)
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
                    binding.cursorView.setValue(oneDigitFormat.format(it.lbgi).toFloat())
                    it.multiDayHistory?.let { histories ->
                        binding.expandableGrid.refreshData(histories)
                    }
                    updateMultiChart(binding.expandableGrid.getDataSet())
                }
            }
        }
    }

    private suspend fun updateMultiChart(mutableList: MutableList<MultiDayBgItem>) {
        val textColor = ContextCompat.getColor(requireContext(), R.color.gray1)
        binding.chartMulti.description.isEnabled = false
        binding.chartMulti.legend.isEnabled = false
        binding.chartMulti.setTouchEnabled(false)

        binding.chartMulti.setBackgroundColor(Color.TRANSPARENT)
        binding.chartMulti.setDrawGridBackground(true)
        binding.chartMulti.setGridBackgroundColor(Color.parseColor("#194D913C"))
        binding.chartMulti.gridBackgroundStart =
            (ThresholdManager.hypo).toGlucoseValue()
        binding.chartMulti.gridBackgroundEnd =
            (ThresholdManager.hyper).toGlucoseValue()
        val xAxis = binding.chartMulti.xAxis
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawLabels(true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = 2400f
        xAxis.textSize = 12f
        xAxis.textColor = textColor
        xAxis.setDrawGridLines(true)
        val lineColor = ThemeManager.getTypeValue(context, R.attr.colorTrendLine)
        xAxis.gridColor = lineColor
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val fl = (value / 100).toInt()
                return if (fl > 9) "$fl:00" else "0$fl:00"
            }
        }
        val yAxis: YAxis = binding.chartMulti.axisLeft
        yAxis.setDrawAxisLine(false)
        yAxis.setDrawGridLines(true)
        yAxis.setDrawLabels(true)
        yAxis.gridColor = lineColor
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        when (UnitManager.glucoseUnit) {
            UnitManager.GlucoseUnit.MMOL_PER_L -> {
                yAxis.axisMinimum = 0f
                yAxis.axisMaximum = 25f
            }

            UnitManager.GlucoseUnit.MG_PER_DL -> {
                yAxis.axisMinimum = 0f
                yAxis.axisMaximum = 450f
            }
        }
        val lines = floatArrayOf(20f, 1000f)
        xAxis.setGridDashedLine(DashPathEffect(lines, 0f));
        xAxis.gridLineWidth = 1f
        yAxis.setLabelCount(5, true)
        yAxis.textSize = 12f
        yAxis.textColor = textColor
        binding.chartMulti.axisLeft.isEnabled = true
        binding.chartMulti.axisRight.isEnabled = false
        val lineData = LineData()
        withContext(Dispatchers.IO) {
            for (item in mutableList) {
                if (item.histories.isNullOrEmpty()) {
                    continue
                }
                addLineData(item.histories, lineData, item.color)
            }
        }
        val combinedData = CombinedData()
        combinedData.setData(lineData)
        binding.chartMulti.data = combinedData
        binding.chartMulti.invalidate()
    }

    private fun addLineData(histories: List<RealCgmHistoryEntity>?, lineData: LineData, color: Int) {
        val instance = Calendar.getInstance()
        if (!histories.isNullOrEmpty()) {
            var entries: MutableList<Entry> = mutableListOf()
            var lastHistoryTime = 0L
            instance.time = Date(histories[0].timestamp)
            instance.set(
                instance.get(Calendar.YEAR),
                instance.get(Calendar.MONTH),
                instance.get(Calendar.DAY_OF_MONTH),
                0,
                0,
                0
            )
            for ((index, history) in histories.withIndex()) {
                val gap = (history.timestamp - instance.time.time).millisToMinutes()
                if (history.glucose == null) {
                    continue
                }
                val entry = Entry(
                    2400 * gap / 1440F,
                    if (UnitManager.glucoseUnit.index == 1) history.glucose!! else history.glucose!!.toGlucoseValue()
                )
                if (abs(history.timestamp - lastHistoryTime).millisToMinutes() > 10 && index != 0) {
                    val dataSet = LineDataSet(entries, "")
                    dataSet.setDrawCircles(false)
                    dataSet.axisDependency = YAxis.AxisDependency.LEFT
                    dataSet.lineWidth = 1.6f
                    dataSet.color = color
                    dataSet.setDrawIcons(false)
                    dataSet.setDrawValues(false)
                    dataSet.isHighlightEnabled = false
                    lineData.addDataSet(dataSet)
                    entries = mutableListOf()
                }
                entries.add(entry)
                lastHistoryTime = history.timestamp
            }
            if (entries.isNotEmpty()) {
                val set = LineDataSet(entries, "")
                set.setDrawCircles(false)
                set.lineWidth = 1.6f
                set.color = color
                set.setDrawIcons(false)
                set.setDrawValues(false)
                set.isHighlightEnabled = false
                set.axisDependency = YAxis.AxisDependency.LEFT
                lineData.addDataSet(set)
            }
        }
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
                        if (UnitManager.glucoseUnit.index == 0) daily90[i].toFloat() else daily90[i].toFloat() * 18
                    )
                )
            }
            val p90DataSet = LineDataSet(p90Entries, "")
            p90DataSet.setDrawIcons(false)
            p90DataSet.setDrawValues(false)
            p90DataSet.isHighlightEnabled = false
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
                        if (UnitManager.glucoseUnit.index == 0) daily10[i].toFloat() else daily10[i].toFloat() * 18
                    )
                )
            }
            val p10DataSet = LineDataSet(p10Entries, "")
            p10DataSet.setDrawIcons(false)
            p10DataSet.setDrawValues(false)
            p10DataSet.isHighlightEnabled = false
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
                        if (UnitManager.glucoseUnit.index == 0) daily75[i].toFloat() else daily75[i].toFloat() * 18
                    )
                )
            }
            val p75DataSet = LineDataSet(p75Entries, "")
            p75DataSet.setDrawIcons(false)
            p75DataSet.setDrawValues(false)
            p75DataSet.isHighlightEnabled = false
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
                        if (UnitManager.glucoseUnit.index == 0) daily25[i].toFloat() else daily25[i].toFloat() * 18
                    )
                )
            }
            val p25DataSet = LineDataSet(p25Entries, "")
            p25DataSet.setDrawIcons(false)
            p25DataSet.setDrawValues(false)
            p25DataSet.isHighlightEnabled = false
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
                            if (UnitManager.glucoseUnit.index == 0) dailyMean[i].toFloat() else dailyMean[i].toFloat() * 18
                        )
                    )
                }
            }
            val meanDataSet = LineDataSet(meanEntries, "")
            meanDataSet.setDrawIcons(false)
            meanDataSet.setDrawValues(false)
            meanDataSet.isHighlightEnabled = false
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

    private fun buildDataHolder(value: Float, color: Int): PieChartView.PieceDataHolder {
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
                2 -> {
                    startDate = Dialogs.DateInfo.dateLast14days!!
                    rangeChanged = true
                }

                3 -> {
                    startDate = Dialogs.DateInfo.dateLastMonth!!
                    rangeChanged = true
                }
            }
            currentStartDate = startDate
            currentEndDate = Dialogs.DateInfo.dateToday!!
            binding.trendRefreshLayout.autoRefresh()
        }, { startDate, endDate ->
            currentStartDate = startDate
            currentEndDate = endDate
            binding.trendRefreshLayout.autoRefresh()
        }).show()
    }

    private fun updateTrends() {
        userIdCurrentShow = UserInfoManager.getCurShowUserId()
        binding.timeBegin.text = currentStartDate.formatToYMd()
        val calendar = Calendar.getInstance()
        calendar.time = currentEndDate
        calendar.add(Calendar.DATE, -1)
        binding.timeEnd.text = calendar.time.formatToYMd()
        lifecycleScope.launch {
            viewModel.runCgat(currentStartDate, currentEndDate)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = TrendsFragment()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.txt_title_glucose -> {
                Dialogs.showMessage(
                    requireContext(),
                    "eHbA1c",
                    getString(R.string.content_about_glucose)
                )
            }

            R.id.tv_average -> {
                Dialogs.showMessage(
                    requireContext(),
                    "MBG",
                    getString(
                        R.string.content_about_average,
                        if (UnitManager.glucoseUnit.index == 0) "4.3–6.6mmol/L" else "${(4.3 * 18).roundToInt()}–${(6.6 * 18).roundToInt()}mg/dL"
                    )
                )
            }

            R.id.txt_title_tir -> {
                val range1 =
                    if (UnitManager.glucoseUnit.index == 0) "3.9-10.0mmol/L" else "${(3.9 * 18).roundToInt()}-${(10.0 * 18).roundToInt()}mg/dL"
                val range2 =
                    if (UnitManager.glucoseUnit.index == 0) "3.9-7.8mmol/L" else "${(3.9 * 18).roundToInt()}-${(7.8 * 18).roundToInt()}mg/dL"
                Dialogs.showMessage(
                    requireContext(),
                    "TIR",
                    String.format(
                        getString(R.string.dialog_content_tir), range1, range2
                    )
                )
            }

            R.id.txt_title_agp -> {
                Dialogs.showMessage(
                    requireContext(),
                    "AGP", getString(R.string.dialog_content_agp)
                )
            }

            R.id.txt_title_LBGI -> {
                Dialogs.showMessage(
                    requireContext(),
                    "LBGI", getString(R.string.dialog_content_lgbi)
                )
            }
        }
    }
}