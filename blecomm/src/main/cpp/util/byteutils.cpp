#include "byteutils.h"


bool ByteUtils::byteToBoolean(uint8 byte) {
    return byte==1;
}

uint8 ByteUtils::booleanToBytes(bool v) {
    if (v) {
        return (uint8) 1;
    } else {
        return (uint8) 0;
    }
}

void ByteUtils::copy(char *dst, const char *src, uint32 size) {
    while (size-- > 0)
        *dst++ = *src++;
}

bool ByteUtils::compare(const char *dst, const char *src, uint32 size) {
    while (size-- > 0) {
        if (*dst++ != *src++)
            return false;
    }
    return true;
}

string ByteUtils::trim(const string &str) {
    int s = (int)str.find_first_not_of(" ");
    if (s < 0) return "";
    int e = (int)str.find_last_not_of(" ");
    return str.substr(s,e-s+1);
}

string ByteUtils::bytesToUtf8String(const char *data, uint16 length) {
    if (isUtf8String(data, length))
        return string(data, length);
    else
        return string("");
}

bool ByteUtils::isUtf8String(const char *data, uint16 length)
{
    uint32 check_sub    = 0;
    uint16 i            = 0;
    uint16 j            = 0;
    for (i = 0; i < length; i++)
    {
        if (check_sub == 0)
        {
            if ((data[i] >> 7) == 0) //0xxx xxxx
                continue;

            /* 获取单个 utf8 除了头部所占用字节个数 */
            struct
            {
                unsigned char cal;
                unsigned char cmp;
            } Utf8NumMap[] = {
                {0xE0,      0xC0},      //110xxxxx
                {0xF0,      0xE0},      //1110xxxx
                {0xF8,      0xF0},      //11110xxx
                {0xFC,      0xF8},      //111110xx
                {0xFE,      0xFC},      //1111110x
            };

            for (j = 0; j < (sizeof(Utf8NumMap)/sizeof(Utf8NumMap[0])); j++)
            {
                if ((data[i] & Utf8NumMap[j].cal) == Utf8NumMap[j].cmp)
                {
                    //printf("%u:%u:%x\n", __LINE__, i, utf[i]);
                    check_sub = j + 1;
                    break;
                }
            }
            if(0 == check_sub)
                return false;
        }
        else
        {
            /* 校验字节是否合法 */
            if ((data[i] & 0xC0) != 0x80)
                return false;
            check_sub--;
        }
    }
    return true;
}

string ByteUtils::bytesToSnString(const char *address, uint16 length) {
    char *bytes = new char(length);
    ByteUtils::copy(bytes, (const char*)address, length);
    ByteUtils::bytesToSn(bytes, length);
    for (int i = 0; i < length; i++)
    {
        if ((uint8)bytes[i] < '0' || (uint8)bytes[i] > 'Z') {
            delete bytes;
            return string("");
        }
    }
    
    string s(bytes, length);
    delete bytes;
    return s;
}

void ByteUtils::bytesToSn(char *address, uint16 length) {
    for (int i = 0; i < length; i++)
    {
        uint8 value = address[i];
        if (value < 10) {
            value += '0';
        } else if (value < 36) {
            value += 'A' - 10;
        } else {
            value = 0xFF;
        }
        address[i] = value;
    }
}

void ByteUtils::snToBytes(char *address, uint16 length) {
    for (int i = 0; i < length; i++) {
        uint8 value = address[i];
        if ((value >= '0') && (value <= '9')) {
            value -= '0';
        } else if ((value >= 'A') && (value <= 'Z')) {
            value -= 'A' - 10;
        } else if ((value >= 'a') && (value <= 'z')) {
            value -= 'a' - 10;
        }
        address[i] = value;
    }
}


int16 LittleEndianByteUtils::byteToShort(const uint8 *bytes) {
    return (int16)byteToUnsignedShort(bytes);
}

uint16 LittleEndianByteUtils::byteToUnsignedShort(const uint8 *bytes) {
    return (((bytes[1] & 255) << 8) +
             (bytes[0] & 255));
}

int32 LittleEndianByteUtils::byteToInt(const uint8 *bytes) {
    return (((bytes[3]) << 24) +
            ((bytes[2] & 255) << 16) +
            ((bytes[1] & 255) << 8) +
            ((bytes[0] & 255)));
}

uint32 LittleEndianByteUtils::byteToUnsignedInt(const uint8* bytes) {
    return (((uint32)(bytes[3] & 255) << 24) +
            ((bytes[2] & 255) << 16) +
            ((bytes[1] & 255) << 8) +
            ((bytes[0] & 255)));
}

void LittleEndianByteUtils::shortToBytes(int16 v, uint8 *bytes) {
    unsignedShortToBytes((unsigned short)v, bytes);
}

void LittleEndianByteUtils::unsignedShortToBytes(uint16 v, uint8 *bytes) {
    bytes[0] = (uint8)((v >> 0) & 0xFF);
    bytes[1] = (uint8)((v >> 8) & 0xFF);
}

void LittleEndianByteUtils::intToBytes(int32 v, uint8 *bytes) {
    unsignedIntToBytes((uint32)v, bytes);
}

void LittleEndianByteUtils::unsignedIntToBytes(uint32 v, uint8 *bytes) {
    bytes[0] = (uint8)((v >> 0) & 0xFF);
    bytes[1] = (uint8)((v >> 8) & 0xFF);
    bytes[2] = (uint8)((v >> 16) & 0xFF);
    bytes[3] = (uint8)((v >> 24) & 0xFF);
}


int16 BigEndianByteUtils::byteToShort(const uint8 *bytes) {
    return (int16)byteToUnsignedShort(bytes);
}

uint16 BigEndianByteUtils::byteToUnsignedShort(const uint8 *bytes) {
    return (((bytes[0] & 255) << 8) +
             (bytes[1] & 255));
}

int32 BigEndianByteUtils::byteToInt(const uint8 *bytes) {
    return (((bytes[0]) << 24) +
            ((bytes[1] & 255) << 16) +
            ((bytes[2] & 255) << 8) +
            ((bytes[3] & 255)));
}

uint32 BigEndianByteUtils::byteToUnsignedInt(const uint8* bytes) {
    return (((uint32)(bytes[0] & 255) << 24) +
            ((bytes[1] & 255) << 16) +
            ((bytes[2] & 255) << 8) +
            ((bytes[3] & 255)));
}

void BigEndianByteUtils::shortToBytes(int16 v, uint8 *bytes) {
    unsignedShortToBytes((unsigned short)v, bytes);
}

void BigEndianByteUtils::unsignedShortToBytes(uint16 v, uint8 *bytes) {
    bytes[0] = (uint8)((v >> 8) & 0xFF);
    bytes[1] = (uint8)((v >> 0) & 0xFF);
}

void BigEndianByteUtils::intToBytes(int32 v, uint8 *bytes) {
    unsignedIntToBytes((uint32)v, bytes);
}

void BigEndianByteUtils::unsignedIntToBytes(uint32 v, uint8 *bytes) {
    bytes[0] = (uint8)((v >> 24) & 0xFF);
    bytes[1] = (uint8)((v >> 16) & 0xFF);
    bytes[2] = (uint8)((v >> 8) & 0xFF);
    bytes[3] = (uint8)((v >> 0) & 0xFF);
}
