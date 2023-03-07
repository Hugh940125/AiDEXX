#include "aidexxdatetimeparser.h"
#include "../../util/inputbytestream.h"


const AidexXDatetimeEntity *AidexXDatetimeParser::getDatetime() {
    try
    {
        datetime.year = ibs->readUnsignedShort();
        datetime.month = ibs->readUnsignedByte();
        datetime.day = ibs->readUnsignedByte();
        datetime.hour = ibs->readUnsignedByte();
        datetime.minute = ibs->readUnsignedByte();
        datetime.second = ibs->readUnsignedByte();
        datetime.timeZone = ibs->readUnsignedByte();
        datetime.dstOffset = ibs->readUnsignedByte();
        return &datetime;
    }
    catch (...)
    {
        ibs->clear();
        LOGE("Datetime Parse Error");
        return NULL;
    }
}
