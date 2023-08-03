package com.microtechmd.blecomm.controller;/**
 * @Description:
 * @Author: Hugh
 * @CreateDate: 2023/8/3 10:51
 */

/**
 *@date 2023/8/3
 *@author Hugh
 *@desc
 */
public class ScanResponseInfo {
    public boolean isBleNativePaired;            // true: Ble pairing information saved
    public boolean isAesInitialized;             // true: AES_Key is initialized

    public ScanResponseInfo(boolean isBleNativePaired, boolean isAesInitialized) {
        this.isBleNativePaired = isBleNativePaired;
        this.isAesInitialized = isAesInitialized;
    }

    @Override
    public String toString() {
        return "ScanResponseInfo{" +
                "isBleNativePaired=" + isBleNativePaired +
                ", isAesInitialized=" + isAesInitialized +
                '}';
    }
}
