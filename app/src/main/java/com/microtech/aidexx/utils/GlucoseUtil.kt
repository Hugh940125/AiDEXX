package com.microtech.aidexx.utils

import android.content.res.Resources
import com.microtech.aidexx.R
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

fun Float.toGlucoseValue(): Float {
    return when (UnitManager.glucoseUnit) {
        UnitManager.GlucoseUnit.MMOL_PER_L -> roundOffDecimal(this / 18f)
        UnitManager.GlucoseUnit.MG_PER_DL -> this
    }
}

fun Int.toGlucoseValue(): Float {
    return when (UnitManager.glucoseUnit) {
        UnitManager.GlucoseUnit.MMOL_PER_L -> this / 18f
        UnitManager.GlucoseUnit.MG_PER_DL -> this.toFloat()
    }
}

fun Float.fromGlucoseValue(): Float {
    return when (UnitManager.glucoseUnit) {
        UnitManager.GlucoseUnit.MMOL_PER_L -> this * 18
        UnitManager.GlucoseUnit.MG_PER_DL -> this
    }
}

fun Float.toCgatInt(): String {
    return BigDecimal("${this.toGlucoseValue()}").setScale(0, RoundingMode.DOWN).toPlainString()
}


fun Float.toCgat(): String {
    return DecimalFormat("0.0").format(this.toGlucoseValue())
}

fun Float.toGlucoseString(): String {
    return UnitManager.unitFormat().format(this.toGlucoseValue())
}

fun Float.toGlucoseStringWithLowAndHigh(context: Resources): String {
    return when {
        this <= ThresholdManager.GLUCOSE_LOW_LIMIT -> context.getString(
            R.string.Glucose_Low
        )
        this >= ThresholdManager.GLUCOSE_UP_LIMIT -> context.getString(
            R.string.Glucose_High
        )
        else -> UnitManager.unitFormat().format(this.toGlucoseValue())
    }
}

fun Float.toStringWithLoAndHi(): String {
    return when {
        this <= ThresholdManager.GLUCOSE_LOW_LIMIT -> "LO"

        this >= ThresholdManager.GLUCOSE_UP_LIMIT -> "HI"

        else -> UnitManager.unitFormat().format(this)
    }
}

fun Float.toGlucoseStringWithUnit(): String {
    return this.toGlucoseString() + " " + UnitManager.glucoseUnit.text
}

fun roundOffDecimal(number: Float): Float {
    val df = DecimalFormat("#.#")
    df.roundingMode = RoundingMode.CEILING
    return df.format(number).toFloat()
}