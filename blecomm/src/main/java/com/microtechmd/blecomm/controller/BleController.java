package com.microtechmd.blecomm.controller;

import com.microtechmd.blecomm.BleAdapter;
import com.microtechmd.blecomm.entity.AidexXDatetimeEntity;
import com.microtechmd.blecomm.entity.NewSensorEntity;

public abstract class BleController {
//    static {
//        System.loadLibrary("blecomm-lib");
//    }

    protected long ptr;
    private long messageCallbackPtr;

    public interface DiscoveredCallback {
        public void onDiscovered(BleControllerInfo info);
    }

    public interface MessageCallback {
        public void onReceive(int operation, boolean success, byte[] data);
    }

    static public native void setBleAdapter(BleAdapter adapter);

    static public native void startScan();

    static public native void stopScan();

    static public native void setDiscoveredCallback(DiscoveredCallback callback);

    public native String getMac();

    public native String getName();

    public native String getSn();

    public native byte[] getHostAddress();

    public native byte[] getId();

    public native byte[] getKey();

    public native int getRssi();

    public native void setMac(String mac);

    public native void setName(String name);

    public native void setSn(String sn);

    public native void setHostAddress(byte[] address);

    public native void setId(byte[] id);

    public native void setKey(byte[] key);

    public native void setRssi(int rssi);

    public native void setMessageCallback(MessageCallback callback);

    public native void register();

    public native void unregister();

    public native void disconnect();

    public native int pair();

    public native int unpair();
}
