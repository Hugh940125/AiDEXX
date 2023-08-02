#ifndef BLECONTROLLERINFO_H
#define BLECONTROLLERINFO_H

#include "../devcomm/CLibrary/global.h"
#include <string>
#include <vector>

using namespace std;

typedef enum {
    DEV_TYPE_UNKNOWN = 0,
    DEV_TYPE_BGM,
    DEV_TYPE_PUMP,
    DEV_TYPE_CGM,
    DEV_TYPE_CGM_X,
    DEV_TYPE_COUNT
} dev_type;

typedef struct {
    dev_type type;
    string address;
    string name;
    string sn;
    int32 rssi;
    vector<uint8> params;
} BleControllerInfo;

#endif // BLECONTROLLERINFO_H

