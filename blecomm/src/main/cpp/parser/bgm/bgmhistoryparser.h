#ifndef BGMHISTORYPARSER_H
#define BGMHISTORYPARSER_H

#include "../../devcomm/CLibrary/global.h"
#include "../streamparser.h"
#include "bgmentities.h"

class InputByteStream;
class BgmHistoryParser : public StreamParser
{
public:
    static const int MIN_BYTES_LENGTH = 18;
    
    BgmHistoryParser(const char *bytes, uint16 length) : StreamParser(bytes, length, InputByteStream::BIG_ENDIAN_MODE) {
        if (length < MIN_BYTES_LENGTH) LOGE("bytes too short");
    }

    const BgmHistoryEntity *getHistory();

protected:
    BgmHistoryEntity history;

    virtual void parse();
};

#endif // BGMHISTORYPARSER_H
