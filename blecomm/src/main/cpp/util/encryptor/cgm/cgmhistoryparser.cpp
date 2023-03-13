#include "cgmhistoryparser.h"
#include "../../constant/cgm/cgmconstants.h"
#include "../../util/inputbytestream.h"
#include "../../util/array.h"

const CgmHistoryEntity *CgmHistoryParser::getHistory() {
    try {
        parse();
    } catch (...) {
        LOGE("History Parse Error");
    }
    return &history;
}

const CgmHistoryEntity *CgmHistoryParser::getFullHistory() {
    try {
        parse();
        readRaw();
    } catch (...) {
        LOGE("History Parse Error");
    }
    return &history;
}

void CgmHistoryParser::parse() {
    history.dateTime = (int64)(ibs->readUnsignedInt()) + CgmHistory::YEAR_2000;
    history.eventIndex = ibs->readUnsignedShort();
    history.sensorIndex = ibs->readUnsignedByte();
    history.eventType = ibs->readUnsignedByte();
    switch (history.eventType) {
    case CgmHistory::HISTORY_SENSOR_NEW:
        history.eventValue = (float)ibs->readByte() * 1.0;
        break;
    default:
        history.eventValue = (float)ibs->readUnsignedByte() / 10.0;
        break;
    }
}

int CgmHistoryParser::readRaw() {
    int len = arrayLen(history.rawValue);
    for (int i=0;i<len;i++) {
        history.rawValue[i] = 0;
    }
    switch (history.eventType) {
    case CgmHistory::HISTORY_GLUCOSE:
    case CgmHistory::HISTORY_GLUCOSE_RECOMMEND_CAL:
    case CgmHistory::HISTORY_GLUCOSE_INVALID:
        history.rawValue[0] = (float)ibs->readUnsignedByte() / 100.0;
        for (int i=1;i<9;i++) {
            history.rawValue[i] = (float)ibs->readUnsignedShort() / 100.0;
        }
        return 17;
    case CgmHistory::HISTORY_IMPENDANCE:
        history.rawValue[0] = (float)ibs->readUnsignedByte() / 1.0;
        for (int i=1;i<3;i++) {
            history.rawValue[i] = (float)ibs->readShort() / 100.0;
        }
        return 5;
    default:
        return 0;
    }
}
