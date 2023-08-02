#include "aidexxscanresponseparser.h"
#include "../../util/inputbytestream.h"
#include "../../constant/cgm/aidexxconstants.h"

const AidexXScanResponseEntity *AidexXScanResponseParser::getScanResponse() {
    try
    {
        uint8 flag = ibs->readUnsignedByte();
        scanResponse.isBleNativePaired = (flag & 0x01) > 0;
        scanResponse.isAesInitialized = (flag & 0x02) > 0;;
        return &scanResponse;
    }
    catch (...)
    {
        ibs->clear();
        LOGE("ScanResponse Parse Error");
        return NULL;
    }
}
