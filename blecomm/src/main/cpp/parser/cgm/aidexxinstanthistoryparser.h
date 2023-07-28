#ifndef AIDEXXINSTANTHISTORYPARSER_H
#define AIDEXXINSTANTHISTORYPARSER_H

#ifdef __cplusplus

#ifdef __APPLE__
#include "global.h"
#include "streamparser.h"
#include "aidexxentities.h"
#else
#include "../../devcomm/CLibrary/global.h"
#include "../streamparser.h"
#include "aidexxentities.h"
#endif

class InputByteStream;
class AidexXInstantHistoryParser : public StreamParser
{
public:
    static const int MIN_BYTES_LENGTH = 14;

    AidexXInstantHistoryParser(const char *bytes, uint16 length) : StreamParser(bytes, length, InputByteStream::LITTLE_ENDIAN_MODE) {
        if (length < MIN_BYTES_LENGTH) LOGE("bytes too short");
    }

    const AidexXInstantHistoryEntity *getInstantHistory();

private:
    AidexXInstantHistoryEntity instantHistory;
};

#endif

#endif // AIDEXXNOTIFICATIONPARSER_H
