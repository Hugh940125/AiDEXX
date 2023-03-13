#include "aidexxdefaultparamsparser.h"
#include "../../util/inputbytestream.h"


bool AidexXDefaultParamsParser::hasNext() {
    return !ibs->isEnd();
}

float32 AidexXDefaultParamsParser::getParam() {
    try
    {
        if (++index == 1)
            return (float32)(ibs->readShort());
        else
            return (float32)(ibs->readShort()) / 100.0;
    }
    catch (...)
    {
        ibs->clear();
        LOGE("Param Parse Error");
        return 0.0;
    }
}
