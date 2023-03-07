#ifndef PUMPBROADCASTPARSER_H
#define PUMPBROADCASTPARSER_H

#include "../../devcomm/CLibrary/global.h"
#include "pumpentities.h"
#include "pumphistoryparser.h"

#include <vector>

using namespace std;

class PumpBroadcastParser
{
public:
    static const int MIN_BYTES_LENGTH = 18;

    PumpBroadcastParser(const char *bytes, uint16 length);
    const PumpBroadcastEntity *getBroadcast();

private:
    vector<uint8> bytes;
    PumpBroadcastEntity broadcast;
};

#endif // PUMPBROADCASTPARSER_H
