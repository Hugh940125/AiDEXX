#ifndef GLOBALCONSTANTS_H
#define GLOBALCONSTANTS_H

#include "../devcomm/CLibrary/global.h"

class Global
{
public:
    static const uint8 FUNCTION_FAIL = 0;
    static const uint8 FUNCTION_OK = 1;

    static const uint8 ADDRESS_REMOTE_MASTER = 0;
    static const uint8 ADDRESS_REMOTE_SLAVE = 1;
    static const uint8 ADDRESS_LOCAL_VIEW = 2;
    static const uint8 ADDRESS_LOCAL_CONTROL = 3;
    static const uint8 ADDRESS_LOCAL_MODEL = 4;
    static const uint8 COUNT_ADDRESS = 5;

    static const uint8 MODE_ACKNOWLEDGE = 0;
    static const uint8 MODE_NO_ACKNOWLEDGE = 1;
    static const uint8 COUNT_MODE = 2;

    static const uint8 OPERATION_EVENT = 0;
    static const uint8 OPERATION_SET = 1;
    static const uint8 OPERATION_GET = 2;
    static const uint8 OPERATION_WRITE = 3;
    static const uint8 OPERATION_READ = 4;
    static const uint8 OPERATION_NOTIFY = 5;
    static const uint8 OPERATION_ACKNOWLEDGE = 6;
    static const uint8 OPERATION_PAIR = 7;
    static const uint8 OPERATION_UNPAIR = 8;
    static const uint8 OPERATION_BOND = 9;
    static const uint8 COUNT_OPERATION = 10;

    static const uint8 EVENT_SEND_DONE = 0;
    static const uint8 EVENT_ACKNOWLEDGE = 1;
    static const uint8 EVENT_TIMEOUT = 2;
    static const uint8 EVENT_RECEIVE_DONE = 3;
    static const uint8 COUNT_EVENT = 4;
};

class Comm {
public:
    static const uint8 PARAM_RF_STATE = 0;
    static const uint8 PARAM_RF_SIGNAL = 1;
    static const uint8 PARAM_RF_LOCAL_ADDRESS = 3;
    static const uint8 PARAM_RF_REMOTE_ADDRESS = 3;
    static const uint8 PARAM_RF_BROADCAST_SWITCH = 4;
    static const uint8 PARAM_BROADCAST_DATA = 5;
    static const uint8 PARAM_BROADCAST_OFFSET = 6;
    static const uint8 PARAM_ENABLE = 7;
    static const uint8 PARAM_DISABLE = 8;
    static const uint8 PARAM_BONDED = 9;
    static const uint8 PARAM_BUSY = 10;
    static const uint8 COUNT_PARAM = 11;

    static const uint8 PARAM_PAIR_DEVICE_0 = 0;
    static const uint8 PARAM_PAIR_DEVICE_1 = 1;
};

#endif // GLOBALCONSTANTS_H
