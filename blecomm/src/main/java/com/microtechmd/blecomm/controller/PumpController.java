package com.microtechmd.blecomm.controller;

public class PumpController extends BleController {

    public PumpController() {
        constructor();
    }

    @Override
    protected void finalize() throws Throwable {
        destructor();
        super.finalize();
    }

    private native void constructor();

    private native void destructor();


    public native int getDeviceInfo();

    public native int getHistory(int index);

    public native int setDatetime(String dateTime);

    public native int setCgmSn(String cmgSn);

    public native int setAutoMode(boolean isOn);

    public native int getMode();

    public native int setMode(int mode);

    public native int setBasalProfile(float[] basal);

    public native int setBolusProfile(float amountTotal, float bolusRatio, float amountExtended, int intervalExtended);

    public native int getBolusProfile();

    public native int setTemporaryProfile(float tempBasal, int interval);

    public native int setSetting(float[] value);

    public native int setRewinding(float amount); //默认32000

    public native int getOcclusion(); //

    public native int setAddress(); //

    public native int getBasalProfile(); //

    public native int getSetting(); //

    public native int getPumpCapacityStatus(); // 获取泵电池状态

    public native int setEventConfirmed(int eventIndex, int event, int value); //

    public native int clearAddress(); //

    public native int setPriming(float amount); //默认120

    public native int setGlucoseTarget(float targetLower, float targetUpper); //设置目标血糖

    public native int setIsf(float isf); //设置胰岛素敏感因子

    public native int setGlucose(float glucose); //设置血糖值

    public native int setBolusRatio(int multiple, int division);

    public native int setTemporaryPercentProfile(int tempBasalPercent, int interval); //临时基础率百分比
}
