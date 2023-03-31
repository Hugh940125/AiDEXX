package com.microtech.aidexx.utils.eventbus

import com.microtech.aidexx.db.entity.RealCgmHistoryEntity

data class AlertInfo(var content: String,val type:Int)

enum class DataChangedType {
    ADD, DELETE, UPDATE
}

typealias CgmDataChangedInfo = Pair<DataChangedType,List<RealCgmHistoryEntity>>