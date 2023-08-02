#include "inputbytestream.h"
#include "byteutils.h"
#include <cstring>

InputByteStream::InputByteStream(const uint8 *bytes, uint16 size, Endian mode)
{
    index = 0;
    this->endian = mode;
    this->size = size;
    this->bytes = bytes;
}

void InputByteStream::skip(uint8 count) {
    if (index+count>size) {
        throw -1;
    }
    index += count;
}

void InputByteStream::clear() {
    index = size;
}

bool InputByteStream::isEnd() {
    return index >= size;
}

uint8 InputByteStream::balance() {
    if (index > size) {
        throw -1;
    }
    return size - index;
}

sint8 InputByteStream::readByte() {
    if (index+1>size) {
        throw -1;
    }
    sint8 value = bytes[index];
    index += 1;
    return value;
}

uint8 InputByteStream::readUnsignedByte() {
    if (index+1>size) {
        throw -1;
    }
    uint8 value = bytes[index];
    index += 1;
    return value;
}

bool InputByteStream::readBoolean() {
    if (index+1>size) {
        throw -1;
    }
    bool value = ByteUtils::byteToBoolean(bytes[index]);
    index += 1;
    return value;
}

int16 InputByteStream::readShort() {
    if (index+2>size) {
        throw -1;
    }
    int16 value = (endian == LITTLE_ENDIAN_MODE)
            ? LittleEndianByteUtils::byteToShort(bytes+index)
            : BigEndianByteUtils::byteToShort(bytes+index);
    index += 2;
    return value;
}

uint16 InputByteStream::readUnsignedShort() {
    if (index+2>size) {
        throw -1;
    }
    uint16 value = (endian == LITTLE_ENDIAN_MODE)
            ? LittleEndianByteUtils::byteToUnsignedShort(bytes+index)
            : BigEndianByteUtils::byteToUnsignedShort(bytes+index);
    index += 2;
    return value;
}

int32 InputByteStream::readInt() {
    if (index+4>size) {
        throw -1;
    }
    int32 value = (endian == LITTLE_ENDIAN_MODE)
            ? LittleEndianByteUtils::byteToInt(bytes+index)
            : BigEndianByteUtils::byteToInt(bytes+index);
    index += 4;
    return value;
}

uint32 InputByteStream::readUnsignedInt() {
    if (index+4>size) {
        throw -1;
    }
    uint32 value = (endian == LITTLE_ENDIAN_MODE)
            ? LittleEndianByteUtils::byteToUnsignedInt(bytes+index)
            : BigEndianByteUtils::byteToUnsignedInt(bytes+index);
    index += 4;
    return value;
}

string InputByteStream::readAddress(uint16 length) {
    if (index+length>size) {
        throw -1;
    }
    string address = ByteUtils::bytesToSnString((char *)this->bytes, length);
    index += length;
    return address;
}
