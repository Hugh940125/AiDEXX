#include "aidexxhistoriesparser.h"
#include "../../util/inputbytestream.h"
#include "../../constant/cgm/aidexxconstants.h"


bool AidexXHistoriesParser::hasNext() {
    return !ibs->isEnd();
}

const AidexXHistoryEntity *AidexXHistoriesParser::getHistory() {
    try
    {
        if (first) {
            history.timeOffset = ibs->readUnsignedShort();
            first = false;
        } else {
            history.timeOffset += AidexxHistory::TIME_INTERVAL;
        }
        uint16 sg_and_state = ibs->readUnsignedShort();
        history.isValid = (sg_and_state != 0xFFFF);
        if (history.isValid) {
            history.glucose = sg_and_state & 0x3FF;
            history.status = (sg_and_state & 0xC00) >> 10;
        }
        return &history;
    }
    catch (...)
    {
        ibs->clear();
        LOGE("History Parse Error");
        return NULL;
    }   
}

const AidexXRawHistoryEntity *AidexXHistoriesParser::getRawHistory() {
    try
    {
        if (first) {
            rawHistory.timeOffset = ibs->readUnsignedShort();
            first = false;
        } else {
            rawHistory.timeOffset += AidexxHistory::TIME_INTERVAL;;
        }
        rawHistory.i1 = (float32)(ibs->readShort()) / 100.0;
        rawHistory.i2 = (float32)(ibs->readShort()) / 100.0;
        uint16 vc = ibs->readUnsignedByte();
        rawHistory.vc = (float32)(vc) / 100.0;
        rawHistory.isValid = (vc != 0xFFFF);
        return &rawHistory;
    }
    catch (...)
    {
        ibs->clear();
        LOGE("Raw History Parse Error");
        return NULL;
    }
}
