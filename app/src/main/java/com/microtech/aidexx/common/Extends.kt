package com.microtech.aidexx.common

import android.content.Context
import androidx.core.content.ContextCompat
import java.math.BigDecimal
import java.nio.ByteBuffer
import java.nio.ByteOrder
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