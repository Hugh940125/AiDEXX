#ifndef CGMDEFAULTPARAMPARSER_H
#define CGMDEFAULTPARAMPARSER_H

#include "../../devcomm/CLibrary/global.h"
#include "../streamparser.h"
#include "../../constant/cgm/cgmconstants.h"
#include "cgmentities.h"

class InputByteStream;
class CgmDefaultParamParser : public StreamParser
{
public:
    static const int MIN_BYTES_LENGTH = 84;

    CgmDefaultParamParser(const char *bytes, uint16 length) : StreamParser(bytes, length, InputByteStream::LITTLE_ENDIAN_MODE) {
        if (length < MIN_BYTES_LENGTH) LOGE("bytes length: %d", length);
    }

    const CgmDefaultParamEntity *getCgmDefaultParam();
    const float32 *getCgmDefaultParamArray();

protected:
    CgmDefaultParamEntity defaultParam;
    float32 value[CgmDefaultParam::DP_COUNT];

    void parse();
};

#endif // CGMDEFAULTPARAMPARSER_H
