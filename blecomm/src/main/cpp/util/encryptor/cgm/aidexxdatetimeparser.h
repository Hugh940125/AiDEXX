#ifndef AIDEXXDATETIMEPARSER_H
#define AIDEXXDATETIMEPARSER_H

#include "../../devcomm/CLibrary/global.h"
#include "../streamparser.h"
#include "aidexxentities.h"

class InputByteStream;
class AidexXDatetimeParser : public StreamParser
{
public:
    static const int MIN_BYTES_LENGTH = 9;

    AidexXDatetimeParser(const char *bytes, uint16 length) : StreamParser(bytes, length, InputByteStream::LITTLE_ENDIAN_MODE) {
        if (length < MIN_BYTES_LENGTH) LOGE("bytes too short");
    }

    const AidexXDatetimeEntity *getDatetime();

private:
    AidexXDatetimeEntity datetime;

};

#endif // AIDEXXDATETIMEPARSER_H
