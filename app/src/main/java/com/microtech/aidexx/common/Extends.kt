package com.microtech.aidexx.common

import android.app.Application
import android.content.Context
import android.os.SystemClock
import android.view.View
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.utils.LocalManageUtil
import com.microtech.aidexx.utils.ThresholdManager
import com.microtech.aidexx.utils.ToastUtil
import com.microtech.aidexx.utils.UnitManager
import io.objectbox.Property
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.math.BigDecimal
import java.math.RoundingMode
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 *@date 2023/2/22
 *@author Hugh
 *@desc
 */

const val baseUuid = "00000000-0000-1000-8000-00805F9B34FB"

const val DATE_FORMAT_YMDHMS = "yyyy-MM-dd HH:mm:ss"
const val DATE_FORMAT_YMDHM = "yyyy/MM/dd HH:mm"
const val DATE_FORMAT_HM = "HH:mm"


fun Float.roundTwoDigits(): Float {
    val df = DecimalFormat("#.##")
    df.roundingMode = RoundingMode.HALF_EVEN
    return df.format(this).toFloat()
}

fun Int.toHistoryDate(sensorStartTime: Date): Date {
    val timeLong = sensorStartTime.time.plus(this * 60 * 1000)
    return Date(timeLong)
}

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
    BigDecimal(this).divide(BigDecimal(60 * 1000), RoundingMode.HALF_UP).toInt()

fun Long.millisToSeconds(): Long =
    BigDecimal(this).divide(BigDecimal(1000), RoundingMode.HALF_UP).toLong()

fun Long.millisToIntSeconds(): Int =
    BigDecimal(this).divide(BigDecimal(1000), RoundingMode.HALF_UP).toInt()

fun Long.millisToHours(): Int =
    BigDecimal(this).divide(BigDecimal(60 * 60 * 1000), RoundingMode.HALF_UP).toInt()

fun Date.date2ymdhm(pattern: String = DATE_FORMAT_YMDHM): String? =
    SimpleDateFormat(pattern, Locale.getDefault()).format(this)

fun Date.dateAndTimeHour(pattern: String = DATE_FORMAT_HM): String? =
    SimpleDateFormat(pattern, Locale.ENGLISH).format(this)

fun Date.getStartOfTheDay(): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}

fun Date.formatWithZone(): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ", Locale.ENGLISH).format(this)

fun Date.formatWithoutZone(): String =
    SimpleDateFormat(DATE_FORMAT_YMDHMS, Locale.ENGLISH).format(this)

fun <T> QueryBuilder<T>.equal(property: Property<T>, value: String): QueryBuilder<T> {
    return equal(property, value, QueryBuilder.StringOrder.CASE_SENSITIVE)
}

fun GsonBuilder.createWithDateFormat(): Gson {
    return setDateFormat("yyyy-MM-dd HH:mm:ssZ").create()
}

fun Float.toGlucoseString2(): String {
    return when {
        this <= ThresholdManager.GLUCOSE_LOW_LIMIT -> "LO"
        this >= ThresholdManager.GLUCOSE_UP_LIMIT -> "HI"
        else -> UnitManager.formatterUnitByIndex().format(this)
    }
}

fun String.convertPointer(): String {
    val POINTER =
        DecimalFormatSymbols.getInstance(LocalManageUtil.getSetLanguageLocale(AidexxApp.instance)).decimalSeparator.toString()
    return replaceFirst(",", POINTER).replaceFirst(".", POINTER)
}

fun String.isNumber(): Boolean = try {
    this.toLong()
    true
} catch (e: NumberFormatException) {
    false
}

fun String.toast() = ToastUtil.showLong(this)
fun String.toastShort() = ToastUtil.showShort(this)

fun Number.stripTrailingZeros(scale: Int? = null): String {
    return (if (scale != null) {
        BigDecimal(this.toString()).setScale(scale, BigDecimal.ROUND_HALF_DOWN).stripTrailingZeros()
            .toPlainString()
    } else {
        BigDecimal(this.toString()).stripTrailingZeros().toPlainString()
    })
}

/**
 * 不自动适配逗号小数点
 */
fun Number.stripTrailingZerosWithoutPointer(scale: Int? = null): String {
    return if (scale != null) {
        BigDecimal(this.toString()).setScale(scale, BigDecimal.ROUND_HALF_DOWN).stripTrailingZeros()
            .toPlainString()
    } else {
        BigDecimal(this.toString()).stripTrailingZeros().toPlainString()
    }
}


inline fun <reified T> getMutableListType() = object : TypeToken<MutableList<T>>() {}.type

fun getContext() = AidexxApp.instance

internal fun Number.dp2px() =
    this.toFloat().times(getContext().resources.displayMetrics.density)
        .plus(.5f).toInt()

/**
 * 全局协程作用域用于取代GlobalScope
 */
val Application.ioScope: CoroutineScope
    get() {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

fun View.setDebounceClickListener(time: Long = 500L, listener: View.OnClickListener) {
    var lastClick = 0L
    this.setOnClickListener {
        val elapsedRealtime = SystemClock.elapsedRealtime()
        if (elapsedRealtime - lastClick > time) {
            listener.onClick(it)
            lastClick = elapsedRealtime
        }
    }
}