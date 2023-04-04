#include "aidexxfullbroadcastparser.h"
#include "aidexxbroadcastparser.h"
#include "aidexxscanresponseparser.h"
#include "../../util/inputbytestream.h"
#include "../../constant/cgm/aidexxconstants.h"
#include "../../devcomm/CLibrary/lib_checksum.h"

AidexXFullBroadcastParser::AidexXFullBroadcastParser(const char *bytes, uint16 length) {
    if (length < MIN_BYTES_LENGTH) LOGE("bytes too short");
    this->bytes = vector<uint8>(bytes, bytes+length);
}

const AidexXFullBroadcastEntity *AidexXFullBroadcastParser::getFullBroadcast() {
    const AidexXBroadcastEntity *broadcast = getBroadcast();
    if (broadcast == NULL) {
        return NULL;
    }
    
    fullBroadcast.historyTimeOffset = broadcast->timeOffset;
    fullBroadcast.historyCount = broadcast->historyCount;
    fullBroadcast.status = broadcast->status;
    fullBroadcast.calTemp = broadcast->calTemp;
    fullBroadcast.trend = broadcast->trend;
    for (uint8 i = 0; i < broadcast->historyCount; i++) {
        fullBroadcast.history[i].timeOffset = broadcast->history[i].timeOffset;
        fullBroadcast.history[i].glucose = broadcast->history[i].glucose;
        fullBroadcast.history[i].status = broadcast->history[i].status;
        fullBroadcast.history[i].quality = broadcast->history[i].quality;
        fullBroadcast.history[i].isValid = broadcast->history[i].isValid;
    }
    
    const AidexXScanResponseEntity *scanResponse = getScanResponse();
    if (scanResponse == NULL) {
        return NULL;
    }
    
    fullBroadcast.calTimeOffset = scanResponse->calTimeOffset;
    fullBroadcast.isPaired = scanResponse->isPaired;
    fullBroadcast.isInitialized = scanResponse->isInitialized;

    return &fullBroadcast;
}

const AidexXBroadcastEntity *AidexXFullBroadcastParser::getBroadcast() {
    uint8 broadcastLength = bytes[bytes.size() - 2];
    
    if (broadcastLength > bytes.size()) {
        return NULL;
    }
    
    uint8 crc8 = LibChecksum_GetChecksum8Bit((const uint8 *)bytes.data(), broadcastLength);
    if (crc8 != 0) {
        return NULL;
    }
    
    return AidexXBroadcastParser((const char *)bytes.data(), broadcastLength - 1).getBroadcast();
}

const AidexXScanResponseEntity *AidexXFullBroadcastParser::getScanResponse() {
    uint8 scanRspLength = bytes[bytes.size() - 1];
    
    if (scanRspLength > bytes.size()) {
        return NULL;
    }
    
    return AidexXScanResponseParser((const char *)bytes.data() + bytes.size() - scanRspLength - 2, scanRspLength).getScanResponse();
}
