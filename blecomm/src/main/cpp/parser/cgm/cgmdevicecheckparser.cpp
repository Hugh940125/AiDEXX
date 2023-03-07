#include "cgmdevicecheckparser.h"

const CgmDeviceCheckEntity *CgmDeviceCheckParser::getCgmDeviceCheck()
{
    try {
        parse();
    } catch (...) {
        LOGE("DeviceCheck Parse Error");
    }
    return &deviceCheck;
}

void CgmDeviceCheckParser::parse() {
    deviceCheck.save = ibs->readUnsignedInt() > 0;
    deviceCheck.load = ibs->readUnsignedInt() > 0;
    deviceCheck.vc = ibs->readInt();
    deviceCheck.i1 = ibs->readInt();
    deviceCheck.i2 = ibs->readInt();
    deviceCheck.real = ibs->readShort();
    deviceCheck.imag = ibs->readShort();
}
