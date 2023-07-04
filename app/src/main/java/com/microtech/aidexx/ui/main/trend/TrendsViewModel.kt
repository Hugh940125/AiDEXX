package com.microtech.aidexx.ui.main.trend

import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.device.model.GLUCOSE_NUM_ONE_DAY
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.repository.CgmCalibBgRepository
import com.microtech.aidexx.utils.ThresholdManager
import com.microtech.aidexx.utils.TimeUtils
import com.microtech.aidexx.utils.toGlucoseValue
import com.microtechmd.cgat.CGA
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.DecimalFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

class TrendsViewModel : BaseViewModel() {

    val cgatFlow = MutableStateFlow<TrendsInfo?>(null)
    private var oneDigitFormat: DecimalFormat = DecimalFormat("0.0")

    suspend fun runCgat(startDate: Date, endDate: Date) {
        val beginTime = startDate.time
        val cgmList =
            CgmCalibBgRepository.queryCgmByPage(
                startDate,
                endDate,
                UserInfoManager.getCurShowUserId()
            )
        val dayCount = ceil((endDate.time - startDate.time) / 1000.0 / 86400.0).toInt()
        val glucoseArray = Array(dayCount) { DoubleArray(GLUCOSE_NUM_ONE_DAY) }
        cgmList?.let {
            it.sortBy { cgm -> cgm.deviceTime }
            var lastHistoryTime = 0L
            var historyCount = 0
            for (dbHistory in cgmList) {
                val time: Long = dbHistory.deviceTime.time
                if (abs(time - lastHistoryTime) < 5 * TimeUtils.oneMinuteMillis) {
                    continue
                }
                lastHistoryTime = time
                historyCount++
                val x = ((time - beginTime) / TimeUtils.oneDayMillis).toInt()
                val y =
                    ((time - beginTime) / (5 * TimeUtils.oneMinuteMillis)).toInt() - x * GLUCOSE_NUM_ONE_DAY
                if (x >= dayCount || y > GLUCOSE_NUM_ONE_DAY) {
                    continue
                }
                dbHistory.glucose?.let { history ->
                    val glucoseValue = history.toGlucoseValue()
                    if (glucoseValue.toDouble() < 2) {
                        glucoseArray[x][y] = 2.0
                    } else if (glucoseValue.toDouble() > 25) {
                        glucoseArray[x][y] = 25.0
                    } else {
                        glucoseArray[x][y] = glucoseValue.toDouble()
                    }
                }
            }
            val trendsInfo = TrendsInfo()
            val cgat = CGA(glucoseArray)
            val dailyP10: DoubleArray? = getValidArray(cgat.getDailyTrendPrctile(10.0))
            val dailyP25: DoubleArray? = getValidArray(cgat.getDailyTrendPrctile(25.0))
            val dailyP50: DoubleArray? = getValidArray(cgat.getDailyTrendPrctile(50.0))
            val dailyP75: DoubleArray? = getValidArray(cgat.getDailyTrendPrctile(75.0))
            val dailyP90: DoubleArray? = getValidArray(cgat.getDailyTrendPrctile(90.0))
            val num = cgat.periodNUM.last().toInt()
            val ehba1c = cgat.hbA1C
            val mbg = cgat.periodMBG.last()
            trendsInfo.coverTime =
                (num * 100f / (dayCount * GLUCOSE_NUM_ONE_DAY)).roundToInt().toString()
            trendsInfo.monitorTimes = num.toString()
            if (!ehba1c.isNaN()) {
                trendsInfo.eHbA1c = oneDigitFormat.format(ehba1c)
                trendsInfo.showEhbA1cTrend = ehba1c >= 7
            }
            if (num > GLUCOSE_NUM_ONE_DAY) {
                trendsInfo.mbg = oneDigitFormat.format(mbg)
                trendsInfo.showMbgUnit = true
                if (!mbg.isNaN() && (mbg > 6.6 || mbg < 4.3)) {
                    trendsInfo.showMbgTrend = true
                }
                val pt = cgat.getPeriodPT(
                    doubleArrayOf(
                        0.0,
                        ThresholdManager.hypo.toDouble(),
                        ThresholdManager.hyper.toDouble(),
                        30.0
                    )
                )
                trendsInfo.lowPercent = oneDigitFormat.format(pt[pt.size - 1][0]).toDouble()
                trendsInfo.highPercent = oneDigitFormat.format(pt[pt.size - 1][2]).toDouble()
                trendsInfo.normalPercent = 100.0 - trendsInfo.lowPercent - trendsInfo.highPercent
                trendsInfo.lbgi = cgat.lbgi
                if (historyCount > 5 * GLUCOSE_NUM_ONE_DAY) {
                    trendsInfo.dailyP10 = dailyP10
                    trendsInfo.dailyP25 = dailyP25
                    trendsInfo.dailyP50 = dailyP50
                    trendsInfo.dailyP75 = dailyP75
                    trendsInfo.dailyP90 = dailyP90
                }
            }
            val findDataByDays = MultiDayBGManager.getInstance().findDataByDays(startDate, endDate)
            trendsInfo.multiDayHistory = findDataByDays
            cgatFlow.emit(trendsInfo)
        }
    }

    private fun getValidArray(array: DoubleArray, ratio: Float? = 0.75f): DoubleArray? {
        var nanCount = 0
        for (v in array) {
            if (v.isNaN()) nanCount++
        }
        return if (nanCount > array.size * (1 - ratio!!)) null else array
    }
}
