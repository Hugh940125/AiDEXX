#ifndef BLEOPERATION_H
#define BLEOPERATION_H

#include "../devcomm/CLibrary/global.h"

class BleOperation
{
public:
    static const uint16 BUSY = 0;
    static const uint16 DISCOVER = 1;
    static const uint16 CONNECT = 2;
    static const uint16 DISCONNECT = 3;
    static const uint16 PAIR = 4;
    static const uint16 UNPAIR = 5;
    static const uint16 BOND = 6;

    static const uint16 UNKNOWN = 0xFFFF;
};

#endif // BLEOPERATION_H
