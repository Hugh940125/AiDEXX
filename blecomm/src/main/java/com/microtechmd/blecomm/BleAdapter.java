package com.microtechmd.blecomm;

public abstract class BleAdapter {
    static {
        System.loadLibrary("blecomm-lib");
    }

    protected static BleAdapter instance;

    public static BleAdapter getInstance() {
        return instance;
    }

    // DO NOT REMOVE!
    protected long ptr;

    protected BleAdapter() {
        constructor();
    }

    @Override
    protected void finalize() throws Throwable {
        destructor();
        super.finalize();
    }

    public abstract void executeStartScan();

    public abstract void executeStopScan();

    public abstract boolean isReadyToConnect(String mac);

    public abstract void executeConnect(String mac);

    public abstract void executeDisconnect();

    public abstract void executeWrite(byte[] data);

    public abstract void executeWriteCharacteristic(int uuid, byte[] data);

    public abstract void executeReadCharacteristic(int uuid);

    public abstract BluetoothDeviceStore getDeviceStore();

    private native void constructor();

    private native void destructor();

    public native int getCharacteristicUUID();

    public native void setDiscoverTimeoutSeconds(int seconds);

    public native void onScanRespond(String address, int rssi, byte[] data);

    public native void onAdvertise(String address, int rssi, byte[] data);

    public native void onConnectSuccess();

    public native void onConnectFailure();

    public native void onDisconnected();

    public native void onReceiveData(byte[] data);

    public native void onReceiveData(int uuid, byte[] data);

    public abstract void startBtScan(Boolean isPeriodic);

    public abstract void stopBtScan(Boolean isPeriodic);
}
