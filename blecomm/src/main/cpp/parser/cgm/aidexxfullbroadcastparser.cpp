#include "aidexxfullbroadcastparser.h"
#include "aidexxbroadcastparser.h"
#include "aidexxscanresponseparser.h"
#include "../../util/inputbytestream.h"
#include "../../constant/cgm/aidexxconstants.h"
#include "../../devcomm/CLibrary/lib_checksum.h"

AidexXFullBroadcastParser::AidexXFullBroadcastParser(const char *bytes, uint16 length) {
    if (length < BROADCAST_BYTES_LENGTH + MIN_SCANRESPONSE_BYTES_LENGTH) LOGE("bytes too short");
    this->bytes = vector<uint8>(bytes, bytes+length);
}

const AidexXFullBroadcastEntity *AidexXFullBroadcastParser::getFullBroadcast() {
    if (!getBroadcast()) {
        return NULL;
    }
    
    if (!getScanResponse()) {
        return NULL;
    }
    
    return &fullBroadcast;
}

bool AidexXFullBroadcastParser::getBroadcast() {
    uint8 crc8 = LibChecksum_GetChecksum8Bit((const uint8 *)bytes.data(), BROADCAST_BYTES_LENGTH);
    if (crc8 != 0) {
        return false;
    }
    
    const AidexXBroadcastEntity *broadcast = AidexXBroadcastParser((const char *)bytes.data(), BROADCAST_BYTES_LENGTH - 1).getBroadcast();
    if (broadcast == NULL) {
        return false;
    }
    
    fullBroadcast.broadcast.timeOffset = broadcast->timeOffset;
    fullBroadcast.broadcast.status = broadcast->status;
    fullBroadcast.broadcast.calTemp = broadcast->calTemp;
    fullBroadcast.broadcast.trend = broadcast->trend;
    fullBroadcast.broadcast.historyCount = broadcast->historyCount;
    for (uint8 i = 0; i < broadcast->historyCount; i++) {
        fullBroadcast.broadcast.history[i].timeOffset = broadcast->history[i].timeOffset;
        fullBroadcast.broadcast.history[i].glucose = broadcast->history[i].glucose;
        fullBroadcast.broadcast.history[i].status = broadcast->history[i].status;
        fullBroadcast.broadcast.history[i].quality = broadcast->history[i].quality;
        fullBroadcast.broadcast.history[i].isValid = broadcast->history[i].isValid;
    }
    
    fullBroadcast.broadcast.calIndex = broadcast->calIndex;
    
    for (uint8 i = 0; i < sizeof(broadcast->reserved) / sizeof(broadcast->reserved[0]); i++) {
        fullBroadcast.broadcast.reserved[i] = broadcast->reserved[i];
    }
    
    return true;
}

bool AidexXFullBroadcastParser::getScanResponse() {
    const AidexXScanResponseEntity *scanResponse = AidexXScanResponseParser((const char *)bytes.data() + BROADCAST_BYTES_LENGTH, bytes.size() - BROADCAST_BYTES_LENGTH).getScanResponse();
    if (scanResponse == NULL) {
        return false;
    }
    
    fullBroadcast.scanResponse.isNativePaired = scanResponse->isNativePaired;
    fullBroadcast.scanResponse.isInitialized = scanResponse->isInitialized;
    
    return true;
}
