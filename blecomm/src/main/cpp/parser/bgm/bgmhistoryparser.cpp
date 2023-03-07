#include "bgmhistoryparser.h"

#include "../../constant/bgm/bgmconstants.h"
#include "../../util/inputbytestream.h"
#include "../../util/array.h"

const BgmHistoryEntity *BgmHistoryParser::getHistory() {
    try {
        parse();
    } catch (...) {
        LOGE("History Parse Error");
    }
    return &history;
}


void BgmHistoryParser::parse() {
    uint16 year = ibs->readUnsignedByte() + BgmHistory::YEAR_2000;
    uint8 mon = ibs->readUnsignedByte();
    uint8 day = ibs->readUnsignedByte();
    uint8 hour = ibs->readUnsignedByte();
    uint8 min = ibs->readUnsignedByte();
    uint8 sec = ibs->readUnsignedByte();
    char* dateTime = (char*) malloc(25);
    sprintf(dateTime, "%.4d-%.2d-%.2d %.2d:%.2d:%.2d", year, mon, day, hour, min, sec);
    history.dateTime = string(dateTime);

    history.temperature = ibs->readUnsignedByte();
    history.flag = ibs->readUnsignedByte();
    history.bgValue = ibs->readUnsignedShort();
    history.reserved = ibs->readUnsignedShort();

    history.eventIndex = ibs->readUnsignedShort();
    history.eventPort = ibs->readUnsignedByte();
    history.eventType = ibs->readUnsignedByte();
    history.eventLevel = ibs->readUnsignedByte();
    history.eventValue = ibs->readUnsignedByte();
    
    history.hypo = (history.flag >> 0) & 0x01;
    history.hyper = (history.flag >> 1) & 0x01;
    history.ketone = (history.flag >> 2) & 0x01;
    history.preMeal = (history.flag >> 3) & 0x01;
    history.postMeal = (history.flag >> 4) & 0x01;
    history.invalid = (history.flag >> 5) & 0x01;
    history.controlSolution = (history.flag >> 6) & 0x01;
    
    free(dateTime);
    dateTime = NULL;
}
