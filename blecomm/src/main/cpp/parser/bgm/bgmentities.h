#ifndef BGMENTITIES_H
#define BGMENTITIES_H

#include "../../devcomm/CLibrary/global.h"

#include <string>

using namespace std;

typedef struct {
    string sn;
    uint8 endian;
    uint8 deviceType;
    uint32 model;
    string edition;
    uint32 capacity;
} BgmDeviceEntity;

typedef struct {
    string dateTime;
    uint8 temperature;
    uint8 flag;
    uint16 bgValue;
    uint16 reserved;
    
    bool hypo;
    bool hyper;
    bool ketone;
    bool preMeal;
    bool postMeal;
    bool invalid;
    bool controlSolution;

    uint16 eventIndex;
    uint8 eventPort;
    uint8 eventType;
    uint8 eventLevel;
    uint8 eventValue;
} BgmHistoryEntity;

#endif // BGMENTITIES_H

