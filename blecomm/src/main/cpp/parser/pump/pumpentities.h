#ifndef PUMPENTITIES_H
#define PUMPENTITIES_H

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
} PumpDeviceEntity;

typedef struct {
    uint32 expirationTime;
    uint32 autoOffTime;
    uint16 reservoirLowLimit;
    uint16 quickBolusStep;
    uint16 occlusionLimit;
    uint16 unitAmount;
    uint16 basalRateLimit;
    uint16 bolusAmountLimit;
} PumpSettingEntity;

typedef struct {
    string dateTime;
    uint8 remainingCapacity;
    uint16 remainingInsulin;
    bool autoMode;
    uint16 basal;
    uint32 bolus;
    float32 basalUnitPerHour;
    float32 bolusUnitPerHour;
    uint16 eventIndex;
    uint8 eventPort;
    uint8 eventType;
    uint8 eventLevel;
    uint32 event;
    uint8 eventValue;
} PumpHistoryEntity;

typedef struct {
    bool historyExpired;
    PumpHistoryEntity history;
} PumpBroadcastEntity;

#endif // PUMPENTITIES_H

