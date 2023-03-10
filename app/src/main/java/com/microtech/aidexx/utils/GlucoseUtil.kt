package com.microtech.aidexx.widget.dialog.x.util

import android.content.res.Resources
import com.microtech.aidexx.R
import com.microtech.aidexx.utils.ThresholdManager
import com.microtech.aidexx.utils.unit.UnitManager
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

fun Float.toGlucoseValue(): Float {
    return when (UnitManager.glucoseUnit) {
        UnitManager.GlucoseUnit.MMOL_PER_L -> this
        UnitManager.GlucoseUnit.MG_PER_DL -> this * 18
    }
}

fun Float.toCgatInt(): String {
    return BigDecimal("${this.toGlucoseValue()}").setScale(0, RoundingMode.DOWN).toPlainString()
}


fun Float.toCgat(): String {
    return DecimalFormat("0.0").format(this.toGlucoseValue())
}

fun Double.toCgat(): String {
    return DecimalFormat("0.0").format(this.toGlucoseValue())
}

fun Double.toGlucoseValue(): Double {

    return when (UnitManager.glucoseUnit) {
        UnitManager.GlucoseUnit.MMOL_PER_L -> this
        UnitManager.GlucoseUnit.MG_PER_DL -> this * 18
    }
}

fun Float.fromGlucoseValue(): Float {
    return when (UnitManager.glucoseUnit) {
        UnitManager.GlucoseUnit.MMOL_PER_L -> this
        UnitManager.GlucoseUnit.MG_PER_DL -> this / 18
    }
}

fun Double.fromGlucoseValue(): Double {
    return when (UnitManager.glucoseUnit) {
        UnitManager.GlucoseUnit.MMOL_PER_L -> this
        UnitManager.GlucoseUnit.MG_PER_DL -> this / 18
    }
}

fun Float.toGlucoseString(): String {
    return UnitManager.unitFormat().format(this.toGlucoseValue())
}

fun Float.toGlucoseStringWithLowAndHigh(context: Resources): String {
    return when {
        this < ThresholdManager.GLUCOSE_LOW_LIMIT -> context.getString(R.string.Glucose_Low)
        this > ThresholdManager.GLUCOSE_UP_LIMIT -> context.getString(R.string.Glucose_High)
        else -> UnitManager.unitFormat().format(this.toGlucoseValue())
    }
}

fun Float.toStringWithLoAndHi(): String {
    return when {
        this <= if (UnitManager.glucoseUnit == UnitManager.GlucoseUnit.MMOL_PER_L) ThresholdManager.GLUCOSE_LOW_LIMIT
        else ThresholdManager.GLUCOSE_LOW_LIMIT * 18 -> "LO"

        this >= if (UnitManager.glucoseUnit == UnitManager.GlucoseUnit.MMOL_PER_L) ThresholdManager.GLUCOSE_UP_LIMIT
        else ThresholdManager.GLUCOSE_UP_LIMIT * 18 -> "HI"

        else -> UnitManager.unitFormat().format(this)
    }
}

fun Double.toGlucoseString(context: Resources): String {
    return when {
        this.isNaN() -> "--"
        this < ThresholdManager.GLUCOSE_LOW_LIMIT -> context.getString(R.string.Glucose_Low)
        this > ThresholdManager.GLUCOSE_UP_LIMIT -> context.getString(R.string.Glucose_High)
        else -> UnitManager.unitFormat().format(this.toGlucoseValue())
    }
}

fun Float.toGlucoseStringWithUnit(): String {
    return this.toGlucoseString() + " " + UnitManager.glucoseUnit.text
}

fun Double.toGlucoseStringWithUnit(context: Resources): String {
    return this.toGlucoseString(context) + " " + UnitManager.glucoseUnit.text
}

fun roundOffDecimal(number: Float): Float {
    val df = DecimalFormat("#.#")
    df.roundingMode = RoundingMode.CEILING
    return df.format(number).toFloat()
}