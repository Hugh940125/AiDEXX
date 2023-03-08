package com.microtechmd.blecomm;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 设备信息集合
 */
public class BluetoothDeviceStore {
    private final Map<String, ScanResult> mDeviceMap;

    public BluetoothDeviceStore() {
        mDeviceMap = new HashMap<>();
    }

    public void add(ScanResult result) {
        if (result == null) {
            return;
        }
        if (!mDeviceMap.containsKey(result.getDevice().getAddress())) {
            mDeviceMap.put(result.getDevice().getAddress(), result);
        }
    }

    public void remove(BluetoothDevice device) {
        if (device == null) {
            return;
        }
        mDeviceMap.remove(device.getAddress());
    }

    public void clear() {
        mDeviceMap.clear();
    }

    public Map<String, ScanResult> getDeviceMap() {
        return mDeviceMap;
    }

//    public List<BluetoothDevice> getDeviceList() {
//        final List<BluetoothDevice> methodResult = new ArrayList<>(mDeviceMap.values());
//        methodResult.sort((arg0, arg1) -> arg0.getAddress().compareToIgnoreCase(arg1.getAddress()));
//        return methodResult;
//    }

    @NonNull
    @Override
    public String toString() {
        return "BluetoothLeDeviceStore{" + "mDeviceMap=" + mDeviceMap + '}';
    }
}
