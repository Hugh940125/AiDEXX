package com.microtech.aidexx.views.chart.dataset

import android.graphics.Color
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterDataSet
import com.microtech.aidexx.R
import com.microtech.aidexx.utils.BitmapUtils
import java.util.concurrent.CopyOnWriteArrayList

class BgDataSet : ScatterDataSet(CopyOnWriteArrayList<Entry>(), "bg") {

    companion object {
        private const val size = 40f
        val icon = BitmapUtils.getBitmapFromResource( R.drawable.ic_bg_cal,
            size,
            size
        )
    }

    init {
        axisDependency = YAxis.AxisDependency.RIGHT

        isAttachedToLineDataSet = false
        color = Color.TRANSPARENT
        valueTextSize = 10f
//        isHighlightEnabled = false
        highLightColor = Color.GRAY
        setDrawHorizontalHighlightIndicator(false)
        setDrawValues(false)
    }
}