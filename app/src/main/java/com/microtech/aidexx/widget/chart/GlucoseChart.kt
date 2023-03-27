package com.microtech.aidexx.widget.chart

import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.IntDef
import androidx.annotation.LongDef
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.jeremyliao.liveeventbus.LiveEventBus
import com.microtech.aidexx.R
import com.microtech.aidexx.common.convertPointer
import com.microtech.aidexx.common.dateAndTimeHour
import com.microtech.aidexx.common.toGlucoseString2
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.entity.EventEntity
import com.microtech.aidexx.ui.main.home.chart.ChartManager
import com.microtech.aidexx.widget.chart.dataset.GlucoseDataSet
import com.microtech.aidexx.utils.*
import com.microtech.aidexx.utils.eventbus.EventBusKey
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * Home页中的图表
 */
class GlucoseChart : MyChart {

    companion object {
        const val CHART_LABEL_COUNT: Int = 6
    }

    interface ExtraParams {

        /**
         * 选中事件浮框根布局
         */
        var outerDescriptionView: View?

        /**
         * 选中事件浮框标题栏根布局
         */
        var llValue: LinearLayout?
        /**
         * 选中事件浮框标题栏数值框
         */
        var outerDescriptionY: TextView?
        /**
         * 选中事件浮框标题栏的单位
         */
        var outerDescriptionUnit: TextView?
        /**
         * 选中事件浮框标题栏的时间
         */
        var outerDescriptionX: TextView?

        /**
         * 选中事件浮框具体事件的根布局
         */
        var rlDescription: RelativeLayout?
        /**
         * 选中事件浮框具体事件的内容
         */
        var outerDescriptionU: TextView?
        /**
         * 选中事件浮框具体事件的跳转历史记录页面按钮
         */
        var goToHistory: ImageView?
        /**
         * 选中事件浮框具体事件的跳转历史记录按钮点击回调
         */
        var onGoToHistory: (() -> Unit)?

    }

    var extraParams: ExtraParams? = null

    private var isValueNull: Boolean = true

    private val chartManager = ChartManager.instance()

