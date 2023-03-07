#ifndef BLECONTROLLERINFO_H
#define BLECONTROLLERINFO_H

#include "../devcomm/CLibrary/global.h"
#include <string>
#include <vector>

using namespace std;

typedef struct {
    string address;
    string name;
    string sn;
    int32 rssi;
    vector<uint8> params;
} BleControllerInfo;

#endif // BLECONTROLLERINFO_H

