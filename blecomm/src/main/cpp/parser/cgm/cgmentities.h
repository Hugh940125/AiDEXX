#ifndef CGMENTITIES_H
#define CGMENTITIES_H

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
} CgmDeviceEntity;

typedef struct {
    int64 dateTime;
    uint32 eventIndex;
    uint32 sensorIndex;
    uint32 eventType;
    float32 eventValue;
    float32 rawValue[9];
} CgmHistoryEntity;

typedef struct {
    int64 dateTime;
    uint32 eventIndex;
    uint32 sensorIndex;
    uint8 bytes[26];
} CgmEncryptedHistoryEntity;

typedef struct {
    uint8 primary;
    uint32 bat;
    int64 dateTime;
    uint32 state;
    float32 glucose;
    CgmHistoryEntity history;
} CgmBroadcastEntity;

typedef struct {
    bool save;
    bool load;
    int32 vc;
    int32 i1;
    int32 i2;
    int16 real;
    int16 imag;
} CgmDeviceCheckEntity;

typedef struct {
    float32 et;
    float32 cf;
    float32 cf1;
    float32 cf2;
    float32 cf3;
    float32 cf4;
    float32 cf5;
    float32 cfh2;
    float32 cfh3;
    float32 cfh4;
    float32 ofs;
    float32 ofs1;
    float32 ofs2;
    float32 ofs3;
    float32 ofs4;
    float32 ofs5;
    float32 ofsh2;
    float32 ofsh3;
    float32 ofsh4;
    float32 ib;
    float32 ird;
    float32 inl1;
    float32 inl0;
    float32 cfls;
    float32 cfus;
    float32 sfl;
    float32 sfu;
    float32 rl;
    float32 ru;
    float32 rns;
    float32 rr;
    float32 rrf;
    float32 rrcsh;
    float32 rrcph;
    float32 rrsc;
    float32 il;
    float32 iu;
    float32 ir;
    float32 irf;
    float32 irsc;
    float32 irofs;
} CgmDefaultParamEntity;

#endif // CGMENTITIES_H

