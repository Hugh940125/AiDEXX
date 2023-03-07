#include "numberparser.h"
#include "../util/byteutils.h"

NumberParser::NumberParser(const char *bytes, uint16 length, bool isSigned, Endian endian)
{
    const uint8 *u8_bytes = (const uint8 *)bytes;
    switch (length) {
    case 1:
        number = isSigned ? (int8)*bytes : (uint8)*bytes;
        break;
    case 2:
        number = endian == LITTLE_ENDIAN_MODE ?
                    (isSigned?
                         LittleEndianByteUtils::byteToShort(u8_bytes) :
                         LittleEndianByteUtils::byteToUnsignedShort(u8_bytes)) :
                    (isSigned ?
                         BigEndianByteUtils::byteToShort(u8_bytes) :
                         BigEndianByteUtils::byteToUnsignedShort(u8_bytes));
        break;
    case 4:
        number = endian == LITTLE_ENDIAN_MODE ?
                    (isSigned ?
                         LittleEndianByteUtils::byteToInt(u8_bytes) :
                         LittleEndianByteUtils::byteToUnsignedInt(u8_bytes)) :
                    (isSigned ?
                         BigEndianByteUtils::byteToInt(u8_bytes) :
                         BigEndianByteUtils::byteToUnsignedInt(u8_bytes));
        break;
    default:
        break;
    }
}
