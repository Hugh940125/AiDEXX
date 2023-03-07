#include "pumphistoryparser.h"

#include "../../constant/pump/pumpconstants.h"
#include "../../util/inputbytestream.h"
#include "../../util/array.h"

const PumpHistoryEntity *PumpHistoryParser::getHistory() {
    try {
        parse();
    } catch (...) {
        LOGE("History Parse Error");
    }
    return &history;
}

void PumpHistoryParser::parse() {
    uint16 year = ibs->readUnsignedByte() + PumpHistory::YEAR_2000;
    uint8 mon = ibs->readUnsignedByte();
    uint8 day = ibs->readUnsignedByte();
    uint8 hour = ibs->readUnsignedByte();
    uint8 min = ibs->readUnsignedByte();
    uint8 sec = ibs->readUnsignedByte();
    char* dateTime = (char*) malloc(25);
    sprintf(dateTime, "%.4d-%.2d-%.2d %.2d:%.2d:%.2d", year, mon, day, hour, min, sec);
    history.dateTime = string(dateTime);

    uint8 remainingCapacity = ibs->readUnsignedByte();
    uint8 remainingInsulin = ibs->readUnsignedByte();
    history.remainingCapacity = remainingCapacity & 0x7F;
    history.remainingInsulin = ((uint16)(remainingCapacity & 0x80) << 1) | (uint16)remainingInsulin;

    uint16 basal = ibs->readUnsignedShort();
    history.autoMode = basal & 0x8000;
    history.basal = basal & 0x7FFF;
    uint16 bolus = ibs->readUnsignedShort();
    if (bolus & 0x8000) {
        history.bolus = 10 * (uint32)(bolus & 0x7FFF);
    } else {
        history.bolus = bolus;
    }
    history.basalUnitPerHour = (float32)history.basal * 0.00625;
    history.bolusUnitPerHour = (float32)history.bolus * 0.00625;

    history.eventIndex = ibs->readUnsignedShort() & 0x7FFF;
    history.eventPort = ibs->readUnsignedByte();
    history.eventType = ibs->readUnsignedByte();
    history.eventLevel = ibs->readUnsignedByte();
    history.event = history.eventLevel | (history.eventType << 8) | (history.eventPort << 16);
    history.eventValue = ibs->readUnsignedByte();
    
    free(dateTime);
    dateTime = NULL;
}
