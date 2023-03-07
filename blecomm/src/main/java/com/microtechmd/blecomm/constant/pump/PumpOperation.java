package com.microtechmd.blecomm.constant.pump;

import com.microtechmd.blecomm.constant.BleOperation;

public class PumpOperation extends BleOperation {
    public static final int GET_DEVICE_INFO = 0x1000;
    public static final int GET_HISTORY = 0x1501;
    public static final int GET_MODE = 0x1400;
    public static final int GET_SETTING = 0x1405;
    public static final int GET_BOLUS = 0x1403;
    public static final int GET_CAPACITY = 0x1509;

    public static final int SET_MODE = 0x2400;
    public static final int SET_BASAL_PROFILE = 0x2402;
    public static final int SET_BOLUS_PROFILE = 0x2403;
    public static final int SET_TEMPORARY_PROFILE = 0x2404;
    public static final int SET_TEMPORARY_PROFILE_ = 0x240C;
    public static final int SET_SETTING = 0x2405;
    public static final int SET_REWINDING = 0x2406;
    public static final int SET_PRIMING = 0x2407;
    public static final int SET_BOLUS_RATIO = 0x240A;
    public static final int SET_DATETIME = 0x2500;
    public static final int GET_OCCLUSION= 0x3502;
    public static final int SET_CGMS_SN = 8458;
    public static final int SET_SMART = 2300;
    public static final int SET_SMART_GLUCOSE = 2303;
    public static final int SET_SLF= 2304;
    public static final int SET_Event_Confirmed= 9474;


}
