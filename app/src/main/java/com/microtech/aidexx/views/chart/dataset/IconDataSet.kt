package com.microtech.aidexx.views.chart.dataset

import android.graphics.Color
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.utils.MPPointF
import com.microtech.aidexx.R
import com.microtech.aidexx.utils.BitmapUtils
import java.util.concurrent.CopyOnWriteArrayList

class IconDataSet : ScatterDataSet(CopyOnWriteArrayList<Entry>(), "icon") {

    companion object {
        private const val size = 40f
        val insulinIcon = BitmapUtils.getBitmapFromResource(
            R.drawable.ic_yds,
            size,
            size
        )
        val dietIcon = BitmapUtils.getBitmapFromResource(R.drawable.ic_have_food,
            size,
            size
        )
        val medicineIcon = BitmapUtils.getBitmapFromResource(
            R.drawable.ic_use_medical,
            size,
            size
        )
        val exerciseIcon =
            BitmapUtils.getBitmapFromResource(
                R.drawable.ic_excise,
                size,
                size
            )
        val otherMarkIcon =
            BitmapUtils.getBitmapFromResource(
                R.drawable.ic_other_mark,
                size,
                size
            )


    }

    init {
        axisDependency = YAxis.AxisDependency.RIGHT
        isAttachedToLineDataSet = true
        color = Color.TRANSPARENT
        valueTextSize = 10f
//        isHighlightEnabled = false
        highLightColor = Color.GRAY
        setDrawHorizontalHighlightIndicator(false)
        iconsOffset = MPPointF(0f, -10f)
        setDrawValues(false)
    }
}