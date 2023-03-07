package com.microtechmd.blecomm.parser;

import java.util.List;

public class CgmParser {
    public static native <T extends CgmBroadcastEntity> void setBroadcastClass(Class<T> broadcastClass);
    public static native <V extends CgmHistoryEntity> void setHistoryClass(Class<V> historyClass);
    public static native <V extends CgmDeviceEntity> void setDeviceInfoClass(Class<V> historyClass);
    public static native <T extends CgmDeviceEntity> T getDeviceInfo(byte[] bytes);

    public static native <T extends CgmBroadcastEntity> T getBroadcast(byte[] bytes);
    public static native <V extends CgmHistoryEntity> V getHistory(byte[] bytes);
    public static native <V extends CgmHistoryEntity> List<V> getHistories(byte[] bytes);
    public static native <V extends CgmHistoryEntity> List<V> getFullHistories(byte[] bytes);

    public static native <V extends CgmConfigEntity> void setDeviceConfigClass(Class<V> historyClass);

    public static native <T extends CgmConfigEntity> T getDeviceConfig(byte[] bytes);

}
