package com.microtechmd.blecomm.constant;

public class AidexXOperation {

    public static final int BUSY = 0;
    public static final int DISCOVER = 1;
    public static final int CONNECT = 2;
    public static final int DISCONNECT = 3;
    public static final int PAIR = 4;
    public static final int UNPAIR = 5;
    public static final int BOND = 6;

    public static int GET_DEVICE_INFO = 0x0100;
    public static int GET_BROADCAST_DATA = 0x0101;

    public static int SET_NEW_SENSOR = 0x0200;
    public static int GET_START_TIME = 0x0201;
    public static int GET_HISTORY_RANGE = 0x0202;
    public static int GET_HISTORIES = 0x0203;
    public static int GET_HISTORIES_RAW = 0x0204;
    public static int SET_CALIBRATION = 0x0205;
    public static int GET_CALIBRATION_RANGE = 0x0206;
    public static int GET_CALIBRATION = 0x0207;

    public static int SET_DEFAULT_PARAM = 0x0300;
    public static int GET_DEFAULT_PARAM = 0x0301;
    public static int GET_SENSOR_CHECK = 0x0302;

    public static int RESET = 0x0F00;
    public static int SHELF_MODE = 0x0F01;
    public static int DELETE_BOND = 0x0F02;
    public static int CLEAR_STORAGE = 0x0F03;
    public static int SET_GC_BIAS_TRIMMING = 0x0F04;
    public static int SET_GC_IMEAS_TRIMMING = 0x0F05;
}

