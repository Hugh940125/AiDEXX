package com.microtech.aidexx.widget.chart.dataset

import android.graphics.Color
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.R

open class GlucoseDataSet: LineDataSet(ArrayList<Entry>(), "glucose") {

    init {
        axisDependency = YAxis.AxisDependency.RIGHT

        lineWidth = 2f
        mode = Mode.CUBIC_BEZIER

        //线条变成透明的
        colors = listOf(
            Color.TRANSPARENT,
            Color.TRANSPARENT,
            Color.TRANSPARENT,
            Color.TRANSPARENT)

        val green = AidexxApp.instance.resources.getColor(R.color.green)
        val yellow = AidexxApp.instance.resources.getColor(R.color.yellow)
        val red = AidexxApp.instance.resources.getColor(R.color.red)
//        colors = listOf(
//            yellow,
//            green,
//            green,
//            red
//        )

        val trans = Color.argb(150, 255, 255, 255)


        setDrawFilled(true)
        fillAlpha = 255
        fillColors =  listOf(
            alpha(yellow, 100),
            alpha(yellow, 25),
            Color.TRANSPARENT,
            Color.TRANSPARENT,
            alpha(red, 25),
            alpha(red, 100)
        )

        setDrawCircles(true)
        setDrawCircleHole(false)
        circleRadius = 2f
        circleColors = listOf(
            yellow,
            green,
            red,
            red
        )

        setDrawValues(false)

        highLightColor = Color.GRAY
        setDrawHorizontalHighlightIndicator(false)
    }

    companion object {
        private fun alpha(color: Int, alpha: Int): Int {
            return Color.argb(alpha, 255, 255, 255) and color
        }
    }
}