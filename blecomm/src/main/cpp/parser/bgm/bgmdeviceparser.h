#ifndef BGMDEVICEPARSER_H
#define BGMDEVICEPARSER_H

#include "../../devcomm/CLibrary/global.h"
#include "../streamparser.h"
#include "bgmentities.h"

class InputByteStream;
class BgmDeviceParser : public StreamParser
{
public:
    static const int MIN_BYTES_LENGTH = 20;
    
    BgmDeviceParser(const char *bytes, uint16 length) : StreamParser(bytes, length, InputByteStream::BIG_ENDIAN_MODE) {
        if (length < MIN_BYTES_LENGTH) LOGE("bytes too short");
    }

    const BgmDeviceEntity *getDevice();

protected:
    BgmDeviceEntity device;

    virtual void parse();
};

#endif // BGMDEVICEPARSER_H
