#ifndef AIDEXXENTITIES_H
#define AIDEXXENTITIES_H

#include "../../devcomm/CLibrary/global.h"

#include <string>

using namespace std;

typedef struct {
    string sn;
    string edition;
} AidexXDeviceEntity;

typedef struct {
    uint16 year;       // 1582~9999
    uint8 month;       // 1~12
    uint8 day;         // 1~31
    uint8 hour;        // 0~23
    uint8 minute;      // 0~59
    uint8 second;      // 0~59
    int8 timeZone;     // 15 minutes; -128: Unknow
    uint8 dstOffset;   // 15 minutes; 255: Unknow
} AidexXDatetimeEntity;

typedef struct {
    uint16 timeOffset; // minutes, based on Session Start Time
    uint16 glucose;    // mg/dL
    uint8 status;      // AidexxHistory::STATUS_ in aidexxconstants.h
    uint8 quality;     // 0~100
    bool isValid;      // false: data loss
} AidexXHistoryEntity;

typedef struct {
    uint16 timeOffset; // minutes, based on Session Start Time
    float32 i1;        // nA
    float32 i2;        // nA
    float32 vc;        // V
    bool isValid;      // false: data loss
} AidexXRawHistoryEntity;

typedef struct {
    uint16 index;
    uint16 timeOffset;        // minutes, based on Session Start Time
    float32 referenceGlucose; // mg/dL
    bool isValid;             // false: data loss
    int16 cf;        // minutes, based on Session Start Time
    int16 offset;        // minutes, based on Session Start Time
} AidexXCalibrationEntity;

/* NewSensor:         (status & AidexxStatus::SESSION_STOPPED) && (calTemp & AidexxCalTemp::TIME_SYNCHRONIZATION_REQUIRED)
   SensorExpired:     (status & AidexxStatus::SESSION_STOPPED) && !(calTemp & AidexxCalTemp::TIME_SYNCHRONIZATION_REQUIRED)
*/
typedef struct {
    AidexXHistoryEntity history[4];
    uint16 timeOffset;              // minutes, based on Session Start Time
    uint8 historyCount;             // count of history entities in this broadcast
    uint8 status;                   // AidexxStatus in aidexxconstants.h
    uint8 calTemp;                  // AidexxCalTemp in aidexxconstants.h
    int8 trend;                     // mg/dL/min; -128: Unknow
} AidexXBroadcastEntity;

typedef struct {
    uint16 calTimeOffset;           // minutes, based on Session Start Time
    bool isPaired;                  // true: Ble pairing information saved
    bool isInitialized;             // true: AES_Key is initialized
} AidexXScanResponseEntity;

typedef struct {
    AidexXHistoryEntity history[4];
    uint16 historyTimeOffset;       // minutes, based on Session Start Time
    uint16 calTimeOffset;           // minutes, based on Session Start Time
    bool isPaired;                  // true: Ble pairing information saved
    bool isInitialized;             // true: AES_Key is initialized
    uint8 historyCount;             // count of history entities in this broadcast
    uint8 status;                   // AidexxStatus in aidexxconstants.h
    uint8 calTemp;                  // AidexxCalTemp in aidexxconstants.h
    int8 trend;                     // mg/dL/min; -128: Unknow
} AidexXFullBroadcastEntity;

#endif // AIDEXXENTITIES_H

