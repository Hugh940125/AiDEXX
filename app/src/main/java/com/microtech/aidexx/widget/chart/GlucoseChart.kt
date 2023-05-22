package com.microtech.aidexx.widget.chart

import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.microtech.aidexx.R
import com.microtech.aidexx.db.entity.BaseEventEntity
import com.microtech.aidexx.utils.LanguageUnitManager
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.ThemeManager
import com.microtech.aidexx.utils.TimeUtils
import com.microtech.aidexx.utils.UnitManager
import java.lang.ref.WeakReference
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Home页中的图表
 */
class GlucoseChart : MyChart {

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

        /**
         * 当前区域所属日期
         */
        var curDateTv: TextView?

        /**
         * 当前x轴最大值
         */
        fun xMax(): Float
        /**
         * 当前x轴最小值
         */
        fun xMin(): Float
        /**
         * 当前可视区域区间
         */
        fun xRange(): Float
        /**
         * 当前最右侧留多大空间/时间
         */
        fun xMargin(): Float

        /**
         * 当前y轴最低限制
         */
        fun lowerLimit(): Float
        /**
         * 当前y轴最高限制
         */
        fun upperLimit(): Float

    }

    var extraParams: ExtraParams? = null

    private var isValueNull: Boolean = true

    var textColor: Int? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    class THandler(val chart: GlucoseChart): Handler(Looper.getMainLooper()) {
        private val mChart: WeakReference<GlucoseChart> = WeakReference(chart)
        override fun handleMessage(msg: Message) {
            if (MSG_TIME_TO_REFRESH_CHART == msg.what) {
                mChart.get()?.notifyChanged(false)
            }
        }
    }
    private val timerHandler by lazy { THandler(this) }

    init {
//        isLogEnabled = BuildConfig.DEBUG
        textColor = ThemeManager.getTypeValue(context, R.attr.colorChartText)
        initBackground()
        initChartAxisX()
        initChartAxisY()

        needDrawLineDataSetValuesAndIcons = false

        minOffset = 0f
        extraTopOffset = 10f
        extraBottomOffset = 21f
        extraLeftOffset = 10f
        extraRightOffset = 10f

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
                extraParams?.outerDescriptionX?.text = formatSD.format(ChartUtil.xToSecond(h.x) * 1000)
                if (ThemeManager.isLight()) {
                    extraParams?.outerDescriptionView?.setBackgroundResource(R.drawable.bg_desc_light)
                } else {
                    extraParams?.outerDescriptionView?.setBackgroundResource(R.drawable.bg_desc_dark)
                }
                if (e.data == null) {
                    extraParams?.outerDescriptionY?.text = buildString {
                        append(h.y)
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
                    val data = e.data as BaseEventEntity
                    if (index > 4) {
                        text += buildString {
                            append("\n ...")
                        }
                        break
                    } else {
                        var itemStr = buildString {
                            if (text.isNotEmpty()) append("\n")
                        } + data.getDisplayTime() + buildString {
                            append(" ")
                        } + data.getEventDescription(
                            resources
                        ) + buildString {
                            append(" ")
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

        resetTimerToRefreshChart()
        // 如果初始化比较慢 新来的数据优先调到这里
        if (data == null) {
            return
        }
        LogUtil.d("===CHART===  highestVisibleX=${highestVisibleX} max=${xAxis.axisMaximum} min=${xAxis.axisMinimum}")
        updateYaxisMaxMin()
        updateGlucoseStartEndValue()
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
        val maxTime = ChartUtil.xToSecond(xChartMax - (extraParams?.xMargin()?:0f))
        val highestVisibleTime = ChartUtil.xToSecond(highestVisibleX)
        LogUtil.d("===CHART=== diff: m=$maxTime - h=$highestVisibleTime = ${maxTime - highestVisibleTime} ")
        return maxTime  - highestVisibleTime < 60
    }

    private fun refresh() {
        touchable = false

        val curMin = xAxis.axisMinimum
        val curRange = visibleXRange
        val targetX = highestVisibleX - curRange

        xAxis.axisMaximum = extraParams?.xMax() ?: 0f
        xAxis.axisMinimum = extraParams?.xMin() ?: 0f
        visibleXRange = extraParams?.xRange() ?: 0f
//        visibleXRange = extraParams?.xRange() ?: 0f // should zoom twice

        LogUtil.d("===CHART=== tar=$targetX hi=$highestVisibleX  cr=$curRange rang=$visibleXRange max=${xAxis.axisMaximum} curmin=$curMin min=${xAxis.axisMinimum}")

        // 保持当前位置
        moveViewToX(targetX)
        delayAutoScaleY(100)

    }

    private fun refreshAndMove() {
        touchable = false
        val max = extraParams?.xMax() ?: 0f
        xAxis.axisMinimum = extraParams?.xMin() ?: 0f
        xAxis.axisMaximum = max
        visibleXRange = extraParams?.xRange() ?: 0f

        moveViewToX(max)
        delayAutoScaleY(1000)
    }



    private fun initBackground() {
        setBackgroundColor(Color.TRANSPARENT)
        setDrawGridBackground(true)
        val color = ThemeManager.getTypeValue(context, R.attr.bgHomeGlucose)
        setGridBackgroundColor(color)
        updateGlucoseStartEndValue()
    }

    /**
     * 更新血糖阈值区间
     */
    private fun updateGlucoseStartEndValue(){
        gridBackgroundStart = extraParams?.lowerLimit() ?: 0f
        gridBackgroundEnd = extraParams?.upperLimit() ?: 0f
    }

    /**
     * 更新y轴最大最小值
     */
    private fun updateYaxisMaxMin() {
        when (UnitManager.glucoseUnit) {
            UnitManager.GlucoseUnit.MMOL_PER_L -> {
                axisRight.axisMinimum = 0f
                axisRight.axisMaximum = 30f
            }
            UnitManager.GlucoseUnit.MG_PER_DL -> {
                axisRight.axisMinimum = 0f
                axisRight.axisMaximum = 600f
            }
        }
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
        xAxis.granularity = G_SIX_HOURS.toFloat()
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
                val second: Long = ChartUtil.xToSecond(value)

                extraParams?.curDateTv?.text = mFormatD.format(Date(second * 1000))

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

        updateYaxisMaxMin()

        yAxis.setLabelCount(6, true)
//        yAxis.granularity = 5f
        yAxis.textSize = 14f
        yAxis.textColor = textColor!!

        axisLeft.isEnabled = false
    }

    fun moveToDay(dayInSecond: Long) {
        moveToTime(TimeUtils.zeroOfDay(dayInSecond))
    }

    private fun moveToTime(timeInSecond: Long) {
        moveViewToX(ChartUtil.secondToX(timeInSecond))
        delayAutoScaleY(100)
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

    private fun resetTimerToRefreshChart() {
        timerHandler.removeMessages(MSG_TIME_TO_REFRESH_CHART)
        timerHandler.sendMessageDelayed(
            Message.obtain(timerHandler, MSG_TIME_TO_REFRESH_CHART),
            INTERVAL_REFRESH_WHEN_NO_EVENT
        )
    }
    companion object {
        private val TAG = GlucoseChart::class.java.simpleName
        const val CHART_LABEL_COUNT: Int = 6
        private const val MSG_TIME_TO_REFRESH_CHART: Int = 0x01
        private const val INTERVAL_REFRESH_WHEN_NO_EVENT: Long = 5 * 60 * 1000
    }

}