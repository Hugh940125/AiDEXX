#ifndef BGMOPERATION_H
#define BGMOPERATION_H

#include "../../devcomm/CLibrary/global.h"
#include "../bleoperation.h"

class BgmOperation : public BleOperation
{
public:
    static const uint16 GET_DEVICE_INFO = 0x1000;
    static const uint16 GET_HISTORY = 0x1501;
    static const uint16 GET_ERROR_CODE = 0x1502;
    static const uint16 GET_COUNT_DOWN = 0x1503;
    static const uint16 GET_BLOOD_SAMPLE = 0x1504;
    static const uint16 GET_CONTROL_SOLUTION = 0x1505;
    static const uint16 GET_BLOOD_LO = 0x1506;
    static const uint16 GET_BLOOD_HI = 0x1507;
};

#endif // BGMOPERATION_H
