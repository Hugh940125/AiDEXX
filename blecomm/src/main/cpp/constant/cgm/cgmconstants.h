#ifndef CGMCONSTANTS_H
#define CGMCONSTANTS_H

#include "../../devcomm/CLibrary/global.h"
#include "../globalconstants.h"

class CgmPort
{
public:
    static const uint8 PORT_SYSTEM = 0;
    static const uint8 PORT_COMM = 1;
    static const uint8 PORT_SHELL = 2;
    static const uint8 PORT_GLUCOSE = 3;
    static const uint8 PORT_DELIVERY = 4;
    static const uint8 PORT_MONITOR = 5;
    static const uint8 COUNT_PORT = 6;
};

class CgmSystem
{
public:
    static const uint8 PARAM_DEVICE = 0;
    static const uint8 PARAM_BUSY = 1;
    static const uint8 PARAM_MEMORY = 2;
    static const uint8 COUNT_PARAM = 3;
};

class CgmComm
{
public:
    static const uint8 PARAM_RF_STATE = 0;
    static const uint8 PARAM_RF_SIGNAL = 1;
    static const uint8 PARAM_RF_LOCAL_ADDRESS = 2;
    static const uint8 PARAM_RF_REMOTE_ADDRESS = 3;
    static const uint8 PARAM_RF_BROADCAST_SWITCH = 4;
    static const uint8 PARAM_BROADCAST_DATA = 5;
    static const uint8 PARAM_BROADCAST_OFFSET = 6;
    static const uint8 PARAM_ENABLE = 7;
    static const uint8 PARAM_DISABLE = 8;
    static const uint8 PARAM_BONDED = 9;
    static const uint8 PARAM_BUSY = 10;
    static const uint8 COUNT_PARAM = 11;
};

class CgmGlucose
{
public:
    static const uint8 PARAM_BUSY = 0;
    static const uint8 PARAM_CONTROL = 1;
    static const uint8 PARAM_STATE = 2;
    static const uint8 PARAM_GLUCOSE = 3;
    static const uint8 PARAM_CALIBRATON = 4;
    static const uint8 PARAM_NEW_SENSOR = 5;
    static const uint8 PARAM_HYPO = 6;
    static const uint8 PARAM_HYPER = 7;
    static const uint8 PARAM_EXPIRATION = 8;
    static const uint8 PARAM_INIT_COUNT_DOWN = 9;
    static const uint8 PARAM_REFERENCE_IMP = 10;
    static const uint8 PARAM_REFERENCE_SHIFT = 11;
    static const uint8 PARAM_REFERENCE_ALL = 12;
    static const uint8 PARAM_DEFAULT_PARAM = 13;
    static const uint8 PARAM_CAL_FACTOR = 14;
    static const uint8 PARAM_OFFSET = 15;
    static const uint8 PARAM_GC_BIAS_TRIMMING = 16;
    static const uint8 PARAM_GC_IMEAS_TRIMMING = 17;
    static const uint8 COUNT_PARAM = 18;

    static const uint8 STATE_OK = 0;
    static const uint8 STATE_INVALID = 1;
    static const uint8 STATE_ERROR = 2;
    static const uint8 STATE_RECOMMEND_CAL = 3;
    static const uint8 COUNT_STATE = 4;
};

class CgmMonitor
{
public:
    static const uint8 PARAM_DATE_TIME = 0;
    static const uint8 PARAM_HISTORY = 1;
    static const uint8 PARAM_EVENT = 2;
    static const uint8 PARAM_BUSY = 3;
    static const uint8 PARAM_STATUS = 4;
    static const uint8 PARAM_POWER = 5;
    static const uint8 PARAM_OPTIONAL_STATUS = 6;
    static const uint8 PARAM_HISTORIES_FULL= 7;
    static const uint8 PARAM_HISTORIES = 8;
    static const uint8 PARAM_START_TIME = 9;
    static const uint8 PARAM_RUNNING_TIME = 10;
    static const uint8 PARAM_ELAPSED_TIME = 11;
    static const uint8 PARAM_UPDATE_SWITCH = 12;
    static const uint8 COUNT_PARAM = 13;
};

