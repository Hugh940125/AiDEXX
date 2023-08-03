package com.microtechmd.blecomm.controller;

import com.microtechmd.blecomm.entity.AidexXDatetimeEntity;
import com.microtechmd.blecomm.entity.NewSensorEntity;

public class AidexXController extends BleControllerProxy {

    static {
        new Thread(() -> System.loadLibrary("blecomm-lib"));
    }

    public static AidexXController getInstance() {
        return new AidexXController();
    }

    public static AidexXController getInstance(BleControllerInfo bleControllerInfo) {
        return new AidexXController(bleControllerInfo);
    }

    private AidexXController() {
        constructor();
    }

    private AidexXController(BleControllerInfo bleControllerInfo) {
        constructorWithInfo(bleControllerInfo);
    }

    @Override
    protected void finalize() throws Throwable {
        destructor();
        super.finalize();
    }

    public native int isBleNativePaired();

    public native int isAesInitialized();

    public native int setDynamicAdvMode(int mode);

    public native int setAutoUpdateStatus();

    private native void constructor();

    private native void constructorWithInfo(BleControllerInfo info);

    private native void destructor();

    public native int getDeviceInfo();

    public native int getBroadcastData();

    public native int newSensor(AidexXDatetimeEntity entity);

    public native int getStartTime();

    public native int getHistoryRange();

    public native int getHistories(int index);

    public native int getRawHistories(int index);

    public native int calibration(int glucose, int timeOffset);

    public native int getCalibrationRange();

    public native int getCalibration(int index);

    public native int getDefaultParamData();

    public native int setDefaultParamData(float[] value);

    public native int getSensorCheck();

    public native int reset();


    public native int shelfMode();

    public native int deleteBond();

    public native int clearStorage();

    public native int setGcBiasTrimming(int value);

    public native int setGcImeasTrimming(int zero, int scale);

    @Override
    public void getTransInfo() {
        getDeviceInfo();
    }

    @Override
    public void getDefaultParam() {
        getDefaultParamData();
    }

    @Override
    public void newSensor(NewSensorEntity newSensorEntity) {
        newSensor(newSensorEntity.getAidexXDatetimeEntity());
    }

    @Override
    public void setDynamicMode(int mode) {
        setDynamicAdvMode(mode);
    }

    @Override
    public void setAutoUpdate() {
        setAutoUpdateStatus();
    }

    @Override
    public boolean isNativePaired() {
        return isBleNativePaired() == 1;
    }

    @Override
    public boolean isInitialized() {
        return isAesInitialized() == 1;
    }

    @Override
    public void startTime() {
        getStartTime();
    }

    @Override
    public void clearPair() {
        deleteBond();
    }
}
