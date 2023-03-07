#include "cgmdeviceparser.h"
#include "../../constant/cgm/cgmconstants.h"
#include "../../util/inputbytestream.h"


const CgmDeviceEntity *CgmDeviceParser::getDevice() {
    try {
        parse();
    } catch (...) {
        LOGE("Device Parse Error");
    }
    return &device;
}

void CgmDeviceParser::parse() {
    device.sn = ibs->readAddress(6);
    device.endian = ibs->readUnsignedByte();
    device.deviceType = ibs->readUnsignedByte();
    device.model = ibs->readUnsignedInt();

    uint8 edition1 = ibs->readUnsignedByte();
    uint8 edition2 = ibs->readUnsignedByte();
    uint8 edition3 = ibs->readUnsignedByte();
    uint8 edition4 = ibs->readUnsignedByte();
    char* edition = (char*) malloc(16);
    sprintf(edition, "%d.%d.%d.%d", edition4, edition3, edition2, edition1);
    device.edition = string(edition);

    device.capacity = ibs->readUnsignedInt();
    
    free(edition);
    edition = NULL;
}
