#ifndef CGMBROADCASTPARSER_H
#define CGMBROADCASTPARSER_H

#include "../../devcomm/CLibrary/global.h"
#include "cgmentities.h"
#include "cgmhistoryparser.h"

#include <vector>

using namespace std;

class CgmBroadcastParser
{
public:
    static const int MIN_BYTES_LENGTH = 20;

    CgmBroadcastParser(const char *bytes, uint16 length);
    const CgmBroadcastEntity *getBroadcast();

private:
    vector<uint8> bytes;
    CgmBroadcastEntity broadcast;
};

#endif // CGMBROADCASTPARSER_H
