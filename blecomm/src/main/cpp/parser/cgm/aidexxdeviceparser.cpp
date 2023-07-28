#include "aidexxdeviceparser.h"
#include "../../util/inputbytestream.h"
#include "../../constant/cgm/aidexxconstants.h"

const AidexXDeviceEntity *AidexXDeviceParser::getDevice() {
    try
    {
        device.hardWare = ibs->readUnsignedByte();
        device.type = ibs->readUnsignedByte();
        uint8 edition1 = ibs->readUnsignedByte();
        uint8 edition2 = ibs->readUnsignedByte();
        uint8 edition3 = ibs->readUnsignedByte();
        uint8 edition4 = ibs->readUnsignedByte();
        device.editionMajor = edition1;
        device.editionMinor = edition2;
        device.editionRevision = edition3;
        device.editionBuild = edition4;
        char edition[16];
        snprintf(edition, sizeof(edition), "%d.%d.%d.%d", edition1, edition2, edition3, edition4);
        device.edition = string(edition);
        
        return &device;
    }
    catch (...)
    {
        ibs->clear();
        LOGE("DeviceInfo Parse Error");
        return NULL;
    }
}
