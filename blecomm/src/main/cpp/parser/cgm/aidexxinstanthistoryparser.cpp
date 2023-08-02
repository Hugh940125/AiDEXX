#include "aidexxinstanthistoryparser.h"

#ifdef __APPLE__
#include "inputbytestream.h"
#include "aidexxconstants.h"
#else
#include "../../util/inputbytestream.h"
#include "../../constant/cgm/aidexxconstants.h"
#endif

const AidexXInstantHistoryEntity *AidexXInstantHistoryParser::getInstantHistory() {
    try
    {
        instantHistory.abstract.status = ibs->readUnsignedByte();
        instantHistory.abstract.calTemp = ibs->readUnsignedByte();
        instantHistory.abstract.trend = ibs->readUnsignedByte();
        instantHistory.abstract.timeOffset = ibs->readUnsignedShort();

        uint16 sg_and_state = ibs->readUnsignedShort();
        instantHistory.history.timeOffset = instantHistory.abstract.timeOffset;
        instantHistory.history.glucose = sg_and_state & 0x3FF;
        instantHistory.history.status = (sg_and_state & 0xC00) >> 10;
        instantHistory.history.quality = 0;
        instantHistory.history.isValid = (sg_and_state != 0xFFFF);
        
        instantHistory.raw.timeOffset = instantHistory.abstract.timeOffset;
        instantHistory.raw.i1 = (float32)(ibs->readShort()) / 100.0;
        instantHistory.raw.i2 = (float32)(ibs->readShort()) / 100.0;
        uint16 vc = ibs->readUnsignedByte();
        instantHistory.raw.vc = (float32)(vc) / 100.0;
        instantHistory.raw.isValid = (vc != 0xFFFF);
        
        instantHistory.abstract.calIndex = ibs->readShort();
        
        return &instantHistory;
    }
    catch (...)
    {
        ibs->clear();
        LOGE("InstantHistory Parse Error");
        return NULL;
    }
}
