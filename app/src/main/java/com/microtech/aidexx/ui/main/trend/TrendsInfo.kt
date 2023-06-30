package com.microtech.aidexx.ui.main.trend

import com.microtech.aidexx.ble.device.model.GLUCOSE_NUM_ONE_DAY
import com.microtech.aidexx.utils.ThresholdManager
import com.microtech.aidexx.utils.toGlucoseString
import com.microtech.aidexx.utils.toGlucoseStringWithUnit

/**
 *@date 2023/6/30
 *@author Hugh
 *@desc
 */
class TrendsInfo {
    var pageTitle: String? = null
    var coverTime: String = "0"
    var monitorTimes: String = "0"
    var eHbA1c: String = "--"
    var showEhbA1cUnit = false
    var showEhbA1cTrend = false
    var mbg: String = "--"
    var showMbgUnit = false
    var showMbgTrend = false
    var lowPercent = 0.0
    var lowPercentDisplay = if (lowPercent == 0.0) "--%" else "$lowPercent%"
    var lowPercentDesc = buildString {
        append(" < ")
        append(ThresholdManager.hypo.toGlucoseStringWithUnit())
    }
    var showLowPercentTrend = false
    var normalPercent = 0.0
    var normalPercentDisplay = if (normalPercent == 0.0) "--%" else "$normalPercent%"
    var normalPercentDesc = buildString {
        append(ThresholdManager.hypo.toGlucoseString())
        append(" - ")
        append(ThresholdManager.hyper.toGlucoseStringWithUnit())
    }
    var showNormalPercentTrend = false
    var highPercent = 0.0
    var highPercentDisplay = if (highPercent == 0.0) "--%" else "$highPercent%"
    var highPercentDesc = buildString {
        append(" > ")
        append(ThresholdManager.hyper.toGlucoseStringWithUnit())
    }
    var showHightPercentTrend = false
    var dailyP10: DoubleArray? = DoubleArray(GLUCOSE_NUM_ONE_DAY)
    var dailyP25: DoubleArray? = DoubleArray(GLUCOSE_NUM_ONE_DAY)
    var dailyP50: DoubleArray? = DoubleArray(GLUCOSE_NUM_ONE_DAY)
    var dailyP75: DoubleArray? = DoubleArray(GLUCOSE_NUM_ONE_DAY)
    var dailyP90: DoubleArray? = DoubleArray(GLUCOSE_NUM_ONE_DAY)
    var lbgi = 0f
}