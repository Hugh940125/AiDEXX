package com.microtech.aidexx.common

import android.content.Context
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.db.entity.CgmHistoryEntity
import com.microtech.aidexx.ui.main.home.chart.CgmModel
import com.microtech.aidexx.utils.LocalManageUtil
import com.microtech.aidexx.utils.UnitManager
import io.objectbox.Property
import io.objectbox.query.QueryBuilder
import java.math.BigDecimal
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

/**
 *@date 2023/2/22
 *@author Hugh
 *@desc
 */

const val baseUuid = "00000000-0000-1000-8000-00805F9B34FB"

fun Int.toUuid(): UUID {
    var hexString = Integer.toHexString(this).uppercase()
    if (hexString.length < 4) {
        val stringBuilder = StringBuilder()
        for (i in 0 until 4 - hexString.length) {
            stringBuilder.append("0")
        }
        stringBuilder.append(hexString)
        hexString = stringBuilder.toString()
    }
    return UUID.fromString(baseUuid.replaceRange(4, 8, hexString))
}

fun UUID.toIntLittleEndian(): Int {
    val parseInt = Integer.parseInt(this.toString().substring(5, 9), 16)
    val buffer = ByteBuffer.allocate(4)
    buffer.order(ByteOrder.BIG_ENDIAN)
    buffer.asIntBuffer().put(parseInt)
    buffer.order(ByteOrder.LITTLE_ENDIAN)
    return buffer.asIntBuffer().get()
}

fun Int.toColor(context: Context): Int = ContextCompat.getColor(context, this)

fun UUID.toIntBigEndian(): Int = Integer.parseInt(this.toString().substring(4, 8), 16)

fun Long.millisToMinutes(): Int =
    BigDecimal(this).divide(BigDecimal(60 * 1000)).toInt()

fun Long.millisToSeconds(): Long = BigDecimal(this).divide(BigDecimal(1000)).toLong()

fun Long.millisToHours(): Int = BigDecimal(this).divide(BigDecimal(60 * 60 * 1000)).toInt()

fun Date.date2ymdhm(pattern: String = "yyyy/MM/dd HH:mm"): String? =
    SimpleDateFormat(pattern, Locale.getDefault()).format(this)

fun Date.dateAndTimeHour(pattern: String = "HH:mm"): String? =
    SimpleDateFormat(pattern, Locale.ENGLISH).format(this)

fun <T> QueryBuilder<T>.equal(property: Property<T>, value: String): QueryBuilder<T> {
    return equal(property, value, QueryBuilder.StringOrder.CASE_SENSITIVE)
}

fun GsonBuilder.createWithDateFormat(): Gson {
    return setDateFormat("yyyy-MM-dd HH:mm:ssZ").create()
}

fun Float.toGlucoseString2(): String {

    return when {
        this <= if (UnitManager.glucoseUnit == UnitManager.GlucoseUnit.MMOL_PER_L) CgmModel.GLUCOSE_LOWER else CgmModel.GLUCOSE_LOWER * 18 -> "LO"

        this >= if (UnitManager.glucoseUnit == UnitManager.GlucoseUnit.MMOL_PER_L) CgmModel.GLUCOSE_UPPER else CgmModel.GLUCOSE_UPPER * 18
        -> "HI"
        else -> UnitManager.formatterUnitByIndex().format(this)
    }
}

fun Float.toGlucoseValue(): Float {
    return when (UnitManager.glucoseUnit) {
        UnitManager.GlucoseUnit.MMOL_PER_L -> this
        UnitManager.GlucoseUnit.MG_PER_DL -> this * 18f
    }
}

fun String.convertPointer(): String {
    val POINTER =
        DecimalFormatSymbols.getInstance(LocalManageUtil.getSetLanguageLocale(AidexxApp.instance)).decimalSeparator.toString()
    return replaceFirst(",", POINTER).replaceFirst(".", POINTER)
}

fun String.convertAllPointer(): String {
    val POINTER =
        DecimalFormatSymbols.getInstance(LocalManageUtil.getSetLanguageLocale(AidexxApp.instance)).decimalSeparator.toString()
    return replace(",", POINTER).replace(".", POINTER)
}

fun Number.stripTrailingZeros(scale: Int? = null): String {
    return (if (scale != null) {
        BigDecimal(this.toString()).setScale(scale, BigDecimal.ROUND_HALF_DOWN).stripTrailingZeros()
            .toPlainString()
    } else {
        BigDecimal(this.toString()).stripTrailingZeros().toPlainString()
    }).convertAllPointer()
}


inline fun <reified T> getMutableListType() = object : TypeToken<MutableList<T>>() {}.type

fun getContext() = AidexxApp.instance