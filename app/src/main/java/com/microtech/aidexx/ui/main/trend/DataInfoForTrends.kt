package com.microtech.aidexx.ui.main.trend

/**
 *@date 2023/7/4
 *@author Hugh
 *@desc
 */
class DataInfoForTrends(
    var multiDayBgItemList: MutableList<MultiDayBgItem>,
    var glucoseArray: Array<DoubleArray>?,
    var historyCount: Int
)