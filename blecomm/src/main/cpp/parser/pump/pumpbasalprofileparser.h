#ifndef PUMPBASALPROFILEPARSER_H
#define PUMPBASALPROFILEPARSER_H

#include "../../devcomm/CLibrary/global.h"
#include "../streamparser.h"
#include "../../constant/pump/pumpconstants.h"
#include "../../controller/pump/pumpcontroller.h"

class InputByteStream;
class PumpBasalProfileParser : public StreamParser
{
public:
    static const int MIN_BYTES_LENGTH = PumpController::BASAL_PROFILE_COUNT;
    
    PumpBasalProfileParser(const char *bytes, uint16 length) : StreamParser(bytes, length, InputByteStream::LITTLE_ENDIAN_MODE) {
        if (length < MIN_BYTES_LENGTH) LOGE("bytes too short");
    }

    const float32 *getBasalProfile();

protected:
    float32 value[PumpController::BASAL_PROFILE_COUNT];

    virtual void parse();
};

#endif // PUMPBASALPROFILEPARSER_H
