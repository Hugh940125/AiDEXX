#ifndef AIDEXXFULLBROADCASTPARSER_H
#define AIDEXXFULLBROADCASTPARSER_H

#include "../../devcomm/CLibrary/global.h"
#include "aidexxentities.h"
#include <vector>

class AidexXFullBroadcastParser
{
public:
    static const int MIN_BYTES_LENGTH = 7;

    AidexXFullBroadcastParser(const char *bytes, uint16 length);
    const AidexXFullBroadcastEntity *getFullBroadcast();
    const AidexXBroadcastEntity *getBroadcast();
    const AidexXScanResponseEntity *getScanResponse();
    
private:
    vector<uint8> bytes;
    AidexXFullBroadcastEntity fullBroadcast;
};

#endif // AIDEXXFULLBROADCASTPARSER_H
