package com.microtech.aidexx.ui.main.trend

import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.device.model.GLUCOSE_NUM_ONE_DAY
import com.microtech.aidexx.utils.ThresholdManager
import com.microtech.aidexx.utils.mmolValueDisplay
import com.microtechmd.cgat.CGA
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.util.Date
import kotlin.math.roundToInt


class TrendsViewModel : BaseViewModel() {

    val cgatFlow = MutableStateFlow<TrendsInfo?>(null)
    private var oneDigitFormat: DecimalFormat = DecimalFormat("0.0")

    suspend fun runCgat(startDate: Date, endDate: Date) {
        val trendsInfo = TrendsInfo()
        val findDataByDays = MultiDayBGManager.getInstance().findDataByDays(startDate, endDate)
        findDataByDays?.let {
            if (it.totalHistory > 0) {
                withContext(Dispatchers.IO) {
                    val cgat = CGA(it.glucoseArray)
                    val num = cgat.periodNUM.last().toInt()
                    val ehba1c = cgat.hbA1C
                    val mbg = cgat.periodMBG.last()
                    trendsInfo.monitorTimes = it.totalHistory.toString()
                    trendsInfo.coverTime =
                        (num * 100f / (it.multiDayBgItemList.size * GLUCOSE_NUM_ONE_DAY)).roundToInt().toString()
                    if (!ehba1c.isNaN()) {
                        trendsInfo.eHbA1c = oneDigitFormat.format(ehba1c)
                        trendsInfo.showEhbA1cTrend = ehba1c >= 7
                    }
                    if (num > GLUCOSE_NUM_ONE_DAY) {
                        trendsInfo.mbg = mbg.toFloat().mmolValueDisplay()
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
                        if (it.historyCount > 5 * GLUCOSE_NUM_ONE_DAY) {
                            val dailyP10: DoubleArray? = getValidArray(cgat.getDailyTrendPrctile(10.0))
                            val dailyP25: DoubleArray? = getValidArray(cgat.getDailyTrendPrctile(25.0))
                            val dailyP50: DoubleArray? = getValidArray(cgat.getDailyTrendPrctile(50.0))
                            val dailyP75: DoubleArray? = getValidArray(cgat.getDailyTrendPrctile(75.0))
                            val dailyP90: DoubleArray? = getValidArray(cgat.getDailyTrendPrctile(90.0))
                            trendsInfo.dailyP10 = dailyP10
                            trendsInfo.dailyP25 = dailyP25
                            trendsInfo.dailyP50 = dailyP50
                            trendsInfo.dailyP75 = dailyP75
                            trendsInfo.dailyP90 = dailyP90
                        }
                    }
                }
            }
        }
        trendsInfo.multiDayHistory = findDataByDays?.multiDayBgItemList
        cgatFlow.emit(trendsInfo)
    }

    private fun getValidArray(array: DoubleArray, ratio: Float? = 0.75f): DoubleArray? {
        var nanCount = 0
        for (v in array) {
            if (v.isNaN()) nanCount++
        }
        return if (nanCount > array.size * (1 - ratio!!)) null else array
    }
}
