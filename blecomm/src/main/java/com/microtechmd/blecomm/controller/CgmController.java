package com.microtechmd.blecomm.controller;

import com.microtechmd.blecomm.entity.NewSensorEntity;

public class CgmController extends BleControllerProxy {
    static {
        System.loadLibrary("blecomm-lib");
    }

    public CgmController() {
        constructor();
    }

    @Override
    protected void finalize() throws Throwable {
        destructor();
        super.finalize();
    }

    private native void constructor();

    private native void destructor();

    public native int getHistories(int index);

    public native int getFullHistories(int index);

    public native int newSensor(boolean isNew, long datetime);

    public native int setDatetime(long datetime);

    public native int recordBg(float glucose, long datetime);

    public native int calibration(float glucose, long datetime);

    public native int setHyper(float hyper);

    public native int setHypo(float hypo);

    public native void initialSettings(float hypo,float hyper);

    public native float getHypo( );

    public native float getHyper();

    public native int getDeviceInfo();

    public native int getDefaultParamData();

    public native int setDefaultParamData(float[] data);

    @Override
    public void getTransInfo() {

    }

    @Override
    public void getDefaultParam() {

    }

    @Override
    public void newSensor(NewSensorEntity newSensorEntity) {

    }

    @Override
    public void startTime() {

    }

    @Override
    public void clearPair() {

    }

    @Override
    public void setDynamicMode(int mode) {
    }

    @Override
    public void setAutoUpdate() {
    }

    @Override
    public boolean isNativePaired() {
        return false;
    }

    @Override
    public boolean isInitialized() {
        return false;
    }
}
