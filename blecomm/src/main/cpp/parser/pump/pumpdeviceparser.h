#ifndef PUMPDEVICEPARSER_H
#define PUMPDEVICEPARSER_H

#include "../../devcomm/CLibrary/global.h"
#include "../streamparser.h"
#include "pumpentities.h"

class InputByteStream;
class PumpDeviceParser : public StreamParser
{
public:
    static const int MIN_BYTES_LENGTH = 20;
    
    PumpDeviceParser(const char *bytes, uint16 length) : StreamParser(bytes, length, InputByteStream::LITTLE_ENDIAN_MODE) {
        if (length < MIN_BYTES_LENGTH) LOGE("bytes too short");
    }

    const PumpDeviceEntity *getDevice();

protected:
    PumpDeviceEntity device;

    virtual void parse();
};

#endif // PUMPDEVICEPARSER_H
