#include "aidexxscanresponseparser.h"
#include "../../util/inputbytestream.h"
#include "../../constant/cgm/aidexxconstants.h"

const AidexXScanResponseEntity *AidexXScanResponseParser::getScanResponse() {
    try
    {
        uint8 flag = ibs->readUnsignedByte();
        scanResponse.isPaired = (flag & 0x01) > 0;
        scanResponse.isInitialized = (flag & 0x02) > 0;;
        scanResponse.calTimeOffset = ibs->readUnsignedShort();
        return &scanResponse;
    }
    catch (...)
    {
        ibs->clear();
        LOGE("ScanResponse Parse Error");
        return NULL;
    }
}
