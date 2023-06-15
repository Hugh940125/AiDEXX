package com.microtech.aidexx.ui.main.trend.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.microtech.aidexx.R
import com.microtech.aidexx.utils.DensityUtils

class DotView : View {
    var dotColor: Int = R.color.gray_cc
    var mPaint: Paint = Paint()

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) {
        context?.let {
            val attributes =
                context.obtainStyledAttributes(attrs, R.styleable.DotView, defStyleAttr, 0)
            dotColor = attributes.getColor(
                R.styleable.DotView_dot_color,
                ContextCompat.getColor(context, R.color.gray_cc)
            )
            attributes.recycle()
        }
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.FILL
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var widthSize = 0
        var heightSize = 0
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        if (widthMode == MeasureSpec.EXACTLY) {
            widthSize = MeasureSpec.getSize(widthMeasureSpec)
        } else if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = DensityUtils.dp2px(12f)
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            heightSize = MeasureSpec.getSize(heightMeasureSpec)
        } else if (widthMode == MeasureSpec.AT_MOST) {
            heightSize = DensityUtils.dp2px(12f)
        }
        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        mPaint.color = dotColor
        canvas?.drawCircle(width / 2.0F, height / 2.0F, width / 2.0F, mPaint)
    }

    fun changeColor(color: Int) {
        dotColor = color
        postInvalidate()
    }
}