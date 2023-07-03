package com.microtech.aidexx.service.push

import android.content.Context
import com.google.gson.Gson
import com.igexin.sdk.GTIntentService
import com.igexin.sdk.message.GTCmdMessage
import com.igexin.sdk.message.GTNotificationMessage
import com.igexin.sdk.message.GTTransmitMessage
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.repository.AccountRepository
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.mmkv.MmkvManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class GeTuiIntentService: GTIntentService() {

    companion object {
        private val tag = GeTuiIntentService::class.java.simpleName
    }

    override fun onReceiveServicePid(context: Context?, pid: Int) {}

    /**
     * 此方法用于接收和处理透传消息。透传消息个推只传递数据，不做任何处理，客户端接收到透传消息后需要自己去做后续动作处理，如通知栏展示、弹框等。
     * 如果开发者在客户端将透传消息创建了通知栏展示，建议将展示和点击回执上报给个推。
     */
    override fun onReceiveMessageData(context: Context?, msg: GTTransmitMessage?) {
        val payload = msg?.payload
        payload?.let { msgBarr ->
            val data = String(msgBarr)

            LogUtil.d(data, tag)
            try {
                Gson().fromJson(data, PushMessage::class.java).also {
                    it.messageId = msg.messageId
                    it.taskId = msg.taskId
                }
            } catch (e: Exception) {
                LogUtil.xLogE("推送消息异常：$e", tag)
                null
            }?.getRealMsgByType()?.let {
                if (it.applyMsg()) {
                    LogUtil.d("")
                } else {
                    LogUtil.xLogE("推送消息处理失败", tag)
                }
            }
        } ?:let {
            LogUtil.xLogE("推送消息接收异常", tag)
        }
    }

    // 接收 cid
    override fun onReceiveClientId(context: Context?, clientid: String) {
        LogUtil.d("CLIENTID=$clientid", tag)
        if (MmkvManager.isLogin()) {
            GlobalScope.launch {
                when (val ret = AccountRepository.getuiLogin(clientid)) {
                    is ApiResult.Success -> LogUtil.d("cid 上传成功", tag)
                    else -> LogUtil.d("cid 上传失败", tag)
                }
            }
        }
    }

    // cid 离线上线通知
    override fun onReceiveOnlineState(context: Context?, online: Boolean) {
        LogUtil.xLogI("o=$online", tag)
    }

    // 各种事件处理回执
    override fun onReceiveCommandResult(context: Context?, cmdMessage: GTCmdMessage?) {}

    // 通知到达，只有个推通道下发的通知会回调此方法
    override fun onNotificationMessageArrived(context: Context?, msg: GTNotificationMessage?) {}

    // 通知点击，只有个推通道下发的通知会回调此方法
    override fun onNotificationMessageClicked(context: Context?, msg: GTNotificationMessage?) {}


}