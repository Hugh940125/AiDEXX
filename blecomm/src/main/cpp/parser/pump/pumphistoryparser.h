#ifndef PUMPHISTORYPARSER_H
#define PUMPHISTORYPARSER_H

#include "../../devcomm/CLibrary/global.h"
#include "../streamparser.h"
#include "pumpentities.h"

class InputByteStream;
class PumpHistoryParser : public StreamParser
{
public:
    static const int MIN_BYTES_LENGTH = 18;

    PumpHistoryParser(const char *bytes, uint16 length) : StreamParser(bytes, length, InputByteStream::LITTLE_ENDIAN_MODE) {
        if (length < MIN_BYTES_LENGTH) LOGE("bytes too short");
    }

    const PumpHistoryEntity *getHistory();

protected:
    PumpHistoryEntity history;

    virtual void parse();
};

#endif // PUMPHISTORYPARSER_H
