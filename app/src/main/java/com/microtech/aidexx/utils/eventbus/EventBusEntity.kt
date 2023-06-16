package com.microtech.aidexx.utils.eventbus

import com.microtech.aidexx.db.entity.BaseEventEntity

data class AlertInfo(
    var content: String,
    val type: Int,
    val showCustomerService: Boolean
)

enum class DataChangedType {
    ADD, DELETE, UPDATE
}

typealias EventDataChangedInfo = Pair<DataChangedType, List<BaseEventEntity>>