#include "aidexxbroadcastparser.h"
#include "../../util/inputbytestream.h"
#include "../../constant/cgm/aidexxconstants.h"

const AidexXBroadcastEntity *AidexXBroadcastParser::getBroadcast() {
    try
    {
        broadcast.historyCount = 0;
        broadcast.abstract.timeOffset = ibs->readUnsignedShort();
        broadcast.abstract.status = ibs->readUnsignedByte();
        broadcast.abstract.calTemp = ibs->readUnsignedByte();
        broadcast.abstract.trend = ibs->readByte();
        
        int count = 0;
        for (int i = 0; i < sizeof(broadcast.history) / sizeof(broadcast.history[0]); i++) {
            int timeOffset = (int)broadcast.abstract.timeOffset - i * AidexxHistory::TIME_INTERVAL;
            uint16 sg_and_state = ibs->readUnsignedShort();
            uint8 quality = ibs->readUnsignedByte();
            
            if (sg_and_state != 0xFFFF) {
                broadcast.historyCount++;
                broadcast.history[i].timeOffset = timeOffset;
                broadcast.history[i].glucose = sg_and_state & 0x3FF;
                broadcast.history[i].status = (sg_and_state & 0xC00) >> 10;
                broadcast.history[i].quality = quality;
                broadcast.history[i].isValid = (sg_and_state != 0xFFFF);
            }
            
            count++;
            if (i == 0 && ibs->balance() == sizeof(broadcast.abstract.calIndex)) {
                break;;
            }
        }
        
        broadcast.abstract.calIndex = ibs->readUnsignedShort();

        return &broadcast;
    }
    catch (...)
    {
        ibs->clear();
        LOGE("Broadcast Parse Error");
        return NULL;
    }
}
