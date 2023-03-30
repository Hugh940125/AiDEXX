package com.microtech.aidexx.utils.eventbus

/**
 *@date 2023/2/9
 *@desc 统一定义EventBus key值
 */
object EventBusKey {
    const val TOKEN_EXPIRED: String = "token.expired"
    const val EVENT_SHOW_ALERT: String = "event.show.alert"
    const val EVENT_RESTART_BLUETOOTH: String = "event.restart.bluetooth"
    const val EVENT_GO_TO_HISTORY: String = "EVENT_GO_TO_HISTORY"
    const val EVENT_UNPAIR_SUCCESS: String = "EVENT_UNPAIR_SUCCESS"
    const val EVENT_CGM_DATA_CHANGED: String = "EVENT_CGM_DATA_CHANGED"
}