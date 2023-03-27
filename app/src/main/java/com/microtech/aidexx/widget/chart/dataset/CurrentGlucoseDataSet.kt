package com.microtech.aidexx.widget.chart.dataset

import android.graphics.Color

class CurrentGlucoseDataSet: GlucoseDataSet() {

    init {
        lineWidth = 0f

        setDrawCircleHole(true)
        circleRadius = 4f
        circleHoleRadius = 2f
        circleHoleColor = Color.DKGRAY

        setDrawFilled(false)
        setDrawValues(false)


        isHighlightEnabled = false
    }
}