class CgmHistory
{
public:
    static const uint8 HISTORY_TRANSMITTER_STARTUP = 0;
    static const uint8 HISTORY_TRANSMITTER_ERROR = 1;
    static const uint8 HISTORY_BATTERY_LOW = 2;
    static const uint8 HISTORY_BATTERY_EXHAUSTED = 3;
    static const uint8 HISTORY_SENSOR_NEW = 4;
    static const uint8 HISTORY_SENSOR_ERROR = 5;
    static const uint8 HISTORY_SENSOR_EXPIRATION = 6;
    static const uint8 HISTORY_GLUCOSE = 7;
    static const uint8 HISTORY_GLUCOSE_RECOMMEND_CAL = 8;
    static const uint8 HISTORY_GLUCOSE_INVALID = 9;
    static const uint8 HISTORY_HYPO = 10;
    static const uint8 HISTORY_HYPER = 11;
    static const uint8 HISTORY_IMPENDANCE = 12;
    static const uint8 HISTORY_BLOOD_GLUCOSE = 13;
    static const uint8 HISTORY_CALIBRATION = 14;
    static const uint8 HISTORY_HYPO_SETTING = 15;
    static const uint8 HISTORY_HYPER_SETTING = 16;
    static const uint8 HISTORY_PLACEHOLDER = 0x1E & 255;
    static const uint8 HISTORY_INVALID = 0x1F & 255;

    static const int64 YEAR_2000 = 946656000;
    static const int32 BATTERY_BASE = 100;
};

class CgmDefaultParam
{
public:
    static const uint8 DP_EXPIRATION_TIME = 0;
    static const uint8 DP_CAL_FACTOR_DEFAULT = 1;
    static const uint8 DP_OFFSET_DEFAULT = 2;
    static const uint8 DP_CAL_FACTOR_1 = 3;
    static const uint8 DP_CAL_FACTOR_2 = 4;
    static const uint8 DP_CAL_FACTOR_3 = 5;
    static const uint8 DP_CAL_FACTOR_4 = 6;
    static const uint8 DP_CAL_FACTOR_5 = 7;
    static const uint8 DP_CAL_FACTOR_HOURS2 = 8;
    static const uint8 DP_CAL_FACTOR_HOURS3 = 9;
    static const uint8 DP_CAL_FACTOR_HOURS4 = 10;
    static const uint8 DP_OFFSET_1 = 11;
    static const uint8 DP_OFFSET_2 = 12;
    static const uint8 DP_OFFSET_3 = 13;
    static const uint8 DP_OFFSET_4 = 14;
    static const uint8 DP_OFFSET_5 = 15;
    static const uint8 DP_OFFSET_HOURS2 = 16;
    static const uint8 DP_OFFSET_HOURS3 = 17;
    static const uint8 DP_OFFSET_HOURS4 = 18;
    static const uint8 DP_INITIALIZAION_BIAS = 19;
    static const uint8 DP_ISIG_REF_DEFAULT = 20;
    static const uint8 DP_ISIG_NONLINEAR_C1 = 21;
    static const uint8 DP_ISIG_NONLINEAR_C0 = 22;
    static const uint8 DP_CAL_FACTOR_LOWER_SCALE = 23;
    static const uint8 DP_CAL_FACTOR_UPPER_SCALE = 24;
    static const uint8 DP_SENS_FACTOR_LOWER = 25;
    static const uint8 DP_SENS_FACTOR_UPPER = 26;
    static const uint8 DP_REF_REAL_LOWER = 27;
    static const uint8 DP_REF_REAL_UPPER = 28;
    static const uint8 DP_REF_REAL_NEW_SENSOR = 29;
    static const uint8 DP_REF_REAL_DEFAULT = 30;
    static const uint8 DP_REF_REAL_FACTOR = 31;
    static const uint8 DP_REF_REAL_CHANGE_START_HOUR = 32;
    static const uint8 DP_REF_REAL_CHANGE_PER_HOUR = 33;
    static const uint8 DP_REF_REAL_SENS_CHANGED = 34;
    static const uint8 DP_REF_IMAG_LOWER = 35;
    static const uint8 DP_REF_IMAG_UPPER = 36;
    static const uint8 DP_REF_IMAG_DEFAULT = 37;
    static const uint8 DP_REF_IMAG_FACTOR = 38;
    static const uint8 DP_REF_IMAG_SENS_CHANGED = 39;
    static const uint8 DP_REF_IMAG_OFFSET = 40;
    static const uint8 DP_COUNT = 41;
};

#endif // CGMCONSTANTS_H
