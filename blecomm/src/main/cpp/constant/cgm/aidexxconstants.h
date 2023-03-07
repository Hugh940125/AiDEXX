#ifndef AIDEXXCONSTANTS_H
#define AIDEXXCONSTANTS_H

#include "../../devcomm/CLibrary/global.h"

class AidexxHistory
{
public:

    static const int TIME_INTERVAL = 1;

    static const int STATUS_OK = 0;
    static const int STATUS_INVALID = 1;
    static const int STATUS_ERROR = 2;
};

class AidexxStatus
{
public:

    static const int SESSION_STOPPED = 0x01;
    static const int DEVICE_BATTERY_LOW = 0x02;
    static const int SENSOR_TYPE_INCORRECT_FOR_DEVICE = 0x04;
    static const int SENSOR_MALFUNCTION = 0x08;
    static const int DEVICE_SPECIFIC_ALERT = 0x10;
    static const int GENERAL_DEVICE_FAULT = 0x20;
};

class AidexxCalTemp
{
public:

    static const int TIME_SYNCHRONIZATION_REQUIRED = 0x01;
    static const int CALIBRATION_NOT_ALLOWED = 0x02;
    static const int CALIBRATION_RECOMMENDED = 0x04;
    static const int CALIBRATION_REQUIRED = 0x08;
    static const int SENSOR_TEMPERATURE_HIGH_DETECTION = 0x10;
    static const int SENSOR_TEMPERATURE_LOW_DETECTION = 0x20;
};

#endif // AIDEXXCONSTANTS_H
