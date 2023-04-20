package com.microtechmd.blecomm;/**
 * @Description:
 * @Author: Hugh
 * @CreateDate: 2023/4/6 13:26
 */

import android.bluetooth.BluetoothDevice;

import com.microtechmd.blecomm.controller.BleControllerInfo;

/**
 *@date 2023/4/6
 *@author Hugh
 *@desc
 */
public class BleInfo {
    public BleControllerInfo bleControllerInfo;
    public BluetoothDevice device;

    public BleInfo(BleControllerInfo bleControllerInfo, BluetoothDevice device) {
        this.bleControllerInfo = bleControllerInfo;
        this.device = device;
    }

    public BleControllerInfo getBleControllerInfo() {
        return bleControllerInfo;
    }

    public void setBleControllerInfo(BleControllerInfo bleControllerInfo) {
        this.bleControllerInfo = bleControllerInfo;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }
}
