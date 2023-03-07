#ifndef AIDEXXCALIBRATIONSPARSER_H
#define AIDEXXCALIBRATIONSPARSER_H

#include "../../devcomm/CLibrary/global.h"
#include "../streamparser.h"
#include "aidexxentities.h"

class InputByteStream;
class AidexXCalibrationsParser : public StreamParser
{
public:
    AidexXCalibrationsParser(const char *bytes, uint16 length) : StreamParser(bytes, length, InputByteStream::LITTLE_ENDIAN_MODE) {
        first = true;
    }

    bool hasNext();
    const AidexXCalibrationEntity *getCalibration();

private:
    bool first;
    AidexXCalibrationEntity calibration;
};

#endif // AIDEXXCALIBRATIONSPARSER_H
