#include "cgmhistoriesparser.h"
#include "../../constant/cgm/cgmconstants.h"
#include "../../util/inputbytestream.h"
#include "../../util/lib_aes.h"


bool CgmHistoriesParser::hasNext() {
    return !ibs->isEnd();
}

const CgmHistoryPlusEntity *CgmHistoriesParser::getHistoryPlus() {
    getHistory();
    historyPlus.history = history;
    historyPlus.startUp = mStartUp;
    
    return &historyPlus;
}

const CgmHistoryPlusEntity *CgmHistoriesParser::getFullHistoryPlus() {
    getFullHistory();
    historyPlus.history = history;
    historyPlus.startUp = mStartUp;
    
    return &historyPlus;
}

const CgmEncryptedHistoryEntity *CgmHistoriesParser::getEncryptedHistory() {
    getFullHistory();
    uint8 *bytes = encryptedHistory.bytes;
    bytes[0] = (history.sensorIndex >> 8) & 0xFF;
    bytes[1] = history.sensorIndex & 0xFF;
    bytes[2] = (history.eventIndex >> 8) & 0xFF;
    bytes[3] = history.eventIndex & 0xFF;
    bytes[4] = (history.eventType >> 8) & 0xFF;
    bytes[5] = history.eventType & 0xFF;

    int16 temp;
    temp = history.eventValue * 10 + (history.eventValue > 0 ? 0.5 : -0.5);
    bytes[6] = (temp >> 8) & 0xFF;
    bytes[7] = temp & 0xFF;
    for (int i = 0; i < 9; i++) {
        temp = history.rawValue[i] * 100 + (history.rawValue[i] > 0 ? 0.5 : -0.5);
        bytes[8+i*2] = (temp >> 8) & 0xFF;
        bytes[9+i*2] = temp & 0xFF;
    }
    LIB_AES_CFB_encrypt(bytes, sizeof(encryptedHistory.bytes));
    encryptedHistory.dateTime = history.dateTime;
    encryptedHistory.eventIndex = history.eventIndex;
    encryptedHistory.sensorIndex = history.sensorIndex;
    
    return &encryptedHistory;
}

const CgmEncryptedHistoryPlusEntity *CgmHistoriesParser::getEncryptedHistoryPlus() {
    getEncryptedHistory();
    encryptedHistoryPlus.encryptedHistory = encryptedHistory;
    encryptedHistoryPlus.startUp = mStartUp;
    
    return &encryptedHistoryPlus;
}

void CgmHistoriesParser::parse() {
    if (first) {
        uint16 index = ibs->readUnsignedShort();
        bool isSensorIndex = index & 0x8000;
        if (isSensorIndex) {
            mSensorIndex = index & 0x00FF;
            mStartUp = ibs->readUnsignedInt() + CgmHistory::YEAR_2000;
            history.eventIndex = ibs->readUnsignedShort();
        } else {
            mSensorIndex = -1;
            history.eventIndex = index & 0x7FFF;
        }
        
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
