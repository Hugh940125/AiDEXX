#include "cgmhistoriesparser.h"
#include "../../constant/cgm/cgmconstants.h"
#include "../../util/inputbytestream.h"


bool CgmHistoriesParser::hasNext() {
    return !ibs->isEnd();
}

void CgmHistoriesParser::parse() {
    if (first) {
        history.eventIndex = ibs->readUnsignedShort();
        history.dateTime = ibs->readUnsignedInt() + CgmHistory::YEAR_2000;
        history.eventType = ibs->readUnsignedByte();
        first = false;
    } else {
        history.eventIndex += 1;
        uint8 firstByte = ibs->readByte();
        bool newType = (firstByte & 0x80) != 0;
        int addTime = (firstByte & 0x7F) * 10;
        if (addTime > 1200) {
            history.dateTime = (int64)(ibs->readUnsignedInt()) + CgmHistory::YEAR_2000;
        } else {
            history.dateTime += addTime;
        }
        if (newType) {
            history.eventType = ibs->readUnsignedByte();
        }
    }
    history.sensorIndex = mSensorIndex;

    switch (history.eventType) {
    case CgmHistory::HISTORY_GLUCOSE:
    case CgmHistory::HISTORY_GLUCOSE_RECOMMEND_CAL:
    case CgmHistory::HISTORY_HYPER:
    case CgmHistory::HISTORY_HYPO:
    case CgmHistory::HISTORY_HYPO_SETTING:
    case CgmHistory::HISTORY_HYPER_SETTING:
        history.eventValue = (float)ibs->readUnsignedByte() / 10.0;
        break;
    case CgmHistory::HISTORY_BLOOD_GLUCOSE:
    case CgmHistory::HISTORY_CALIBRATION:
        history.eventValue = (float)ibs->readUnsignedShort() / 10.0;
        break;
    default:
        history.eventValue = 0;
        break;
    }
}
