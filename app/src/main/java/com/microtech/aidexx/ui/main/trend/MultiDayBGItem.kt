package com.microtech.aidexx.ui.main.trend

import com.microtech.aidexx.db.entity.RealCgmHistoryEntity


class MultiDayBGItem(
    var dateDesc: String?,
    var checked: Boolean,
    var startTime: Long?,
    var endTime: Long?,
    var histories: List<RealCgmHistoryEntity>?,
    var color:Int
)