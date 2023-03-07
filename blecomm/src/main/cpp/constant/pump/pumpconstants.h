#ifndef PUMPCONSTANTS_H
#define PUMPCONSTANTS_H

#include "../../devcomm/CLibrary/global.h"
#include "../globalconstants.h"

class PumpPort
{
public:
    static const uint8 PORT_SYSTEM = 0;
    static const uint8 PORT_COMM = 1;
    static const uint8 PORT_SHELL = 2;
    static const uint8 PORT_GLUCOSE = 3;
    static const uint8 PORT_DELIVERY = 4;
    static const uint8 PORT_MONITOR = 5;
    static const uint8 PORT_OCCLUSION = 0x15;
};

class PumpSystem
{
public:
    static const uint8 PARAM_DEVICE = 0;
};

class PumpComm
{
public:
    static const uint8 PARAM_ADDRESS = 3;
    static const uint8 PARAM_CGM_SN = 10;
};

class PumpGlucose
{
public:
    static const uint8 PARAM_AUTO_MODE = 0;
    static const uint8 PARAM_GLUCOSE_TARGET = 3;
    static const uint8 PARAM_ISF = 4;
};

class PumpDeliver
{
public:
    static const uint8 PARAM_MODE = 0;
    static const uint8 PARAM_BUSY = 1;
    static const uint8 PARAM_PROFILE_BASAL = 2;
    static const uint8 PARAM_PROFILE_BOLUS = 3;
    static const uint8 PARAM_PROFILE_TEMPORARY = 4;
    static const uint8 PARAM_SETTING = 5;
    static const uint8 PARAM_REWINDING = 6;
    static const uint8 PARAM_PRIMING = 7;
    static const uint8 PARAM_UNIT = 8;
    static const uint8 PARAM_OCCLUSION = 9;
    static const uint8 PARAM_BOLUS_RATIO = 10;
    static const uint8 PARAM_ACKNOWLEDGEMENT = 11;
    static const uint8 PARAM_PROFILE_TEMPORARY_PERCENT = 12;
};

class PumpMode
{
public:
    static const uint8 MODE_SUSPEND = 0;
    static const uint8 MODE_DELIVER = 1;
    static const uint8 MODE_STOP = 2;
};

class PumpSetting
{
public:
    static const uint8 SETTING_INDEX_EXPIRATION_TIME = 0;
    static const uint8 SETTING_INDEX_AUTO_OFF_TIME = 1;
    static const uint8 SETTING_INDEX_RESERVOIR_LOW_LIMIT = 2;
    static const uint8 SETTING_INDEX_QUICK_BOLUS_STEP = 3;
    static const uint8 SETTING_INDEX_OCCLUSION_LIMIT = 4;
    static const uint8 SETTING_INDEX_UNIT_AMOUNT = 5;
    static const uint8 SETTING_INDEX_BASAL_RATE_LIMIT = 6;
    static const uint8 SETTING_INDEX_BOLUS_AMOUNT_LIMIT = 7;
    static const uint8 SETTING_COUNT = 8;
    static const uint8 SETTING_INDEX_BOLUS_STEP = 8;
    static const uint8 SETTING_INDEX_BOLUS_RATIO = 9;
};

class PumpMonitor
{
public:
    static const uint8 PARAM_DATE_TIME = 0;
    static const uint8 PARAM_HISTORY = 1;
    static const uint8 PARAM_EVENT = 2;
};

class PumpOcclusion
{
public:
    static const uint8 PARAM_OCCLUSION = 2;
};

class PumpHistory
{
public:
    static const int64 YEAR_2000 = 2000;

    static const uint32 NOTIFICATION_PUMP_DELIVER_RATE_CHANGED = 0x040000;
    static const uint32 NOTIFICATION_PUMP_PAUSED = 0x040500;
    static const uint32 NOTIFICATION_PUMP_PRIMING = 0x040700;
    static const uint32 NOTIFICATION_PUMP_REWINDING = 0x040800;
    static const uint32 NOTIFICATION_PUMP_QUICK_BOLUS = 0x040900;
    static const uint32 NOTIFICATION_PUMP_CAPACITY_LOW = 0x050000;
    static const uint32 NOTIFICATION_PUMP_POWER_ON_RESET = 0x050100;
    static const uint32 NOTIFICATION_PUMP_CHARGING = 0x050400;

    static const uint32 WARNING_PUMP_AUTO_BASAL_MAX_2H = 0x030101;
    static const uint32 WARNING_PUMP_AUTO_BASAL_SUSPENDED_2H = 0x030201;
    static const uint32 WARNING_PUMP_PERSISTENT_HYPER = 0x030301;
    static const uint32 WARNING_PUMP_NO_SG_RECEIVED = 0x030401;
    static const uint32 WARNING_PUMP_RESERVOIR_LOW = 0x040101;
    static const uint32 WARNING_PUMP_ABOUT_TO_EXPIRED = 0x040401;
    static const uint32 WARNING_PUMP_DELIVER_SUSPEND = 0x040501;
    static const uint32 WARNING_PUMP_ABOUT_TO_AUTO_OFF = 0x040601;
    static const uint32 WARNING_PUMP_CAPACITY_LOW = 0x050001;
    static const uint32 WARNING_PUMP_BUTTON_ERROR = 0x050201;
    static const uint32 WARNING_PUMP_IN_STRONG_MAGNETIC_FIELD = 0x050301;

    static const uint32 ALARM_PUMP_RESERVOIR_EMPTY = 0x040102;
    static const uint32 ALARM_PUMP_OCCLUSION = 0x040202;
    static const uint32 ALARM_PUMP_MOTOR_ERROR = 0x040302;
    static const uint32 ALARM_PUMP_EXPIRED = 0x040402;
    static const uint32 ALARM_PUMP_AUTO_OFF = 0x040602;
    static const uint32 ALARM_PUMP_CAPACITY_EMPTY = 0x050002;
    static const uint32 ALARM_PUMP_ABNORMAL_DELIVER_STOP = 0x050102;
};

class PumpBolus
{
public:
    static const uint8 BOLUS_AMOUNT_NOW = 0;
    static const uint8 BOLUS_INTERVAL_NOW = 1;
    static const uint8 BOLUS_AMOUNT_EXTENDED = 2;
    static const uint8 BOLUS_INTERVAL_EXTENDED = 3;
};

#endif // PUMPCONSTANTS_H
