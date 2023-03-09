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
    BigDecimal(this).divide(BigDecimal(1000)).divide(BigDecimal(60)).toInt()

fun Long.millisToSeconds(): Long = BigDecimal(this).divide(BigDecimal(1000)).toLong()

fun Date.date2ymdhm(pattern: String = "yyyy/MM/dd HH:mm"): String? =
    SimpleDateFormat(pattern, Locale.getDefault()).format(this)