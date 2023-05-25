package com.microtech.aidexx.utils.eventbus

import com.microtech.aidexx.db.entity.BloodGlucoseEntity
import com.microtech.aidexx.db.entity.CalibrateEntity
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity

data class AlertInfo(
    var content: String,
    val type: Int,
    val showCustomerService: Boolean
)

enum class DataChangedType {
    ADD, DELETE, UPDATE
}

typealias CgmDataChangedInfo = Pair<DataChangedType, List<RealCgmHistoryEntity>>
typealias BgDataChangedInfo = Pair<DataChangedType, List<BloodGlucoseEntity>>
typealias CalDataChangedInfo = Pair<DataChangedType, List<CalibrateEntity>>