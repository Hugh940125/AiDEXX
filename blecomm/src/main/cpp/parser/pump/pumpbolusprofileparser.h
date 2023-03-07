#ifndef PUMPBOLUSPROFILEPARSER_H
#define PUMPBOLUSPROFILEPARSER_H

#include "../../devcomm/CLibrary/global.h"
#include "../streamparser.h"
#include "../../constant/pump/pumpconstants.h"
#include "../../controller/pump/pumpcontroller.h"

class InputByteStream;
class PumpBolusProfileParser : public StreamParser
{
public:
    static const int MIN_BYTES_LENGTH = 16;
    
    PumpBolusProfileParser(const char *bytes, uint16 length) : StreamParser(bytes, length, InputByteStream::LITTLE_ENDIAN_MODE) {
        if (length < MIN_BYTES_LENGTH) LOGE("bytes too short");
    }

    const float32 *getBolusProfile();

protected:
    float32 value[4];

    virtual void parse();
};

#endif // PUMPBOLUSPROFILEPARSER_H
