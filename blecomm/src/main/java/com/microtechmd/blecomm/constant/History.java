package com.microtechmd.blecomm.constant;


public class History {
    public static final int HISTORY_TRANSMITTER_STARTUP = 0;
    public static final int HISTORY_TRANSMITTER_ERROR = 1;
    public static final int HISTORY_BATTERY_LOW = 2;
    public static final int HISTORY_BATTERY_EXHAUSTED = 3;
    public static final int HISTORY_SENSOR_NEW = 4;
    public static final int HISTORY_SENSOR_ERROR = 5;
    public static final int HISTORY_SENSOR_EXPIRATION = 6;
    public static final int HISTORY_GLUCOSE = 7;
    public static final int HISTORY_GLUCOSE_RECOMMEND_CAL = 8;
    public static final int HISTORY_GLUCOSE_INVALID = 9;
    public static final int HISTORY_HYPO = 10;
    public static final int HISTORY_HYPER = 11;
    public static final int HISTORY_IMPENDANCE = 12;
    public static final int HISTORY_BLOOD_GLUCOSE = 13;
    public static final int HISTORY_CALIBRATION = 14;
    public static final int HISTORY_HYPO_SETTING = 15;
    public static final int HISTORY_HYPER_SETTING = 16;
    public static final int HISTORY_PLACEHOLDER = 0x1E & 255;
    public static final int HISTORY_INVALID = 0x1F & 255;

    public static final int HISTORY_LOCAL_HYPO = 2;
    public static final int HISTORY_LOCAL_HYPER = 1;
    public static final int HISTORY_LOCAL_NORMAL = 0;

    public static final long YEAR_2000 = 946656000;
    public static final int HISTORY_LOCAL_URGENT_HYPO = 3;

    public static final int SESSION_STOPPED = 0x01;
    public static final int DEVICE_BATTERY_LOW = 0x02;
    public static final int SENSOR_TYPE_INCORRECT_FOR_DEVICE = 0x04;
    public static final int SENSOR_MALFUNCTION = 0x08;
    public static final int DEVICE_SPECIFIC_ALERT = 0x10;
    public static final int GENERAL_DEVICE_FAULT = 0x20;

    public static final int TIME_SYNCHRONIZATION_REQUIRED = 0x01;
    public static final int CALIBRATION_NOT_ALLOWED = 0x02;
    public static final int CALIBRATION_RECOMMENDED = 0x04;
    public static final int CALIBRATION_REQUIRED = 0x08;
    public static final int SENSOR_TEMPERATURE_HIGH_DETECTION = 0x10;
    public static final int SENSOR_TEMPERATURE_LOW_DETECTION = 0x20;

    public static final int STATUS_OK = 0;
    public static final int STATUS_INVALID = 1;
    public static final int STATUS_ERROR = 2;
}
