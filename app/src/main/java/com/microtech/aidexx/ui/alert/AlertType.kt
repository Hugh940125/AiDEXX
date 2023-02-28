package com.microtech.aidexx.ui.alert

/**
 *@date 2023/2/28
 *@author Hugh
 *@desc
 */
object AlertType {
    const val MESSAGE_TYPE_GLUCOSEHIGH = 1 //  高血糖紧急报警
    const val MESSAGE_TYPE_GLUCOSELOW = 2 //  低血糖紧急报警
    const val MESSAGE_TYPE_GLUCOSELOWALERT = 3 //  低血糖紧急提醒
    const val MESSAGE_TYPE_GLUCOSEDOWN = 4 //  血糖数据快速下降
    const val MESSAGE_TYPE_GLUCOSEUP = 5  //  血糖数据快速上升
    const val MESSAGE_TYPE_SIGNLOST = 6  // 信号丢失
    const val MESSAGE_TYPE_SENRORERROR = 7  // 传感器故障
    const val MESSAGE_TYPE_NEWSENROR = 8  // 新传感器
    const val MESSAGE_TYPE_SENROR_EMBEDDING = 10  // 传感器植入失败
    const val MESSAGE_TYPE_SENROR_EMBEDDING_SUPER = 11  // 传感器植入失败升级版
    const val MESSAGE_NEW_SENSOR_ERROR = 12  // 新传感器确认失败5次
    const val MESSAGE_UPDATE_NOTICATION = 14  // 更新通知
    const val MESSAGE_TYPE_DEVICEERROR = 1001  //设备时间不对
    const val MESSAGE_TYPE_ERROR_HIGHORLOW = 9  //高低血糖异常
}