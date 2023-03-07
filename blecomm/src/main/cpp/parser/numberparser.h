#ifndef NUMBERPARSER_H
#define NUMBERPARSER_H

#include "../devcomm/CLibrary/global.h"

class NumberParser
{
public:
    typedef enum {
        LITTLE_ENDIAN_MODE,
        BIG_ENDIAN_MODE
    } Endian;
public:
    NumberParser(const char *bytes, uint16 length, bool isSigned, Endian endian = LITTLE_ENDIAN_MODE);
    int64 getNumber() { return number; }

private:
    int64 number = 0;
};

#endif // NUMBERPARSER_H
