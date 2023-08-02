#ifndef AIDEXXENTITIES_H
#define AIDEXXENTITIES_H

#include "../../../devcomm/CLibrary/global.h"
#include <string>

using namespace std;

//typedef enum
//{
//  GX_01S = 0,     // 1.5v ,15days
//  GX_02S,         // 1.5v ,10days
//  GX_03S,         // 1.5v ,8days
//  GX_01,          // 3v ,15days
//  GX_02,          // 3v ,10days
//  GX_03,          // 3v ,8days
//  DEVICE_TYPE_COUNT
//} device_type;


typedef struct {
    uint8 hardWare;
    uint8 type;
    uint8 editionMajor;
    uint8 editionMinor;
    uint8 editionRevision;
    uint8 editionBuild;

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
    uint16 index;
    uint16 timeOffset;        // minutes, based on Session Start Time
    uint16 referenceGlucose; // mg/dL
    float32 cf;
    float32 offset;
    bool isValid;             // false: data loss
} AidexXCalibrationEntity;

typedef struct {
    uint16 timeOffset; // minutes, based on Session Start Time
    uint16 glucose;    // mg/dL
    uint8 status;      // AidexxHistory::STATUS_ in aidexxconstants.h
    uint8 quality;     // 0~100，broadcast only
    bool isValid;      // false: data loss
} AidexXHistoryEntity;

typedef struct {
    uint16 timeOffset; // minutes, based on Session Start Time
    float32 i1;        // nA
    float32 i2;        // nA
    float32 vc;        // V
    bool isValid;      // false: data loss
} AidexXRawHistoryEntity;

/* NewSensorDetection:  (status & AidexxStatus::SESSION_STOPPED) && (calTemp & AidexxCalTemp::TIME_SYNCHRONIZATION_REQUIRED)
   SensorExpired:       (status & AidexxStatus::SESSION_STOPPED) && !(calTemp & AidexxCalTemp::TIME_SYNCHRONIZATION_REQUIRED)
*/

typedef struct {
    uint16 timeOffset;      // minutes, based on Session Start Time
    uint8 status;           // AidexxStatus in aidexxconstants.h
    uint8 calTemp;          // AidexxCalTemp in aidexxconstants.h
    int8 trend;             // mg/dL/min; -128: Unknow
    uint16 calIndex;        // calibration record index
} AidexXAbstractEntity;

typedef struct {
    AidexXAbstractEntity abstract;
    AidexXHistoryEntity history[3];     //histories, max to 3
    uint8 historyCount;                 // count of history entities in this broadcast
} AidexXBroadcastEntity;

typedef struct {
    bool isBleNativePaired;            // true: Ble pairing information saved
    bool isAesInitialized;             // true: AES_Key is initialized
} AidexXScanResponseEntity;

typedef struct {
    AidexXAbstractEntity abstract;
    AidexXHistoryEntity history;
    AidexXRawHistoryEntity raw;
} AidexXInstantHistoryEntity;

typedef enum
{
    INSTANT_MESSAGE_TYPE_HISTORY = 1,
    INSTANT_MESSAGE_TYPE_CALIBRATION,
    INSTANT_MESSAGE_TYPE_COUNT
} instant_message_type;

#endif // AIDEXXENTITIES_H

