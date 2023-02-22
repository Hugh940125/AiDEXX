package com.microtech.aidexx.common

import java.math.BigDecimal

/**
 *@date 2023/2/22
 *@author Hugh
 *@desc
 */

fun Long.millisToMinutes(): Int =
    BigDecimal(this).divide(BigDecimal(1000)).divide(BigDecimal(60)).toInt()

fun Long.millisToSeconds(): Long = BigDecimal(this).divide(BigDecimal(1000)).toLong()