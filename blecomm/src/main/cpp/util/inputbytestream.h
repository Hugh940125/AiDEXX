#ifndef INPUTBYTESTREAM_H
#define INPUTBYTESTREAM_H

#include "../devcomm/CLibrary/global.h"

#include <string>

using namespace std;

class InputByteStream
{
public:
    typedef enum {
        LITTLE_ENDIAN_MODE,
        BIG_ENDIAN_MODE
    } Endian;
public:
    InputByteStream(const uint8 *bytes, uint16 size, Endian endian);
    void skip(uint8 count);
    void clear();
    bool isEnd();
    uint8 balance();
    sint8 readByte();
    uint8 readUnsignedByte();
    bool readBoolean();
    int16 readShort();
    uint16 readUnsignedShort();
    int32 readInt();
    uint32 readUnsignedInt();
    string readAddress(uint16 length);

private:
    Endian endian;
    int index;
    int size;
    const uint8 *bytes;
};

#endif // INPUTBYTESTREAM_H
