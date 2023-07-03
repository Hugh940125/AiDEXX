package com.microtech.aidexx.service.push

open class PushMessage {

    //taskid和messageid字段，是用于回执上报的必要参数。详情见下方文档“6.2 上报透传消息的展示和点击数据”
    var taskId: String? = null
    var messageId: String? = null

    //-1 cgm无效数据 1高血糖 2-低血糖，3-紧急低
    // 4-饮食事件变更，5运动事件变更，6用药事件变更，7德岛素事件变更，8-其他事件变更，
    // 9-指血数据变更，10-日志上报
    private val msgType: String? = null

    val detail: String? = null

    fun getRealMsgByType(): PushMessage? {
        when (msgType) {

        }
        return null
    }

    open fun applyMsg(): Boolean = false
}

data class PushCgmMsg(
    val msgType: String,

): PushMessage() {
    override fun applyMsg(): Boolean {
        return super.applyMsg()
    }
}