#ifndef CGMDEVICECHECKPARSER_H
#define CGMDEVICECHECKPARSER_H

#include "../../devcomm/CLibrary/global.h"
#include "../streamparser.h"
#include "cgmentities.h"

class InputByteStream;
class CgmDeviceCheckParser : public StreamParser
{
public:
    static const int MIN_BYTES_LENGTH = 24;

    CgmDeviceCheckParser(const char *bytes, uint16 length) : StreamParser(bytes, length, InputByteStream::LITTLE_ENDIAN_MODE) {
        if (length < MIN_BYTES_LENGTH) LOGE("bytes length: %d", length);
    }

    const CgmDeviceCheckEntity *getCgmDeviceCheck();

protected:
    CgmDeviceCheckEntity deviceCheck;

    void parse();
};

#endif // CGMDEVICECHECKPARSER_H
