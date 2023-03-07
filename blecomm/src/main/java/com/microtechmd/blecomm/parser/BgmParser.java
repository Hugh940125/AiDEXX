package com.microtechmd.blecomm.parser;

public class BgmParser {
    public static native <T extends BgmDeviceEntity> void setDeviceInfoClass(Class<T> deviceInfoClass);
    public static native <V extends BgmHistoryEntity> void setHistoryClass(Class<V> historyClass);

    public static native <T extends BgmDeviceEntity> T getDeviceInfo(byte[] bytes);
    public static native <V extends BgmHistoryEntity> V getHistory(byte[] bytes);
}
