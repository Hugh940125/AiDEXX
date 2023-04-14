package com.microtech.aidexx.ble

import com.microtechmd.blecomm.entity.BleMessage

/**
 *@date 2023/4/4
 *@author Hugh
 *@desc
 */
interface MessageObserver {


    fun onMessage(message: BleMessage)
}