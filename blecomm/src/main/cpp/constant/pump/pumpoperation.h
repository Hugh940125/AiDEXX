#ifndef PUMPOPERATION_H
#define PUMPOPERATION_H

#include "../../devcomm/CLibrary/global.h"
#include "../bleoperation.h"

class PumpOperation : public BleOperation
{
public:
    static const uint16 GET_DEVICE_INFO = 0x1000;
    static const uint16 GET_MODE = 0x1400;
    static const uint16 GET_DELIVERY_BUSY = 0x1401;
    static const uint16 GET_BASAL_PROFILE = 0x1402;
    static const uint16 GET_BOLUS_PROFILE = 0x1403;
    static const uint16 GET_SETTING = 0x1405;
    static const uint16 GET_HISTORY = 0x1501;
    static const uint16 GET_OCCLUSION = 0x3502;

    static const uint16 SET_CGM_SN = 0x210A;
    static const uint16 SET_AUTO_MODE = 2300;
    static const uint16 SET_GLUCOSE_TARGET = 2303;
    static const uint16 SET_ISF = 2304;
    static const uint16 SET_MODE = 0x2400;
    static const uint16 SET_BASAL_PROFILE = 0x2402;
    static const uint16 SET_BOLUS_PROFILE = 0x2403;
    static const uint16 SET_TEMPORARY_PROFILE = 0x2404;
    static const uint16 SET_SETTING = 0x2405;
    static const uint16 SET_REWINDING = 0x2406;
    static const uint16 SET_PRIMING = 0x2407;
    static const uint16 SET_BOLUS_RATIO = 0x240A;
    static const uint16 SET_TEMPORARY_PERCENT_PROFILE = 0x240C;
    static const uint16 SET_DATETIME = 0x2500;
    static const uint16 SET_EVENT_CONFIRMED = 0x2502;
};

#endif // PUMPOPERATION_H
