package com.microtech.aidexx.ui.main.trend

import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import java.util.*


class MultiDayBgItem(
    var dateDesc: Calendar,
    var checked: Boolean,
    var startTime: Long?,
    var endTime: Long?,
    var histories: List<RealCgmHistoryEntity>?,
    var color:Int
)