#ifndef AIDEXXHISTORIESPARSER_H
#define AIDEXXHISTORIESPARSER_H

#include "../../devcomm/CLibrary/global.h"
#include "../streamparser.h"
#include "aidexxentities.h"

class InputByteStream;
class AidexXHistoriesParser : public StreamParser
{
public:
    AidexXHistoriesParser(const char *bytes, uint16 length) : StreamParser(bytes, length, InputByteStream::LITTLE_ENDIAN_MODE) {
        first = true;
    }

    bool hasNext();
    const AidexXHistoryEntity *getHistory();
    const AidexXRawHistoryEntity *getRawHistory();

private:
    bool first;
    AidexXHistoryEntity history;
    AidexXRawHistoryEntity rawHistory;
};

#endif // AIDEXXHISTORIESPARSER_H
