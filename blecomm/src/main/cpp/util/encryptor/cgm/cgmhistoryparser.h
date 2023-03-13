#ifndef CGMHISTORYPARSER_H
#define CGMHISTORYPARSER_H

#include "../../devcomm/CLibrary/global.h"
#include "../streamparser.h"
#include "cgmentities.h"

class InputByteStream;
class CgmHistoryParser : public StreamParser
{
public:
    static const int MIN_BYTES_LENGTH = 9;

    CgmHistoryParser(const char *bytes, uint16 length) : StreamParser(bytes, length, InputByteStream::LITTLE_ENDIAN_MODE) {
        if (length < MIN_BYTES_LENGTH) LOGE("bytes length: %d", length);
    }

    const CgmHistoryEntity *getHistory();
    const CgmHistoryEntity *getFullHistory();

protected:
    CgmHistoryEntity history;

    virtual void parse();
    int readRaw();
};

#endif // CGMHISTORYPARSER_H
