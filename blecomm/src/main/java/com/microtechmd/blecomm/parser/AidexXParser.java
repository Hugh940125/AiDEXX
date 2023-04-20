package com.microtechmd.blecomm.parser;

import java.util.List;

public class AidexXParser {

    public static native <T extends AidexXFullBroadcastEntity> T getFullBroadcast(byte[] bytes);

    public static native <V extends AidexXHistoryEntity> List<V> getHistories(byte[] bytes);

    public static native <V extends AidexXRawHistoryEntity> List<V> getRawHistory(byte[] bytes);

    public static native <V extends AidexXCalibrationEntity> List<V> getAidexXCalibration(byte[] bytes);

    public static native float[] getParam(byte[] bytes);

}
