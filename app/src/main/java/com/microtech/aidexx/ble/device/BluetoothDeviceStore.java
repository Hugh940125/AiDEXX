package com.microtech.aidexx.ble.device;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 设备信息集合
 */
public class BluetoothDeviceStore {
    private final Map<String, BluetoothDevice> mDeviceMap;

    public BluetoothDeviceStore() {
        mDeviceMap = new HashMap<>();
    }

    public void add(BluetoothDevice device) {
        if (device == null) {
            return;
        }
        if (!mDeviceMap.containsKey(device.getAddress())) {
            mDeviceMap.put(device.getAddress(), device);
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

    public Map<String, BluetoothDevice> getDeviceMap() {
        return mDeviceMap;
    }

    public List<BluetoothDevice> getDeviceList() {
        final List<BluetoothDevice> methodResult = new ArrayList<>(mDeviceMap.values());
        methodResult.sort((arg0, arg1) -> arg0.getAddress().compareToIgnoreCase(arg1.getAddress()));
        return methodResult;
    }

    @NonNull
    @Override
    public String toString() {
        return "BluetoothLeDeviceStore{" + "mDeviceMap=" + mDeviceMap + '}';
    }
}
