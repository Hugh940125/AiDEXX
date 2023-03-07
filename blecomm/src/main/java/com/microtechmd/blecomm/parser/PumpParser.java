package com.microtechmd.blecomm.parser;

public class PumpParser {
    public static native <T extends PumpDeviceEntity> void setDeviceInfoClass(Class<T> deviceInfoClass);

    public static native <V extends PumpHistoryEntity> void setHistoryClass(Class<V> historyClass);

    public static native <T extends PumpDeviceEntity> T getDeviceInfo(byte[] bytes);

    public static native <V extends PumpHistoryEntity> V getHistory(byte[] bytes);

    public static native <T extends PumpBroadcastEntity> void setBroadcastClass(Class<T> broadcastClass);

    public static native <T extends PumpBroadcastEntity> T getBroadcast(byte[] bytes);

    public static native float[]  getBolus(byte[] bytes);
}
