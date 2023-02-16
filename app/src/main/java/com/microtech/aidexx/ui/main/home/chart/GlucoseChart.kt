//package com.microtech.aidexx.ui.home.chart
//
//import android.content.Context
//import android.graphics.Color
//import android.graphics.DashPathEffect
//import android.util.AttributeSet
//import android.view.View
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.RelativeLayout
//import android.widget.TextView
//import androidx.appcompat.widget.ThemeUtils
//import com.github.mikephil.charting.components.XAxis
//import com.github.mikephil.charting.components.YAxis
//import com.github.mikephil.charting.data.*
//import com.github.mikephil.charting.formatter.ValueFormatter
//import com.github.mikephil.charting.highlight.Highlight
//import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
//import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
//import com.github.mikephil.charting.listener.OnChartValueSelectedListener
//import com.jeremyliao.liveeventbus.LiveEventBus
//import com.microtech.aidexx.R
//import com.microtech.aidexx.utils.ThemeManager
//import com.microtech.aidexx.utils.UnitManager
//import com.microtechmd.cgms.R
//import com.microtechmd.cgms.chart.dataset.GlucoseDataSet
//import com.microtechmd.cgms.chart.uitl.XAxisUtils
//import com.microtechmd.cgms.constants.EventKey
//import com.microtechmd.cgms.entity.objectbox.event.EventEntity
//import com.microtechmd.cgms.manager.ChartManager
//import com.microtechmd.cgms.manager.LanguageUnitManager
//import com.microtechmd.cgms.manager.UnitManager
//import com.microtechmd.cgms.manager.UserManager
//import com.microtechmd.cgms.util.ThemeUtils
//import com.microtechmd.cgms.util.TimeUtils
//import com.microtechmd.cgms.util.dateAndTimeHour
//import com.microtechmd.cgms.util.toGlucoseString2
//import com.microtechmd.cgms.widget.chart.MyChart
//import java.nio.charset.Charset
//import java.text.SimpleDateFormat
//import java.util.*
//
//
///**
// * APP-SRC-A-3-2-1
// */
//class GlucoseChart : MyChart {
//    var rlDescription: RelativeLayout? = null
//    private var isValueNull: Boolean = true
//    var llValue: LinearLayout? = null
//    var outerDescriptionUnit: TextView? = null
//    private val chartManager = ChartManager.instance()
//
//    private var labelCount: Int = 6
//
//    var outerDescriptionX: TextView? = null
//    var outerDescriptionY: TextView? = null
//    var outerDescriptionU: TextView? = null
//    var outerDescriptionView: View? = null
//    var goToHistory: ImageView? = null
//    var textColor: Int? = null
//    var yXAxisPosition = 0
//    var onGoToHistory: (() -> Unit)? = null
//
//    constructor(context: Context?) : super(context)
//    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
//
//        val tArray = context!!.obtainStyledAttributes(
//            attrs,
//            R.styleable.GlucoseChart
//        )
//        yXAxisPosition = tArray.getInt(R.styleable.GlucoseChart_yAxis, 0)
//        initChartAxisX()
//        initChartAxisY()
//        tArray.recycle()
//    }
//
//    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
//        context,
//        attrs,
//        defStyle
//    )
//
//    init {
//        textColor = ThemeManager.getTypeValue(context, R.attr.colorChartText)
//        initBackground()
////        initChartAxisX()
////        initChartAxisY()
//        minOffset = 0f
//        extraTopOffset = 10f
//        extraBottomOffset = 21f
//        extraLeftOffset = 10f
//        extraRightOffset = 10f
//        mMaxVisibleCount = 2000
//
//        description.isEnabled = false
//        description.textColor = textColor!!
//        description.textSize = 20f
//        legend.isEnabled = false
//
////        chart.setClipDataToContent(false)
//        setScaleEnabled(false)
//        setTouchEnabled(true)
//        isDragXEnabled = true
//        isDragYEnabled = false
//        isDragDecelerationEnabled = false //继续滚动
//        isHighlightPerDragEnabled = false
//        isHighlightPerTapEnabled = false
//        maxHighlightDistance = 10f
//
//        enableAutoScaleY = true
//        when (UnitManager.glucoseUnit) {
//            UnitManager.GlucoseUnit.MMOL_PER_L -> yMaximums.addAll(listOf(20f))
//            UnitManager.GlucoseUnit.MG_PER_DL -> yMaximums.addAll(listOf(400f))
//        }
//
//        setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
//            private val formatSD = LanguageUnitManager.languageUnitByIndex(context).homeDateFormat
//            override fun onValueSelected(e: Entry, h: Highlight) {
//                outerDescriptionX?.text = formatSD.format(XAxisUtils.xToSecond(h.x) * 1000)
//                if (ThemeUtils.isLight()) {
//                    outerDescriptionView?.setBackgroundResource(com.microtechmd.cgms.R.drawable.bg_desc_light)
//                } else {
//                    outerDescriptionView?.setBackgroundResource(com.microtechmd.cgms.R.drawable.bg_desc_dark)
//                }
//                if (e.data == null) {
//                    outerDescriptionY?.text = buildString {
//                        append(h.y.toGlucoseString2())
//                    }
//                    outerDescriptionUnit?.text = UnitManager.glucoseUnit.text
//                    isValueNull = false
//                } else {
//                    outerDescriptionY?.text = "--"
//                    outerDescriptionUnit?.text = UnitManager.glucoseUnit.text
//                    isValueNull = true
//                }
//                outerDescriptionView?.visibility = VISIBLE
//            }
//
//            override fun onNothingSelected() {
//                outerDescriptionUnit?.text = ""
//                outerDescriptionX?.text = ""
//                outerDescriptionY?.text = ""
//                outerDescriptionUnit?.text = ""
//                outerDescriptionView?.visibility = GONE
//                goToHistory?.visibility = View.GONE
//            }
//        })
//
//        onSelectX = { x ->
//            var text = ""
//            if (x != null && data.scatterData != null) {
//                val list: MutableList<Entry> = ArrayList()
//                for (dataSet in data.scatterData.dataSets) {
//                    list.addAll(dataSet.getEntriesForXRange(x - 0.5f, x + 0.5f))
//                }
//                if (list.size > 5) {
//                    goToHistory?.visibility = View.VISIBLE
//                    goToHistory?.setOnClickListener {
//                        onGoToHistory?.invoke()
//                    }
//                } else {
//                    goToHistory?.visibility = View.GONE
//                }
//                for ((index, e) in list.withIndex()) {
//                    val data = e.data as EventEntity
//                    if (index > 4) {
//                        text += buildString {
//                            append("\n ...")
//                        }
//                        break
//                    } else {
//                        var itemStr = buildString {
//                            if (text.isNotEmpty()) append("\n")
//                        } + data.time.dateAndTimeHour() + buildString {
//                            append(" ")
//                        } + data.getEventDescription(
//                            resources
//                        ) + buildString {
//                            append("")
//                        } + data.getValueDescription(
//                            resources
//                        )
//                        itemStr = itemStr.trimEnd()
//                        val toByteArray = itemStr.toByteArray(Charset.forName("utf-8"))
//                        if (toByteArray.size > 40) {
//                            itemStr = getSubstring(itemStr, 40) + "..."
//                        }
//                        text += itemStr
//                    }
//                }
//            }
//            if (text.isEmpty()) {
//                if (isValueNull) {
//                    outerDescriptionView?.visibility = GONE
//                }
//                rlDescription?.visibility = GONE
//                outerDescriptionU?.text = ""
//            } else {
//                rlDescription?.visibility = VISIBLE
//                outerDescriptionU?.text = text
//                outerDescriptionView?.visibility = VISIBLE
//            }
//        }
//    }
//
//    fun getSubstring(str: String?, count: Int): String {
//        if (null == str || str.isEmpty()) {
//            return ""
//        }
//        val uriBytes = str.toByteArray()
//        //用来存储中文字节下标
//        val list: MutableList<Int> = ArrayList()
//        for (i in 0 until count) {
////            判断中文，utf-8中一个中文3个字节，当中每个字节的ASCII值<0
//            if (Integer.valueOf(uriBytes[i].toString()) < 0) {
//                if (list.size == 3) {
//                    list.clear()
//                    list.add(i)
//                } else {
//                    list.add(i)
//                }
//            }
//        }
//        return if (list.size != 3 && list.size != 0) {
//            val newUriValue = ByteArray(count - list.size)
//            System.arraycopy(uriBytes, 0, newUriValue, 0, count - list.size)
//            String(newUriValue)
//        } else {
//            val newUriValue = ByteArray(count)
//            System.arraycopy(uriBytes, 0, newUriValue, 0, count)
//            String(newUriValue)
//        }
//    }
//
//    fun initBackground() {
//        setBackgroundColor(Color.TRANSPARENT)
//        setDrawGridBackground(true)
//        val color = ThemeManager.getTypeValue(context, R.attr.bgHomeGlucose)
//        setGridBackgroundColor(color)
//        gridBackgroundStart = chartManager.lowerLimit
//        gridBackgroundEnd = chartManager.upperLimit
//    }
//
//    fun initChartAxisX() {
//        xAxis.setDrawAxisLine(false)
//        xAxis.setDrawGridLines(true)
//        val color = ThemeManager.getTypeValue(context, R.attr.colorLineChart)
//        xAxis.gridColor = color
//        xAxis.setDrawLabels(true)
//        xAxis.position = XAxis.XAxisPosition.BOTTOM
//        xAxis.textSize = 14f
//        xAxis.yOffset = 5f
//        xAxis.textColor = textColor!!
//        xAxis.granularity = chartManager.granularity.toFloat()
//        xAxis.labelCount = (labelCount + 1)
//        val lines = floatArrayOf(22f, 1000f)
//        xAxis.setGridDashedLine(DashPathEffect(lines, 0f))
//        xAxis.gridLineWidth = 1f
//
//        xAxis.valueFormatter = object : ValueFormatter() {
//            private var mFormatD: SimpleDateFormat =
//                SimpleDateFormat("MM/dd", Locale.getDefault())
//            private var mFormatT: SimpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
//            override fun getFormattedValue(value: Float): String {
//                val second: Long = XAxisUtils.xToSecond(value)
//                LiveEventBus.get<String>(EventKey.REFRESH_CHART_DATE)
//                    .post(mFormatD.format(Date(second * 1000)))
//                val hours =
//                    (second + TimeUtils.timeZoneOffsetSeconds()) % TimeUtils.oneDay / TimeUtils.oneHour
//                return when {
//                    hours == 0L -> {
//                        mFormatT.format(Date(second * 1000))
//                    }
//                    (value - lowestVisibleX < xAxis.granularity) && (hours % 24 < 24 - xAxis.granularity * (labelCount - 1)) -> {
//                        mFormatT.format(Date(second * 1000))
//                    }
//                    else -> {
//                        mFormatT.format(Date(second * 1000))
//                    }
//                }
//            }
//        }
//    }
//
//    fun initChartAxisY() {
//
//        majorAxis = if (0 == yXAxisPosition) {
//            YAxis.AxisDependency.RIGHT
//        } else {
//            YAxis.AxisDependency.LEFT
//        }
//
//        val yAxis: YAxis = if (0 == yXAxisPosition) {
//            axisRight
//        } else {
//            axisLeft
//        }
//        yAxis.setDrawAxisLine(false)
//        yAxis.setDrawGridLines(true)
//        yAxis.setDrawLabels(true)
//        val color = ThemeManager.getTypeValue(context, R.attr.colorLineChart)
//        yAxis.gridColor = color
//        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
//        when (UnitManager.glucoseUnit) {
//            UnitManager.GlucoseUnit.MMOL_PER_L -> {
//                yAxis.axisMinimum = 0f
//                yAxis.axisMaximum = 30f
//            }
//            UnitManager.GlucoseUnit.MG_PER_DL -> {
//                yAxis.axisMinimum = 0f
//                yAxis.axisMaximum = 600f
//            }
//        }
//
//        yAxis.setLabelCount(6, true)
//        yAxis.textSize = 14f
//        yAxis.textColor = textColor!!
//        if (0 == yXAxisPosition) {
//            axisLeft.isEnabled = false
//        } else {
//            axisRight.isEnabled = false
//            axisLeft.isEnabled = true
//        }
//
//    }
//
//    fun xRange(): Float {
//        return xAxis.granularity * labelCount
//    }
//
//    fun xMargin(): Float {
//        return xAxis.granularity / 2f
//    }
//
//    fun xMin(): Float {
//        val default = xMax() - xRange()
//        return if (chartManager.timeMin == null || chartManager.timeMin!! > default) default
//        else chartManager.timeMin!!
//    }
//
//    fun xMax(): Float {
//        return XAxisUtils.secondToX(Date().time / 1000) + xMargin()
//    }
//
//    fun moveToDay(dayInSecond: Long) {
//        moveToTime(TimeUtils.zeroOfDay(dayInSecond))
//    }
//
//    fun moveToTime(timeInSecond: Long) {
//        moveViewToX(XAxisUtils.secondToX(timeInSecond))
//        delayAutoScaleY(100)
//    }
//
//    fun paint() {
//        initBackground()
//        val lineDataSets: ArrayList<ILineDataSet> = ArrayList()
//        lineDataSets.addAll(generateLimitLines())
//        lineDataSets.addAll(chartManager.getGlucoseSets())
//        if (UserManager.shareUserEntity == null) {
//            val currentGlucose = chartManager.getCurrentGlucose()
//            currentGlucose.circleHoleColor =
//                ThemeUtils.getTypeValue(context, com.microtechmd.cgms.R.attr.containerBackground)
//            lineDataSets.add(currentGlucose)
//        }
//
//        val scatterDataSets: ArrayList<IScatterDataSet> = ArrayList()
//        scatterDataSets.add(chartManager.calSet)
//        scatterDataSets.add(chartManager.bgSet)
//        scatterDataSets.add(chartManager.eventSet)
//
//        val combinedData = CombinedData()
//        combinedData.setData(LineData(lineDataSets))
//        combinedData.setData(ScatterData(scatterDataSets))
//        data = combinedData
//
//        refreshAndMove()
//    }
//
//    private fun generateLimitLines(): List<LineDataSet> {
//        val l1 = LineDataSet(
//            listOf(
//                Entry(xMin(), chartManager.upperLimit),
//                Entry(xMax(), chartManager.upperLimit)
//            ),
//            ""
//        )
//        l1.axisDependency = YAxis.AxisDependency.RIGHT
//        l1.setDrawValues(false)
//        l1.setDrawCircles(false)
//        l1.color = Color.TRANSPARENT
//        l1.lineWidth = 0f
//        l1.isHighlightEnabled = false
//
//        val l2 = LineDataSet(
//            listOf(
//                Entry(xMin(), chartManager.lowerLimit),
//                Entry(xMax(), chartManager.lowerLimit)
//            ),
//            ""
//        )
//        l2.setDrawValues(false)
//        l2.setDrawCircles(false)
//        l2.color = Color.TRANSPARENT
//        l2.lineWidth = 0f
//        l2.isHighlightEnabled = false
//        l1.setDrawFilled(false)
//
//        return listOf(l1, l2)
//    }
//
//    fun refresh() {
//        touchable = false
////        changeStyle()
//
//        xAxis.granularity = chartManager.granularity.toFloat()
//        xAxis.axisMinimum = xMin()
//        xAxis.axisMaximum = xMax()
//        visibleXRange = xRange()
//        visibleXRange = xRange() // should zoom twice
//        autoScaleY()
//    }
//
//    fun refreshAndMove() {
//        touchable = false
////        changeStyle()
//
//        xAxis.axisMinimum = xMin()
//        xAxis.axisMaximum = xMax()
//        xAxis.granularity = chartManager.granularity.toFloat()
//
//        visibleXRange = xRange()
////        visibleXRange = xRange()
//        moveViewToX(xMax())
//        delayAutoScaleY(1000)
//    }
//
//    private fun changeStyle() {
//        for (dataSet in data.lineData.dataSets) {
//            if (dataSet.label == "glucose") {
//                dataSet as GlucoseDataSet
//                if (chartManager.granularity < 2) {
//                    dataSet.setDrawCircles(true)
//                    dataSet.lineWidth = 0f
//                } else {
//                    dataSet.setDrawCircles(false)
//                    dataSet.lineWidth = 2f
//                }
//            }
//        }
//    }
//}