#ifndef AIDEXXOPERATION_H
#define AIDEXXOPERATION_H

#include "../../devcomm/CLibrary/global.h"
#include "../bleoperation.h"

class AidexXOperation : public BleOperation
{
public:
    static const uint16 GET_DEVICE_INFO = 0x0100;
    static const uint16 GET_BROADCAST_DATA = 0x0101;

    static const uint16 SET_NEW_SENSOR = 0x0200;
    static const uint16 GET_START_TIME = 0x0201;
    static const uint16 GET_HISTORY_RANGE = 0x0202;
    static const uint16 GET_HISTORIES = 0x0203;
    static const uint16 GET_HISTORIES_RAW = 0x0204;
    static const uint16 SET_CALIBRATION = 0x0205;
    static const uint16 GET_CALIBRATION_RANGE = 0x0206;
    static const uint16 GET_CALIBRATION = 0x0207;

    static const uint SET_DEFAULT_PARAM = 0x0300;
    static const uint GET_DEFAULT_PARAM = 0x0301;
    static const uint GET_SENSOR_CHECK = 0x0302;

    static const uint GET_AUTO_UPDATE_STATUS = 0x0303;
    static const uint SET_AUTO_UPDATE_STATUS = 0x0304;
    static const uint SET_DYNAMIC_ADV_MODE = 0x0305;

    static const uint16 RESET = 0x0F00;
    static const uint16 SHELF_MODE = 0x0F01;
    static const uint16 DELETE_BOND = 0x0F02;
    static const uint16 CLEAR_STORAGE = 0x0F03;
    static const uint16 SET_GC_BIAS_TRIMMING = 0x0F04;
    static const uint16 SET_GC_IMEAS_TRIMMING = 0x0F05;
    
    static const uint16 AUTO_UPDATE_FULL_HISTORY = 0xFE01;
    static const uint16 AUTO_UPDATE_CALIBRATION = 0xFE02;

    static const uint16 SET_SN = 0xFF01;
};

class AidexXResponseCode
{
public:
    static const uint8 REFUSED = 0;
    static const uint8 OK = 1;
    static const uint8 TIME_OUT = 2;
    static const uint8 INVALID_OPERAND = 3;
    static const uint8 LESS_THAN_RANGE = 4;
    static const uint8 GREATER_THAN_RANGE = 5;
};

#endif // AIDEXXOPERATION_H
