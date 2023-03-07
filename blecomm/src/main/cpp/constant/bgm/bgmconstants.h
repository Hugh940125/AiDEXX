#ifndef BGMCONSTANTS_H
#define BGMCONSTANTS_H

#include "../../devcomm/CLibrary/global.h"
#include "../globalconstants.h"

class BgmPort
{
public:
    static const uint8 PORT_SYSTEM = 0;
    static const uint8 PORT_COMM = 1;
    static const uint8 PORT_MONITOR = 5;
};

class BgmSystem
{
public:
    static const uint8 PARAM_DEVICE = 0;
};

class BgmMonitor
{
public:
    static const uint8 PARAM_DATE_TIME = 0;
    static const uint8 PARAM_HISTORY = 1;
    static const uint8 PARAM_ERROR_CODE = 2;
    static const uint8 PARAM_COUNT_DOWN = 3;
    static const uint8 PARAM_BLOOD_SAMPLE = 4;
    static const uint8 PARAM_CONTROL_SOLUTION = 5;
    static const uint8 PARAM_BLOOD_LO = 6;
    static const uint8 PARAM_BLOOD_HI = 7;
};

class BgmHistory
{
public:


    static const int64 YEAR_2000 = 2000;
};

#endif // BGMCONSTANTS_H
