#include "aidexxbroadcastparser.h"
#include "../../util/inputbytestream.h"
#include "../../constant/cgm/aidexxconstants.h"

const AidexXBroadcastEntity *AidexXBroadcastParser::getBroadcast() {
    try
    {
        broadcast.historyCount = 0;
        broadcast.timeOffset = ibs->readUnsignedShort();
        broadcast.status = ibs->readUnsignedByte();
        broadcast.calTemp = ibs->readUnsignedByte();
        broadcast.trend = ibs->readByte();
        
        for (int i = 0; i < sizeof(broadcast.history) / sizeof(broadcast.history[0]); i++) {
            int timeOffset = (int)broadcast.timeOffset - i * AidexxHistory::TIME_INTERVAL;
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
        }
        
        broadcast.calIndex = ibs->readUnsignedShort();

        for (int i = 0; i < sizeof(broadcast.reserved) / sizeof(broadcast.reserved[0]); i++) {
            broadcast.reserved[i] = ibs->readUnsignedByte();
        }

        return &broadcast;
    }
    catch (...)
    {
        ibs->clear();
        LOGE("Broadcast Parse Error");
        return NULL;
    }
}
