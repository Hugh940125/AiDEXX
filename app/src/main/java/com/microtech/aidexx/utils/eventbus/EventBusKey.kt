package com.microtech.aidexx.utils.eventbus

/**
 *@date 2023/2/9
 *@desc 统一定义EventBus key值
 */
object EventBusKey {
    const val TOKEN_EXPIRED: String = "TOKEN_EXPIRED"
    const val EVENT_SHOW_ALERT: String = "EVENT_SHOW_ALERT"
    const val EVENT_RESTART_BLUETOOTH: String = "EVENT_REBOOT_BLUETOOTH"
    const val REFRESH_CHART_DATE: String = "REFRESH_CHART_DATE"
    const val GO_TO_HISTORY: String = "GO_TO_HISTORY"
    const val EVENT_UNPAIR_SUCCESS: String = "EVENT_UNPAIR_SUCCESS"
    const val EVENT_PAIR_SUCCESS: String = "EVENT_PAIR_SUCCESS"
}