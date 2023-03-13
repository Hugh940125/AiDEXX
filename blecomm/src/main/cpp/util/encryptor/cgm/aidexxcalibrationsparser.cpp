#include "aidexxcalibrationsparser.h"
#include "../../util/inputbytestream.h"


bool AidexXCalibrationsParser::hasNext() {
    return !ibs->isEnd();
}

const AidexXCalibrationEntity *AidexXCalibrationsParser::getCalibration() {
    try
    {
        if (first) {
            calibration.index = ibs->readUnsignedShort();
            first = false;
        } else {
            calibration.index++;
        }
        calibration.timeOffset = ibs->readUnsignedShort(); 
        calibration.referenceGlucose = ibs->readUnsignedShort();
        calibration.isValid = (calibration.referenceGlucose != 0xFFFF);
        return &calibration;
    }
    catch (...)
    {
        ibs->clear();
        LOGE("Calibration Parse Error");
        return NULL;
    }
}
