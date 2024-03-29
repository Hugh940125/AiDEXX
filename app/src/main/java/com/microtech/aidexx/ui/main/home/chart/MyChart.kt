package com.microtech.aidexx.ui.main.home.chart

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.BarLineChartTouchListener
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.utils.MPPointD

open class MyChart : CombinedChart {
    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null

    var majorAxis: AxisDependency? = null
    private var highlightOnLongPressedEnable = true

    var touchable = true
    private var selectAllow = false
    var onSelectX: ((x: Float?) -> Unit)? = null

    var gridBackgroundStart = 0f
    var gridBackgroundEnd = 0f

    private var mVisibleXRange = 6f
    fun setVisibleXRange(range: Float) {
        mVisibleXRange = range
        stopFlying()
        zoomToVisibleRange()
    }

    var enableAutoScaleY = false
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
        onChartGestureListener = MyOnChartGestureListener()
    }

    /*
    自定义背景
     */
    override fun drawGridBackground(c: Canvas?) {
        if (mDrawGridBackground) { // draw the grid background
            val rect = RectF(mViewPortHandler.contentRect)
            rect.bottom = getPixelForValues(xChartMin, gridBackgroundStart, majorAxis).y.toFloat()
            rect.top = getPixelForValues(xChartMin, gridBackgroundEnd, majorAxis).y.toFloat()
            c!!.drawRect(rect, mGridBackgroundPaint)
        }

        if (mDrawBorders) {
            c!!.drawRect(mViewPortHandler.contentRect, mBorderPaint)
        }
    }


    override fun getChartBitmap(): Bitmap {
        bitmap = bitmap ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap?.let {
            canvas = canvas ?: Canvas(it)
            try {
                background?.draw(canvas!!)
                draw(canvas)
            } catch (e: Exception) {
                e.printStackTrace()
            }
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


    /*
    完善缩放动画
     */
    override fun zoomAndCenterAnimated(
        scaleX: Float,
        scaleY: Float,
        xValue: Float,
        yValue: Float,
        axis: AxisDependency?,
        duration: Long
    ) {
        val origin = getValuesByTouchPoint(
            mViewPortHandler.contentLeft(),
            mViewPortHandler.contentRect.centerY(),
            axis
        )
        val job = MyAnimatedZoomJob.getInstance(
            mViewPortHandler,
            this,
            getTransformer(axis),
            getAxis(axis),
            mXAxis.mAxisRange,
            scaleX,
            scaleY,
            mViewPortHandler.scaleX,
            mViewPortHandler.scaleY,
            xValue,
            yValue,
            origin.x.toFloat(),
            origin.y.toFloat(),
            200
        )
        addViewportJob(job)
        MPPointD.recycleInstance(origin)
    }

    fun delayAutoScaleY(delayMillis: Long) {
        zoomToVisibleRange()
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
            if (data.yMax < y - 2) {
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
        private val chart = this@MyChart

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