#ifndef CGMOPERATION_H
#define CGMOPERATION_H

#include "../../devcomm/CLibrary/global.h"
#include "../bleoperation.h"

class CgmOperation : public BleOperation
{
public:
    static const uint16 GET_DATETIME = 7;
    static const uint16 SET_DATETIME = 8;
    static const uint16 GET_HISTORIES = 9;
    static const uint16 GET_HISTORIES_FULL = 10;
    static const uint16 SET_NEW_SENSOR = 11;
    static const uint16 GET_HYPO = 12;
    static const uint16 SET_HYPO = 13;
    static const uint16 GET_HYPER = 14;
    static const uint16 SET_HYPER = 15;
    static const uint16 RECORD_BG = 16;
    static const uint16 CALIBRATION = 17;
    
    static const uint16 GET_CAL_FACTOR = 20;
    static const uint16 SET_CAL_FACTOR = 21;
    static const uint16 GET_OFFSET = 22;
    static const uint16 SET_OFFSET = 23;

    static const uint16 GET_DEVICE_INFO = 0x1000;
    static const uint16 GET_DEVICE_CHECK = 0x1301;
    static const uint16 GET_DEFAULT_PARAM = 0x130D;
    static const uint16 SET_DEFAULT_PARAM = 0x230D;
    static const uint16 SET_GC_BIAS_TRIMMING = 0x2310;
    static const uint16 SET_GC_IMEAS_TRIMMING = 0x2311;
    
    static const uint16 GET_BROADCAST_DATA = 0x1105;
    
    static const uint16 GET_SINGLE_HISTORY = 0xF001;

    static const uint16 EXT_FORCE_UNPAIR = 0xFF01;
    static const uint16 EXT_FORCE_REBOOT = 0xFF02;
};

#endif // CGMOPERATION_H
