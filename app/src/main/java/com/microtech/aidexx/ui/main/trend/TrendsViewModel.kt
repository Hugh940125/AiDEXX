package com.microtech.aidexx.ui.main.trend

import android.view.View
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.device.model.GLUCOSE_NUM_ONE_DAY
import com.microtech.aidexx.utils.ThresholdManager
import com.microtechmd.cgat.CGA
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.roundToInt

class TrendsViewModel : BaseViewModel() {

    private fun runCgat(glucoseArray: Array<DoubleArray>, dayCount: Int) {
//        val cgat = CGA(glucoseArray)
//        val dailyP10: DoubleArray? = getValidArray(cgat.getDailyTrendPrctile(10.0))
//        val dailyP25: DoubleArray? = getValidArray(cgat.getDailyTrendPrctile(25.0))
//        val dailyP50: DoubleArray? = getValidArray(cgat.getDailyTrendPrctile(50.0))
//        val dailyP75: DoubleArray? = getValidArray(cgat.getDailyTrendPrctile(75.0))
//        val dailyP90: DoubleArray? = getValidArray(cgat.getDailyTrendPrctile(90.0))
//        val num = cgat.periodNUM.last().toInt()
//        val pt = cgat.getPeriodPT(
//            doubleArrayOf(0.0, ThresholdManager.hypo.toDouble(), ThresholdManager.hyper.toDouble(), 30.0)
//        )
//        var lpt = if (num < GLUCOSE_NUM_ONE_DAY) 0.0 else pt[pt.size - 1][0]
//        var mpt = if (num < GLUCOSE_NUM_ONE_DAY) 0.0 else pt[pt.size - 1][1]
//        var hpt = if (num < GLUCOSE_NUM_ONE_DAY) 0.0 else pt[pt.size - 1][2]
//        val mbg = if (num < GLUCOSE_NUM_ONE_DAY) 0.0 else cgat.periodMBG.last()
//        val lbgi = if (num < GLUCOSE_NUM_ONE_DAY) 0.0 else cgat.lbgi
//        val result = (num.toFloat() * 100 / (dayCount * GLUCOSE_NUM_ONE_DAY)).roundToInt()
//        vb.tvCoverTimeValue.text = "$result"
//        vb.tvMonitorTimesValue.text = "$num"
//        val lbgiDecimal = BigDecimal(lbgi)
//        val zero = BigDecimal.ZERO
//        val lbgiResult = zero.add(lbgiDecimal.setScale(1, RoundingMode.FLOOR))
//        vb.cursorView.setValue(lbgiResult.toFloat())
//        val hba1c = cgat.hbA1C
//        if (hba1c.isNaN()) {
//            vb.trendHemoglobin.text = "--"
//            vb.ivHemoglobinTrends.visibility = View.GONE
//        } else {
//            vb.trendHemoglobin.text = (hba1c).To1Num()
//            if (hba1c >= 7) {
//                vb.ivHemoglobinTrends.visibility = View.VISIBLE
//            } else {
//                vb.ivHemoglobinTrends.visibility = View.GONE
//            }
//        }
//        if (num < ONE_DAY_NUM_GLUCOSE) {
//            vb.tvGlucoseAverage.text = "--"
//        } else {
//            vb.tvGlucoseAverage.text = mbg.toGlucoseString(resources)
//        }
//        if ((mbg > 6.6 || mbg < 4.3) && num > ONE_DAY_NUM_GLUCOSE) {
//            vb.ivGlucoseTrends.visibility = View.VISIBLE
//        } else {
//            vb.ivGlucoseTrends.visibility = View.GONE
//        }
//        vb.tvUnit.text = UnitManager.glucoseUnit.text
//        vb.descHigh.text = buildString {
//            append(" > ")
//            append(ThresholdManager.hyperThreshold.toGlucoseStringWithUnit(resources))
//        }
//        vb.descLow.text = buildString {
//            append(" < ")
//            append(ThresholdManager.hypoThreshold.toGlucoseStringWithUnit(resources))
//        }
//        vb.descNormal.text = buildString {
//            append(ThresholdManager.hypoThreshold.toGlucoseString(resources))
//            append(" - ")
//            append(ThresholdManager.hyperThreshold.toGlucoseString(resources))
//            append(UnitManager.glucoseUnit.text)
//        }
//        if (hpt.isNaN()) {
//            hpt = 0.0
//            "--%".also {
//                vb.highPercent.text = it
//            }
//        } else {
//            hpt = DecimalFormat("0.0").format(hpt).toDouble()
//            vb.highPercent.text =
//                buildString {
//                    append(hpt)
//                    append("%")
//                }
//            if (hpt >= 25) {
//                vb.ivHighTrends.visibility = View.VISIBLE
//            } else {
//                vb.ivHighTrends.visibility = View.GONE
//            }
//        }
//
//        if (lpt.isNaN()) {
//            lpt = 0.0
//            vb.lowPercent.text = "--%"
//        } else {
//            lpt = if (lpt != 0.0 && (hpt != 0.0 || mpt != 0.0)) {
//                DecimalFormat("0.0").format(
//                    (100f - DecimalFormat("0.0").format(hpt)
//                        .toFloat() - DecimalFormat("0.0").format(mpt).toFloat())
//                ).toDouble()
//            } else {
//                DecimalFormat("0.0").format(lpt).toDouble()
//            }
//            vb.lowPercent.text =
//                buildString {
//                    append(lpt)
//                    append("%")
//                }
//            if (lpt >= 4) {
//                vb.ivLowTrends.visibility = View.VISIBLE
//            } else {
//                vb.ivLowTrends.visibility = View.GONE
//            }
//        }
//
//        if (mpt.isNaN()) {
//            mpt = 0.0
//            vb.normalPercent.text = "--%"
//        } else {
//            mpt = DecimalFormat("0.0").format(mpt).toDouble()
//            vb.normalPercent.text =
//                buildString {
//                    append(mpt)
//                    append("%")
//                }
//        }
//
//        updatePieChart(
//            mpt,
//            hpt,
//            lpt,
//            glucoseArray.size
//        )
//
//        if (num < 5 * ONE_DAY_NUM_GLUCOSE) {
//            LogUtils.error("小于五天,不更新趋势表")
//            updateLineChart(
//                DoubleArray(288),
//                DoubleArray(288),
//                DoubleArray(288),
//                DoubleArray(288),
//                DoubleArray(288)
//            )
//        } else {
//            updateLineChart(
//                dailyP50,
//                dailyP75,
//                dailyP25,
//                dailyP90,
//                dailyP10
//            )
//        }
    }

    private fun getValidArray(array: DoubleArray, ratio: Float? = 0.75f): DoubleArray? {
        var nanCount = 0
        for (v in array) {
            if (v.isNaN()) nanCount++
        }
        return if (nanCount > array.size * (1 - ratio!!)) null else array
    }
}
