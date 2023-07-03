package com.microtech.aidexx.ui.main.trend.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.BarLineChartTouchListener
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener


open class TrendsChart : CombinedChart {
    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null

    var majorAxis: AxisDependency? = null
    var highlightOnLongPressedEnable = true

    var touchable = true
    var selectAllow = false
    var onSelectX: ((x: Float?) -> Unit)? = null

    var gridBackgroundStart = 0f
    var gridBackgroundEnd = 0f

    var mVisibleXRange = 6f
    fun setVisibleXRange(range: Float) {
        mVisibleXRange = range
        stopFlying()
        zoomToVisibleRange()
    }

    var enableAutoScaleY = true
    val yMaximums: MutableList<Float> = ArrayList()
    val yMinimums: MutableList<Float> = ArrayList()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun init() {
        super.init()
        setNoDataText("")
        extraBottomOffset = 18f
        onChartGestureListener = MyOnChartGestureListener()
    }

    /*
    自定义背景
     */
    override fun drawGridBackground(c: Canvas?) {
//        if (mDrawGridBackground) { // draw the grid background
//            val rect = RectF(mViewPortHandler.contentRect)
//            rect.bottom = getPixelForValues(xChartMin, gridBackgroundStart, majorAxis).y.toFloat()
//            rect.top = getPixelForValues(xChartMin, gridBackgroundEnd, majorAxis).y.toFloat()
//            c!!.drawRect(rect, mGridBackgroundPaint)
//        }
//
//        if (mDrawBorders) {
//            c!!.drawRect(mViewPortHandler.contentRect, mBorderPaint)
//        }
    }


    override fun getChartBitmap(): Bitmap {
        bitmap = bitmap ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444)

        canvas = canvas ?: Canvas(bitmap!!)

        try {
            background?.draw(canvas!!)
            draw(canvas)
        } catch (e: Exception) {
            //TODO
        }

        return bitmap!!
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action and MotionEvent.ACTION_MASK != MotionEvent.ACTION_DOWN && !touchable) return false
        val returnValue = super.onTouchEvent(event)
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                touchable = true
                selectAllow = false
                onSelectX?.invoke(null)
            }
            MotionEvent.ACTION_UP -> {
                zoomToVisibleRange()
                if (enableAutoScaleY) autoScaleY()
            }
            MotionEvent.ACTION_MOVE -> {
                if (selectAllow) {
                    onSelectX?.invoke(
                        getValuesByTouchPoint(
                            event.x,
                            event.y,
                            majorAxis
                        ).x.toFloat()
                    )
                }
            }
        }
        return returnValue
    }

    /*
    大小变化时保持显示区域不变
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val lastLowestVisibleX = lowestVisibleX
        super.onSizeChanged(w, h, oldw, oldh)
        if (oldw > 0 || oldh > 0) moveViewToX(lastLowestVisibleX)
    }

    fun delayAutoScaleY(delayMillis: Long) {
        postDelayed({ autoScaleY() }, delayMillis)
    }


    /*
    自适应y轴
     */
    fun autoScaleY() {
        if (data == null) return
        data.calcMinMaxY(lowestVisibleX, highestVisibleX)

        val yAxis = getAxis(majorAxis)
        var yUpper = yAxis.axisMaximum
        for (y in yMaximums) {
            if (data.yMax < y) {
                yUpper = y
                break
            }
        }
        var yLower = yAxis.axisMinimum
        for (y in yMinimums) {
            if (data.yMin > y) {
                yLower = y
                break
            }
        }
        val newScaleY = (yAxis.axisMaximum - yAxis.axisMinimum) / (yUpper - yLower)
        if (scaleY != newScaleY) {
            zoomAndCenterAnimated(
                scaleX, newScaleY,
                (lowestVisibleX + highestVisibleX) / 2f,
                (yUpper + yLower) / 2f,
                majorAxis, 200
            )
        }
    }


    private fun stopFlying() {
        (mChartTouchListener as BarLineChartTouchListener).stopDeceleration()
    }


    /*
    根据指定显示范围缩放
     */
    private fun zoomToVisibleRange() {
        zoom(visibleXRange / mVisibleXRange, 1f, centerOffsets.x * 2, 0f)
    }


    fun removeHighLine() {
        this.highlightValue(null, true)
        this.isHighlightPerDragEnabled = false
    }


    /*
    长按触发highlight
     */
    inner class MyOnChartGestureListener : OnChartGestureListener {

        private var highlightAllowed: Boolean = true
        private val chart = this@TrendsChart

        override fun onChartGestureStart(
            me: MotionEvent?,
            lastPerformedGesture: ChartTouchListener.ChartGesture?
        ) {
            chart.highlightValue(null, true)
            highlightAllowed = true
            chart.isHighlightPerDragEnabled = false
        }

        override fun onChartGestureEnd(
            me: MotionEvent?,
            lastPerformedGesture: ChartTouchListener.ChartGesture?
        ) {
            highlightAllowed = false
            chart.isHighlightPerDragEnabled = false
        }

        override fun onChartLongPressed(me: MotionEvent) {
            if (!highlightOnLongPressedEnable) return

            if (highlightAllowed) {
                selectAllow = true
                onSelectX?.invoke(getValuesByTouchPoint(me.x, me.y, majorAxis).x.toFloat())
                val h: Highlight? = chart.getHighlightByTouchPoint(me.x, me.y)
                chart.highlightValue(h, true)
            }
            chart.isHighlightPerDragEnabled = true
        }

        override fun onChartDoubleTapped(me: MotionEvent?) {}
        override fun onChartSingleTapped(me: MotionEvent?) {}
        override fun onChartFling(
            me1: MotionEvent?,
            me2: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ) {
        }

        override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {}

        override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
            highlightAllowed = false
        }
    }

}