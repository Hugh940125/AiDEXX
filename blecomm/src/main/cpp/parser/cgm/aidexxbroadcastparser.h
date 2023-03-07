#ifndef AIDEXXBROADCASTPARSER_H
#define AIDEXXBROADCASTPARSER_H

#include "../../devcomm/CLibrary/global.h"
#include "../streamparser.h"
#include "aidexxentities.h"

class InputByteStream;
class AidexXBroadcastParser : public StreamParser
{
public:
    static const int MIN_BYTES_LENGTH = 5;

    AidexXBroadcastParser(const char *bytes, uint16 length) : StreamParser(bytes, length, InputByteStream::LITTLE_ENDIAN_MODE) {
        if (length < MIN_BYTES_LENGTH) LOGE("bytes too short");
    }

    const AidexXBroadcastEntity *getBroadcast();

private:
    AidexXBroadcastEntity broadcast;
};

#endif // AIDEXXBROADCASTPARSER_H
