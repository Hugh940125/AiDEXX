#include "pumpbroadcastparser.h"
#include "../../constant/cgm/cgmconstants.h"
#include "../../util/byteutils.h"

#include <cstring>

PumpBroadcastParser::PumpBroadcastParser(const char *bytes, uint16 length) {
    if (length < MIN_BYTES_LENGTH) LOGE("Broadcast bytes too short");
    this->bytes = vector<uint8>(bytes, bytes+length);
}

const PumpBroadcastEntity *PumpBroadcastParser::getBroadcast() {
    broadcast.historyExpired = LittleEndianByteUtils::byteToUnsignedShort(&bytes[12]) & 0x8000;
    broadcast.history = *(PumpHistoryParser((const char *)bytes.data(), PumpHistoryParser::MIN_BYTES_LENGTH).getHistory());

    return &broadcast;
}
