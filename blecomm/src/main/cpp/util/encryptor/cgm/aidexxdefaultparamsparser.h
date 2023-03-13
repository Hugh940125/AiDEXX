#ifndef AIDEXXDEFAULTPARAMSPARSER_H
#define AIDEXXDEFAULTPARAMSPARSER_H

#include "../../devcomm/CLibrary/global.h"
#include "../streamparser.h"
#include "aidexxentities.h"

class InputByteStream;
class AidexXDefaultParamsParser : public StreamParser
{
public:
    static const int MIN_BYTES_LENGTH = 48;

    AidexXDefaultParamsParser(const char *bytes, uint16 length) : StreamParser(bytes, length, InputByteStream::LITTLE_ENDIAN_MODE) {
        if (length < MIN_BYTES_LENGTH) LOGE("bytes too short");
        index = 0;
    }

    bool hasNext();
    float32 getParam();

private:
    int index;
};

#endif // AIDEXXDEFAULTPARAMSPARSER_H
