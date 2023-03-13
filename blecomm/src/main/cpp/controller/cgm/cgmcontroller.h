#ifndef CGMCONTROLLER_H
#define CGMCONTROLLER_H

#include "../../devcomm/CLibrary/global.h"
#include "../../constant/cgm/cgmconstants.h"
#include "../blecontroller.h"

#include <string>
#include <vector>
#include <map>
#include <functional>

using namespace std;

class Ble;
class CgmController : public BleController
{
public:
    static const uint16 SERVICE_UUID = 0xF000;
    static const uint16 CHARACTERISTIC_UUID = 0xF001;
    static const uint16 PRIVATE_CHARACTERISTIC_UUID = 0xF101;
    static const uint8 PACKET_LENGTH = 38;
    static const uint HOST_ADDRESS_LENGTH = 32;
    static const uint ID_LENGTH = 6;
    static const uint KEY_LENGTH = 16;

    explicit CgmController();

    // CGM初始设置
    void initialSettings(float32 hypo, float32 hyper);
    float32 getHypo() { return hypo; }
    float32 getHyper() { return hyper; }

    // CGM命令
    uint16 getDeviceInfo();
    uint16 getSingleHistory(uint16 index);
    uint16 getHistories(uint16 index);
    uint16 getFullHistories(uint16 index);
    uint16 newSensor(bool isNew, int64 datetime = 0);
    uint16 setDatetime(int64 datetime);
    uint16 recordBg(float32 glucose, int64 datetime);
    uint16 calibration(float32 glucose, int64 datetime);
    uint16 setHyper(float32 hyper);
    uint16 setHypo(float32 hypo);
    uint16 getDeviceCheck();
    uint16 getDefaultParamData();
    uint16 setDefaultParamData(const uint8 *data, uint16 length);
    uint16 setDefaultParamData(float32 value[]);
    uint16 setGcBiasTrimming(uint16 value);
    uint16 setGcImeasTrimming(uint8 channel, int16 zero, uint16 scale);
    
    uint16 getBroadcastData();
    
    //CGM命令
    uint16 forceUnpair();
    uint16 forceReboot();
    uint16 pairWithForceUnpair();

protected:
    uint16 getServiceUUID() const override { return SERVICE_UUID; }
    uint16 getCharacteristicUUID() const override { return CHARACTERISTIC_UUID; }
    uint16 getPrivateCharacteristicUUID() const override { return  PRIVATE_CHARACTERISTIC_UUID; };
    uint8 getPacketLength() const override { return PACKET_LENGTH; }
    uint getCommPort() const override { return CgmPort::PORT_COMM; }
    uint getHostAddressLength() const override {return HOST_ADDRESS_LENGTH;}
    uint getIdLength() const override {return ID_LENGTH;}
    uint getKeyLength() const override {return KEY_LENGTH;}

    bool isEncryptionEnabled() const override { return true; }
    bool isBufferEnabled() const override { return true; }
    bool isAuthenticated() const { return authenticated; }
	bool isFrameEnabled() const { return frameEnable; }
	bool isAcknowledgement() const { return acknowledgement; }

    void setInfo(const BleControllerInfo &info) override;

    bool handleCommand(uint8 port, uint8 op, uint8 param, const uint8 *data, uint16 length) override;
    void onReceive(uint16 op, bool success, const uint8 *data = 0, uint16 length = 0) override;

private:
    float32 hypo;
    float32 hyper;
    uint8 sensorIndex;
    uint32 startUp;
};

#endif // CGMCONTROLLER_H
