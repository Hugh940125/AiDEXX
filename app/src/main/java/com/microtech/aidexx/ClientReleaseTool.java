package com.microtech.aidexx;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ClientReleaseTool {

    public static boolean releaseAllScanClient() {
        try {
            Object mIBluetoothManager = getIBluetoothManager(BluetoothAdapter.getDefaultAdapter());
            if (mIBluetoothManager == null) return false;
            Object iGatt = getIBluetoothGatt(mIBluetoothManager);
            if (iGatt == null) return false;
            Method unregisterClient = getDeclaredMethod(iGatt, "unregisterClient", int.class);
            Method stopScan;
            int type;
            try {
                type = 0;
                stopScan = getDeclaredMethod(iGatt, "stopScan", int.class, boolean.class);
            } catch (Exception e) {
                type = 1;
                stopScan = getDeclaredMethod(iGatt, "stopScan", int.class);
            }

            for (int mClientIf = 0; mClientIf <= 40; mClientIf++) {
                if (type == 0) {
                    try {
                        stopScan.invoke(iGatt, mClientIf, false);
                    } catch (Exception ignored) {
                    }
                }
                if (type == 1) {
                    try {
                        stopScan.invoke(iGatt, mClientIf);
                    } catch (Exception ignored) {
                    }
                }
                try {
                    unregisterClient.invoke(iGatt, mClientIf);
                } catch (Exception ignored) {
                }
            }
            stopScan.setAccessible(false);
            unregisterClient.setAccessible(false);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Method getDeclaredMethod(Object obj, String name, Class<?>... parameterTypes) throws
            NoSuchMethodException {
        Method declaredMethod = obj.getClass().getDeclaredMethod(name, parameterTypes);
        declaredMethod.setAccessible(true);
        return declaredMethod;
    }

    @SuppressLint("PrivateApi")
    public static Object getIBluetoothManager(BluetoothAdapter adapter) throws
            NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method getBluetoothManager = getDeclaredMethod(BluetoothAdapter.class, "getBluetoothManager");
        return getBluetoothManager.invoke(adapter);
    }

    @SuppressLint("PrivateApi")
    public static Object getIBluetoothGatt(Object mIBluetoothManager) throws
            NoSuchMethodException, IllegalAccessException {
        Method getBluetoothGatt = getDeclaredMethod(mIBluetoothManager, "getBluetoothGatt");
        Object object = new Object();
        try {
            object = getBluetoothGatt.invoke(mIBluetoothManager);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return object;
    }
}
