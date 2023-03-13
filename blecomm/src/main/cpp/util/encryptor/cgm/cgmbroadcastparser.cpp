#include "cgmbroadcastparser.h"
#include "../../constant/cgm/cgmconstants.h"
#include "../../util/byteutils.h"

#include <cstring>

CgmBroadcastParser::CgmBroadcastParser(const char *bytes, uint16 length) {
    if (length < MIN_BYTES_LENGTH) LOGE("Broadcast bytes too short");
    this->bytes = vector<uint8>(bytes, bytes+length);
}

const CgmBroadcastEntity *CgmBroadcastParser::getBroadcast() {
    broadcast.bat = bytes[0] + CgmHistory::BATTERY_BASE;
    broadcast.dateTime = (int64)bytes[1] * 10
            + (int64)(LittleEndianByteUtils::byteToUnsignedInt(&bytes[2]))
            + CgmHistory::YEAR_2000;

    if ((bytes[9] & 0x20) > 0) {
        broadcast.state = CgmGlucose::STATE_ERROR;
    } else if ((bytes[9] & 0x40) > 0) {
        broadcast.state = CgmGlucose::STATE_INVALID;
    } else if ((bytes[9] & 0x80) > 0) {
        broadcast.state = CgmGlucose::STATE_RECOMMEND_CAL;
    } else {
        broadcast.state = CgmGlucose::STATE_OK;
    }

    uint historyLength = CgmHistoryParser::MIN_BYTES_LENGTH;
    char historyBytes[historyLength];
    for(int i=0;i<historyLength;i++) {
        historyBytes[i] = bytes[i+2];
    }
    historyBytes[7] &= 0x1F;
    broadcast.history = *(CgmHistoryParser(historyBytes, historyLength).getHistory());

    switch(broadcast.history.eventType) {
    case CgmHistory::HISTORY_GLUCOSE:
    case CgmHistory::HISTORY_GLUCOSE_RECOMMEND_CAL:
    case CgmHistory::HISTORY_HYPO:
    case CgmHistory::HISTORY_HYPER:
    case CgmHistory::HISTORY_HYPO_SETTING:
    case CgmHistory::HISTORY_HYPER_SETTING:
    case CgmHistory::HISTORY_IMPENDANCE:
    case CgmHistory::HISTORY_PLACEHOLDER:
        if (broadcast.state == CgmGlucose::STATE_OK || broadcast.state == CgmGlucose::STATE_RECOMMEND_CAL) {
            broadcast.glucose = broadcast.history.eventValue;
        } else {
            broadcast.glucose = -1;
        }
        break;
    default:
        broadcast.glucose = -1;
        break;
    }

    switch(broadcast.history.eventType) {
    case CgmHistory::HISTORY_HYPO:
    case CgmHistory::HISTORY_HYPER:
    case CgmHistory::HISTORY_HYPO_SETTING:
    case CgmHistory::HISTORY_HYPER_SETTING:
        broadcast.history.eventValue = (float) (bytes[11] & 255) / 10.0;
        break;
    }
    
    broadcast.primary = bytes[18];

    return &broadcast;
}
