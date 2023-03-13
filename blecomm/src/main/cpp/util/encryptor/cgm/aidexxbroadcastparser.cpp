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
        while(!ibs->isEnd()) {
            try
            {
                int timeOffset = (int)broadcast.timeOffset - (int)broadcast.historyCount * AidexxHistory::TIME_INTERVAL;
                if (timeOffset <= 0)
                    break;
                broadcast.history[broadcast.historyCount].timeOffset = timeOffset;
                uint16 sg_and_state = ibs->readUnsignedShort();
                broadcast.history[broadcast.historyCount].glucose = sg_and_state & 0x3FF;
                broadcast.history[broadcast.historyCount].status = (sg_and_state & 0xC00) >> 10;
                broadcast.history[broadcast.historyCount].quality = ibs->readByte();
                broadcast.history[broadcast.historyCount].isValid = (sg_and_state != 0xFFFF);
                if (++broadcast.historyCount >= sizeof(broadcast.history) / sizeof(broadcast.history[0]))
                    break;
            }
            catch (...)
            {
                ibs->clear();
                LOGE("Broadcast Parse Error");
                break;
            }
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
