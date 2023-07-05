package com.microtech.aidexx.ui.main.trend

import com.microtech.aidexx.db.entity.RealCgmHistoryEntity


class MultiDayBgItem(
    var dateDesc: Pair<Int, Int>,
    var checked: Boolean,
    var histories: List<RealCgmHistoryEntity>?,
    var color: Int
)