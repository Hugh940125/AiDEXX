#ifndef CGMDEVICEPARSER_H
#define CGMDEVICEPARSER_H

#include "../../devcomm/CLibrary/global.h"
#include "../streamparser.h"
#include "cgmentities.h"

class InputByteStream;
class CgmDeviceParser : public StreamParser
{
public:
    static const int MIN_BYTES_LENGTH = 20;
    
    CgmDeviceParser(const char *bytes, uint16 length) : StreamParser(bytes, length, InputByteStream::LITTLE_ENDIAN_MODE) {
        if (length < MIN_BYTES_LENGTH) LOGE("bytes too short");
    }

    const CgmDeviceEntity *getDevice();

protected:
    CgmDeviceEntity device;

    virtual void parse();
};

#endif // CGMDEVICEPARSER_H
