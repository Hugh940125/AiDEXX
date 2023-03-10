package com.microtech.aidexx.common

import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

/**
 *@date 2023/2/22
 *@author Hugh
 *@desc
 */

fun Long.millisToMinutes(): Int =
    BigDecimal(this).divide(BigDecimal(60 * 1000)).toInt()

fun Long.millisToSeconds(): Long = BigDecimal(this).divide(BigDecimal(1000)).toLong()

fun Long.millisToHours(): Int = BigDecimal(this).divide(BigDecimal(60 * 60 * 1000)).toInt()

fun Date.date2ymdhm(pattern: String = "yyyy/MM/dd HH:mm"): String? =
    SimpleDateFormat(pattern, Locale.getDefault()).format(this)