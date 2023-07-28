#ifndef AIDEXXDEVICEPARSER_H
#define AIDEXXDEVICEPARSER_H

#ifdef __cplusplus

#include "global.h"
#include "streamparser.h"
#include "aidexxentities.h"

class InputByteStream;
class AidexXDeviceParser : public StreamParser
{
public:
    static const int MIN_BYTES_LENGTH = 6;

    AidexXDeviceParser(const char *bytes, uint16 length) : StreamParser(bytes, length, InputByteStream::LITTLE_ENDIAN_MODE) {
        if (length < MIN_BYTES_LENGTH) LOGE("bytes too short");
    }

    const AidexXDeviceEntity *getDevice();

private:
    AidexXDeviceEntity device;
};

#endif

#endif // AIDEXXDEVICEPARSER_H
