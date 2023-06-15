package com.microtech.aidexx.ui.main.trend.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.microtech.aidexx.R
import com.microtech.aidexx.common.stripTrailingZeros
import com.microtech.aidexx.utils.DensityUtils
import com.microtech.aidexx.utils.ThemeManager

class LbgiCursorView : View {
    private var valueString: String = "0"
    private var extremelyLowBitmap: Bitmap? = null
    private var lowBitmap: Bitmap? = null
    private var midBitmap: Bitmap? = null
    private var highBitmap: Bitmap? = null
    private var value: Float = 0f
    private lateinit var rect: Rect
    private lateinit var paint: Paint
    private val size15dp = DensityUtils.dp2px(15f)
    private val size18dp = DensityUtils.dp2px(18f)
    private val marker1Dot1 = 1.1
    private val marker2Dot5 = 2.5
    private val marker5 = 5
    private val hintExtremelyLow = context.getString(R.string.extremely_low)
    private val hintLow = context.getString(R.string.low)
    private val hintMid = context.getString(R.string.mid)
    private val hintHigh = context.getString(R.string.high)
    private var textColor: Int = 0
    private var highColor: Int = 0
    private var midColor: Int = 0
    private var lowColor: Int = 0
    private var extremelyLowColor: Int = 0

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun init() {
        extremelyLowColor = Color.parseColor("#89D29C")
        lowColor = Color.parseColor("#70BD65")
        midColor = Color.parseColor("#F0BE5B")
        highColor = Color.parseColor("#E15D4D")
        textColor =
            if (ThemeManager.isLight()) Color.parseColor("#393939")
            else Color.parseColor("#E6E6E6")
        val extremelyLowDrawable =
            ResourcesCompat.getDrawable(resources, R.drawable.ic_extremely_low_cursor, null)
        if (extremelyLowDrawable != null) {
            extremelyLowBitmap = drawableToBitmap(extremelyLowDrawable)
        }
        val lowDrawable =
            ResourcesCompat.getDrawable(resources, R.drawable.ic_low_cursor, null)
        if (lowDrawable != null) {
            lowBitmap = drawableToBitmap(lowDrawable)
        }
        val midDrawable =
            ResourcesCompat.getDrawable(resources, R.drawable.ic_mid_cursor, null)
        if (midDrawable != null) {
            midBitmap = drawableToBitmap(midDrawable)
        }
        val highDrawable =
            ResourcesCompat.getDrawable(resources, R.drawable.ic_high_cursor, null)
        if (highDrawable != null) {
            highBitmap = drawableToBitmap(highDrawable)
        }
        paint = Paint()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        rect = Rect()
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
            widthSize = DensityUtils.dp2px(200f)
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            heightSize = MeasureSpec.getSize(heightMeasureSpec)
        } else if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = DensityUtils.dp2px(80f)
        }
        setMeasuredDimension(widthSize, heightSize)
    }

    fun setValue(value: Float) {
        if (value > 5) {
            this.value = 5.5f
        } else {
            this.value = value
        }
        this.valueString = this.value.stripTrailingZeros()
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.color = extremelyLowColor
        canvas?.drawCircle(
            (size15dp).toFloat(),
            (height / 2).toFloat(),
            (size15dp / 2).toFloat(),
            paint
        )
        rect.set(
            size15dp,
            height / 2 - size15dp / 2,
            ((width - size15dp) * 0.18 + size15dp).toInt(),
            height / 2 + size15dp / 2
        )
        canvas?.drawRect(rect, paint)
        paint.color = lowColor
        rect.set(
            ((width - 2 * size15dp) * 0.18 + size15dp).toInt(),
            height / 2 - size15dp / 2,
            ((width - 2 * size15dp) * 0.42 + size15dp).toInt(),
            height / 2 + size15dp / 2
        )
        canvas?.drawRect(rect, paint)
        paint.color = midColor
        rect.set(
            ((width - 2 * size15dp) * 0.42 + size15dp).toInt(),
            height / 2 - size15dp / 2,
            ((width - 2 * size15dp) * 0.83 + size15dp).toInt(),
            height / 2 + size15dp / 2
        )
        canvas?.drawRect(rect, paint)
        paint.color = highColor
        canvas?.drawCircle(
            (width - size15dp).toFloat(),
            (height / 2).toFloat(),
            (size15dp / 2).toFloat(),
            paint
        )
        rect.set(
            ((width - 2 * size15dp) * 0.83 + size15dp).toInt(),
            height / 2 - size15dp / 2,
            (width - size15dp),
            height / 2 + size15dp / 2
        )
        canvas?.drawRect(rect, paint)

        paint.color = textColor
        paint.typeface = Typeface.DEFAULT
        paint.textSize = DensityUtils.sp2px(12f)
        if (value !in 0.8f..1.4f) {
            paint.getTextBounds(marker1Dot1.toString(), 0, marker1Dot1.toString().length, rect)
            canvas?.drawText(
                marker1Dot1.toString(),
                ((width - 2 * size15dp) * 0.18 + size15dp - rect.width() / 2).toFloat(),
                (height / 2).toFloat() - size15dp / 2 - rect.height(), paint
            )
        }
        if (value !in 2.2f..2.8f) {
            paint.getTextBounds(marker2Dot5.toString(), 0, marker2Dot5.toString().length, rect)
            canvas?.drawText(
                marker2Dot5.toString(),
                ((width - 2 * size15dp) * 0.42 + size15dp - rect.width() / 2).toFloat(),
                (height / 2).toFloat() - size15dp / 2 - rect.height(), paint
            )
        }
        if (value !in 4.7..5.3) {
            paint.getTextBounds(marker5.toString(), 0, marker5.toString().length, rect)
            canvas?.drawText(
                marker5.toString(),
                ((width - 2 * size15dp) * 0.83 + size15dp - rect.width() / 2).toFloat(),
                (height / 2).toFloat() - size15dp / 2 - rect.height(), paint
            )
        }

        paint.getTextBounds(hintExtremelyLow, 0, hintExtremelyLow.length, rect)
        canvas?.drawText(
            hintExtremelyLow,
            (((width - 2 * size15dp) * 0.18) / 2).toFloat() + size15dp - rect.width() / 2,
            (height / 2).toFloat() + 2 * size18dp - paint.textSize / 2, paint
        )

        paint.getTextBounds(hintLow, 0, hintLow.length, rect)
        canvas?.drawText(
            hintLow,
            (((width - 2 * size15dp) * 0.18 + (width - 2 * size15dp) * 0.42 + 2 * size15dp) / 2).toFloat()
                    - rect.width() / 2,
            (height / 2).toFloat() + 2 * size18dp - paint.textSize / 2, paint
        )

        paint.getTextBounds(hintMid, 0, hintMid.length, rect)
        canvas?.drawText(
            hintMid,
            (((width - 2 * size15dp) * 0.42 + (width - 2 * size15dp) * 0.83 + 2 * size15dp) / 2).toFloat()
                    - rect.width() / 2,
            (height / 2).toFloat() + 2 * size18dp - paint.textSize / 2, paint
        )

        paint.getTextBounds(hintHigh, 0, hintHigh.length, rect)
        canvas?.drawText(
            hintHigh,
            (((width - 2 * size15dp) * 0.83 + width) / 2).toFloat()
                    - rect.width() / 2,
            (height / 2).toFloat() + 2 * size18dp - paint.textSize / 2, paint
        )

        val cursor = value / 6f * (width - 2 * size15dp) + size15dp

        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = DensityUtils.sp2px(15f)
        paint.getTextBounds(valueString, 0, valueString.length, rect)
        canvas?.drawText(
            valueString,
            cursor - rect.width() / 2,
            (height / 2).toFloat() - size15dp / 2 - rect.height(), paint
        )

        var bitmap: Bitmap? = null
        if (value <= 1.1f) {
            bitmap = extremelyLowBitmap
        } else if (value > 1.1f && value <= 2.5f) {
            bitmap = lowBitmap
        } else if (value > 2.5f && value <= 5f) {
            bitmap = midBitmap
        } else if (value > 5f) {
            bitmap = highBitmap
        }
        bitmap?.let {
            rect.set(
                (cursor - size15dp / 2).toInt(),
                height / 2 - DensityUtils.dp2px(12f),
                (cursor + size15dp / 2).toInt(),
                height / 2 + DensityUtils.dp2px(12f)
            )
            canvas?.drawBitmap(it, null, rect, paint)
        }
    }
}