    var textColor: Int? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    init {
        textColor = ThemeManager.getTypeValue(context, R.attr.colorChartText)
        initBackground()
        initChartAxisX()
        initChartAxisY()
        minOffset = 0f
        extraTopOffset = 10f
        extraBottomOffset = 21f
        extraLeftOffset = 10f
        extraRightOffset = 10f
        mMaxVisibleCount = 2000

        description.isEnabled = false
        description.textColor = textColor!!
        description.textSize = 20f
        legend.isEnabled = false

//        chart.setClipDataToContent(false)
        setScaleEnabled(false)
        setTouchEnabled(true)
        isDragXEnabled = true
        isDragYEnabled = false
        isDragDecelerationEnabled = true //惯性滚动
        isHighlightPerDragEnabled = false
        isHighlightPerTapEnabled = false
        maxHighlightDistance = 10f

        enableAutoScaleY = true
        when (UnitManager.glucoseUnit) {
            UnitManager.GlucoseUnit.MMOL_PER_L -> yMaximums.addAll(listOf(20f))
            UnitManager.GlucoseUnit.MG_PER_DL -> yMaximums.addAll(listOf(400f))
        }

        setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            private val formatSD =
                LanguageUnitManager.languageUnitByIndex(context).hmFormat

            override fun onValueSelected(e: Entry, h: Highlight) {
                extraParams?.outerDescriptionX?.text = formatSD.format(XAxisUtils.xToSecond(h.x) * 1000)
                if (ThemeManager.isLight()) {
                    extraParams?.outerDescriptionView?.setBackgroundResource(R.drawable.bg_desc_light)
                } else {
                    extraParams?.outerDescriptionView?.setBackgroundResource(R.drawable.bg_desc_dark)
                }
                if (e.data == null) {
                    extraParams?.outerDescriptionY?.text = buildString {
                        append(h.y.toGlucoseString2().convertPointer())
                    }
                    extraParams?.outerDescriptionUnit?.text = UnitManager.glucoseUnit.text
                    isValueNull = false
                } else {
                    extraParams?.outerDescriptionY?.text = "--"
                    extraParams?.outerDescriptionUnit?.text = UnitManager.glucoseUnit.text
                    isValueNull = true
                }
                extraParams?.outerDescriptionView?.visibility = VISIBLE
            }

            override fun onNothingSelected() {
                extraParams?.outerDescriptionUnit?.text = ""
                extraParams?.outerDescriptionX?.text = ""
                extraParams?.outerDescriptionY?.text = ""
                extraParams?.outerDescriptionUnit?.text = ""
                extraParams?.outerDescriptionView?.visibility = GONE
            }
        })

        onSelectX = { x ->
            var text = ""
            if (x != null && data.scatterData != null) {
                val list: MutableList<Entry> = ArrayList()
                for (dataSet in data.scatterData.dataSets) {
                    list.addAll(dataSet.getEntriesForXRange(x - 0.5f, x + 0.5f))
                }
                if (list.size > 5) {
                    extraParams?.goToHistory?.visibility = View.VISIBLE
                    extraParams?.goToHistory?.setOnClickListener {
                        extraParams?.onGoToHistory?.invoke()
                    }
                } else {
                    extraParams?.goToHistory?.visibility = View.GONE
                }
                for ((index, e) in list.withIndex()) {
                    val data = e.data as EventEntity
                    if (index > 4) {
                        text += buildString {
                            append("\n ...")
                        }
                        break
                    } else {
                        var itemStr = buildString {
                            if (text.isNotEmpty()) append("\n")
                        } + data.time.dateAndTimeHour() + buildString {
                            append(" ")
                        } + data.getEventDescription(
                            resources
                        ) + buildString {
                            append("")
                        } + data.getValueDescription(
                            resources
                        )
                        itemStr = itemStr.trimEnd()
                        val toByteArray = itemStr.toByteArray(Charset.forName("utf-8"))
                        if (toByteArray.size > 40) {
                            itemStr = getSubstring(itemStr, 40) + "..."
                        }
                        text += itemStr
                    }
                }
            }
            if (text.isEmpty()) {
                if (isValueNull) {
                    extraParams?.outerDescriptionView?.visibility = GONE
                }
                extraParams?.rlDescription?.visibility = GONE
                extraParams?.outerDescriptionU?.text = ""
            } else {
                extraParams?.rlDescription?.visibility = VISIBLE
                extraParams?.outerDescriptionU?.text = text
                extraParams?.outerDescriptionView?.visibility = VISIBLE
            }
        }

    }


    /**
     * 初始化数据集
     */
    fun initData(combinedData: CombinedData) {
        initBackground()

        data = combinedData

        notifyChanged(true)
    }

    /**
     * 数据或者配置变动后刷新图表,业务可控制是否移动到最新视图
     * @param needMoveLatest 外部控制是否移动到最新视图，
     *                       默认false时，会根据图表当前是否在最新视图位置判断是否需要移动到最新视图
     */
    fun notifyChanged(needMoveLatest: Boolean = false) {

        if (UserInfoManager.shareUserInfo == null) {
            val currentGlucose = chartManager.getCurrentGlucose()
            currentGlucose.circleHoleColor =
                ThemeManager.getTypeValue(context, R.attr.containerBackground)

            data.lineData.dataSets.remove(chartManager.getCurrentGlucose())
            data.lineData.dataSets.add(currentGlucose)
        } else {
            data.lineData.dataSets.remove(chartManager.getCurrentGlucose())
        }

        data.notifyDataChanged()
        notifyDataSetChanged()

        if(needMoveLatest || isAtLatestPosition(highestVisibleX))
            refreshAndMove()
        else
            refresh()
    }

    /**
     * 切换时间模式
     */
    fun updateGranularity(@ChartGranularityPerScreen granularity: Int) {
        xAxis.granularity = granularity.toFloat()
    }

    private fun isAtLatestPosition(highestVisibleX: Float): Boolean {
        val cur = (highestVisibleX * 1000).roundToInt().toFloat() / 1000
        val max = (xChartMax * 1000).roundToInt().toFloat() / 1000
        return cur >= max
    }

    private fun refresh() {
        touchable = false
//        xAxis.granularity = chartManager.granularity.toFloat()
        xAxis.axisMinimum = xMin()
        xAxis.axisMaximum = xMax()
        visibleXRange = xRange()
        visibleXRange = xRange() // should zoom twice
        autoScaleY()
    }

    private fun refreshAndMove() {
        touchable = false
        xAxis.axisMinimum = xMin()
        xAxis.axisMaximum = xMax()
//        xAxis.granularity = chartManager.granularity.toFloat()
        visibleXRange = xRange()

        moveViewToX(xMax())
        delayAutoScaleY(1000)
    }



    private fun initBackground() {
        setBackgroundColor(Color.TRANSPARENT)
        setDrawGridBackground(true)
        val color = ThemeManager.getTypeValue(context, R.attr.bgHomeGlucose)
        setGridBackgroundColor(color)
        gridBackgroundStart = chartManager.lowerLimit
        gridBackgroundEnd = chartManager.upperLimit
    }

    private fun initChartAxisX() {
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(true)
        val color = ThemeManager.getTypeValue(context, R.attr.colorLineChart)
        xAxis.gridColor = color
        xAxis.setDrawLabels(true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textSize = 14f
        xAxis.yOffset = 5f
        xAxis.textColor = textColor!!
        xAxis.granularity = chartManager.granularity.toFloat()
        xAxis.labelCount = (CHART_LABEL_COUNT + 1)
        val lines = floatArrayOf(22f, 1000f)
        xAxis.setGridDashedLine(DashPathEffect(lines, 0f));
        xAxis.gridLineWidth = 1f

        xAxis.valueFormatter = object : ValueFormatter() {

            private val mFormatD =
                LanguageUnitManager.languageUnitByIndex(context).monthDayDateFormat
            private var mFormatT: SimpleDateFormat =
                LanguageUnitManager.languageUnitByIndex(context).hmFormat

            override fun getFormattedValue(value: Float): String {
                val second: Long = XAxisUtils.xToSecond(value)
                LiveEventBus.get<String>(EventBusKey.REFRESH_CHART_DATE)
                    .post(mFormatD.format(Date(second * 1000)))
                val hours =
                    (second + TimeUtils.timeZoneOffsetSeconds()) % TimeUtils.oneDaySeconds / TimeUtils.oneHourSeconds
                return when {
                    hours == 0L -> {
                        mFormatT.format(Date(second * 1000))
                    }
                    (value - lowestVisibleX < xAxis.granularity) && (hours % 24 < 24 - xAxis.granularity * (CHART_LABEL_COUNT - 1)) -> {
                        mFormatT.format(Date(second * 1000))
                    }
                    else -> {
                        mFormatT.format(Date(second * 1000))
                    }
                }
            }
        }
    }

    private fun initChartAxisY() {
        majorAxis = YAxis.AxisDependency.RIGHT
        val yAxis: YAxis = axisRight
        yAxis.setDrawAxisLine(false)
        yAxis.setDrawGridLines(true)
        yAxis.setDrawLabels(true)
        val color = ThemeManager.getTypeValue(context, R.attr.colorLineChart)
        yAxis.gridColor = color
        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
        when (UnitManager.glucoseUnit) {
            UnitManager.GlucoseUnit.MMOL_PER_L -> {
                yAxis.axisMinimum = 0f
                yAxis.axisMaximum = 30f
            }
            UnitManager.GlucoseUnit.MG_PER_DL -> {
                yAxis.axisMinimum = 0f
                yAxis.axisMaximum = 600f
            }
        }

        yAxis.setLabelCount(6, true)
//        yAxis.granularity = 5f
        yAxis.textSize = 14f
        yAxis.textColor = textColor!!

        axisLeft.isEnabled = false
    }

    private fun xRange(): Float {
        return xAxis.granularity * CHART_LABEL_COUNT
    }

    private fun xMargin(): Float {
        return xAxis.granularity / 2f
    }

    private fun xMin(): Float {
        val default = xMax() - xRange()
        return if (chartManager.timeMin == null || chartManager.timeMin!! > default) default
        else chartManager.timeMin!!
    }

    private fun xMax(): Float {
        return XAxisUtils.secondToX(Date().time / 1000) + xMargin()
    }

    fun moveToDay(dayInSecond: Long) {
        moveToTime(TimeUtils.zeroOfDay(dayInSecond))
    }

    private fun moveToTime(timeInSecond: Long) {
        moveViewToX(XAxisUtils.secondToX(timeInSecond))
        delayAutoScaleY(100)
    }

    private fun generateLimitLines(): List<LineDataSet> {
        val l1 = LineDataSet(
            listOf(
                Entry(xMin(), chartManager.upperLimit),
                Entry(xMax(), chartManager.upperLimit)
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
                Entry(xMin(), chartManager.lowerLimit),
                Entry(xMax(), chartManager.lowerLimit)
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


    private fun changeStyle() {
        for (dataSet in data.lineData.dataSets) {
            if (dataSet.label == "glucose") {
                dataSet as GlucoseDataSet
                if (chartManager.granularity < 2) {
                    dataSet.setDrawCircles(true)
                    dataSet.lineWidth = 0f
                } else {
                    dataSet.setDrawCircles(false)
                    dataSet.lineWidth = 2f
                }
            }
        }
    }

    private fun getSubstring(str: String?, count: Int): String {
        if (null == str || str.isEmpty()) {
            return ""
        }
        val uriBytes = str.toByteArray()
        //用来存储中文字节下标
        val list: MutableList<Int> = ArrayList()
        for (i in 0 until count) {
//            判断中文，utf-8中一个中文3个字节，当中每个字节的ASCII值<0
            if (Integer.valueOf(uriBytes[i].toString()) < 0) {
                if (list.size == 3) {
                    list.clear()
                    list.add(i)
                } else {
                    list.add(i)
                }
            }
        }
        return if (list.size != 3 && list.size != 0) {
            val newUriValue = ByteArray(count - list.size)
            System.arraycopy(uriBytes, 0, newUriValue, 0, count - list.size)
            String(newUriValue)
        } else {
            val newUriValue = ByteArray(count)
            System.arraycopy(uriBytes, 0, newUriValue, 0, count)
            String(newUriValue)
        }
    }
}