#ifndef BYTEUTILS_H
#define BYTEUTILS_H

#include "../devcomm/CLibrary/global.h"

#include<string>

using namespace std;

class ByteUtils
{
public:
    static bool byteToBoolean(uint8 byte);
    static uint8 booleanToBytes(bool v);
    static void copy(char *const dst, const char *src, uint32 size);
    static bool compare(const char *dst, const char *src, uint32 size);
    static string trim(const string &str);
    static string bytesToUtf8String(const char *data, uint16 length);
    static bool isUtf8String(const char *data, uint16 length);
    static string bytesToSnString(const char *address, uint16 length);
    static string bytesToHexString(const char *data, uint16 length);
    static void bytesToSn(char *address, uint16 length);
    static void snToBytes(char *address, uint16 length);
};

class LittleEndianByteUtils
{
public:
    static int16 byteToShort(const uint8 *bytes);
    static uint16 byteToUnsignedShort(const uint8 *bytes);
    static int32 byteToInt(const uint8 *bytes);
    static uint32 byteToUnsignedInt(const uint8* bytes);
    static void shortToBytes(int16 v, uint8 *bytes);
    static void unsignedShortToBytes(uint16 v, uint8 *bytes);
    static void intToBytes(int32 v, uint8 *bytes);
    static void unsignedIntToBytes(uint32 v, uint8 *bytes);
};

class BigEndianByteUtils
{
public:
    static int16 byteToShort(const uint8 *bytes);
    static uint16 byteToUnsignedShort(const uint8 *bytes);
    static int32 byteToInt(const uint8 *bytes);
    static uint32 byteToUnsignedInt(const uint8* bytes);
    static void shortToBytes(int16 v, uint8 *bytes);
    static void unsignedShortToBytes(uint16 v, uint8 *bytes);
    static void intToBytes(int32 v, uint8 *bytes);
    static void unsignedIntToBytes(uint32 v, uint8 *bytes);
};

#endif // BYTEUTILS_H
