#ifndef CGMHISTORIESPARSER_H
#define CGMHISTORIESPARSER_H

#include "../../devcomm/CLibrary/global.h"
#include "cgmhistoryparser.h"

class CgmHistoriesParser : public CgmHistoryParser
{
public:
    static const int MIN_BYTES_LENGTH = 7;

    CgmHistoriesParser(const char *bytes, uint16 length) : CgmHistoryParser(bytes, length) {
        if (length < MIN_BYTES_LENGTH) LOGE("bytes length: %d", length);
        first = true;
//        mSensorIndex = sensorIndex;
    }

    bool hasNext();

protected:
    virtual void parse() override;

private:
    bool first;
    uint8 mSensorIndex;
};

#endif // CGMHISTORIESPARSER_H
