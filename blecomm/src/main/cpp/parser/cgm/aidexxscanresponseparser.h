#ifndef AIDEXXSCANRESPONSEPARSER_H
#define AIDEXXSCANRESPONSEPARSER_H

#include "../../devcomm/CLibrary/global.h"
#include "../streamparser.h"
#include "aidexxentities.h"

class InputByteStream;
class AidexXScanResponseParser : public StreamParser
{
public:
    static const int MIN_BYTES_LENGTH = 1;

    AidexXScanResponseParser(const char *bytes, uint16 length) : StreamParser(bytes, length, InputByteStream::LITTLE_ENDIAN_MODE) {
        if (length < MIN_BYTES_LENGTH) LOGE("bytes too short");
    }

    const AidexXScanResponseEntity *getScanResponse();

private:
    AidexXScanResponseEntity scanResponse;
};

#endif // AIDEXXSCANRESPONSEPARSER_H
