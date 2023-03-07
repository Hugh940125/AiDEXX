#ifndef PUMPSETTINGPARSER_H
#define PUMPSETTINGPARSER_H

#include "../../devcomm/CLibrary/global.h"
#include "../streamparser.h"
#include "../../constant/pump/pumpconstants.h"

class InputByteStream;
class PumpSettingParser : public StreamParser
{
public:
    static const int MIN_BYTES_LENGTH = 20;
    
    PumpSettingParser(const char *bytes, uint16 length) : StreamParser(bytes, length, InputByteStream::LITTLE_ENDIAN_MODE) {
        if (length < MIN_BYTES_LENGTH) LOGE("bytes too short");
    }

    const float32 *getSetting();

protected:
    float32 value[PumpSetting::SETTING_COUNT];

    virtual void parse();
};

#endif // PUMPSETTINGPARSER_H
