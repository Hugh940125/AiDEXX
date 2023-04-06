package com.microtechmd.blecomm;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;
import com.microtechmd.blecomm.controller.BleControllerInfo;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 设备信息集合
 */
public class BluetoothDeviceStore {
    private final ConcurrentHashMap<String, BluetoothDevice> mDeviceMap;

    public BluetoothDeviceStore() {
        mDeviceMap = new ConcurrentHashMap<>();
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

    public ConcurrentHashMap<String, BluetoothDevice> getDeviceMap() {
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
