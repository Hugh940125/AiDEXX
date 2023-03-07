#ifndef BGMCONTROLLER_H
#define BGMCONTROLLER_H

#include "../../devcomm/CLibrary/global.h"
#include "../../constant/bgm/bgmconstants.h"
#include "../blecontroller.h"

#include <string>
#include <vector>
#include <map>
#include <functional>

using namespace std;

class Ble;
class BgmController : public BleController
{
public:
    static const uint16 SERVICE_UUID = 0xFFE0;
    static const uint16 CHARACTERISTIC_UUID = 0xFFE1;
    static const uint8 PACKET_LENGTH = 20;

    explicit BgmController();

    // BGMeter命令
    uint16 getDeviceInfo();
    uint16 getHistory(uint16 index);

protected:
    uint16 getServiceUUID() const { return SERVICE_UUID; }
    uint16 getCharacteristicUUID() const { return CHARACTERISTIC_UUID; }
    uint8 getPacketLength() const { return PACKET_LENGTH; }
    uint getCommPort() const { return BgmPort::PORT_COMM; }
    uint getHostAddressLength() const {return 0;}
    uint getIdLength() const {return 0;}
    uint getKeyLength() const {return 0;}
    
    bool isEncryptionEnabled() const { return false; }
    bool isBufferEnabled() const { return false; }
    bool isAuthenticated() const { return authenticated; }
    bool isFrameEnabled() const { return frameEnable; }
    bool isAcknowledgement() const { return acknowledgement; }
    
    bool handleCommand(uint8 port, uint8 op, uint8 param, const uint8 *data, uint16 length);
    void onReceive(uint16 op, bool success, const uint8 *data = 0, uint16 length = 0);
};

#endif // BGMCONTROLLER_H
