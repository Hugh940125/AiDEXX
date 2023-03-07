#include "bgmstateparser.h"

#include "../../util/byteutils.h"

BgmStateParser::BgmStateParser(const char *bytes, uint16 length) {
    if (length < MIN_BYTES_LENGTH) LOGE("bytes too short");
    this->bytes = vector<uint8>(bytes, bytes+length);
}

int32 BgmStateParser::getValue() {
    if (this->bytes.size() == 1) {
        return this->bytes[0];
    }
    
    return BigEndianByteUtils::byteToUnsignedShort(&this->bytes[0]);
}
