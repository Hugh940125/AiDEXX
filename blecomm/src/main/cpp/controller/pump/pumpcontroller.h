#ifndef PUMPCONTROLLER_H
#define PUMPCONTROLLER_H

#include "../../devcomm/CLibrary/global.h"
#include "../../constant/pump/pumpconstants.h"
#include "../../constant/pump/pumpoperation.h"
#include "../blecontroller.h"

#include <string>
#include <vector>
#include <map>
#include <functional>

using namespace std;

class Ble;
class PumpController : public BleController
{
public:
    static const uint16 SERVICE_UUID = 0xF000;
    static const uint16 CHARACTERISTIC_UUID = 0xF001;
    static const uint8 PACKET_LENGTH = 16;
    static const uint HOST_ADDRESS_LENGTH = 6;

    static const uint32 VALUE_SCALE = 1000;
    static const uint32 STEP_MIN = 4;
    static const uint32 STEP_UNIT = 8;
    static const uint32 INSULIN_MIN = 25;
    static const uint32 INSULIN_UNIT = 50;

    static const uint32 BASAL_PROFILE_COUNT = 48;
    static const uint32 BOLUS_EXTENDED_RATE_MAX = 11200;

    static const uint32 BOLUS_STEP_DEFAULT = 100;

    explicit PumpController();

    // PUMP命令
    uint16 setAddress();
    uint16 clearAddress();
    uint16 getDeviceInfo();
    uint16 getHistory(uint16 index);
    uint16 setEventConfirmed(uint16 eventIndex, uint32 event, uint8 value);
    uint16 setDatetime(const string &dateTime);
    uint16 getMode();
    uint16 setMode(uint32 mode);
    uint16 getDeliveryBusy();
    uint16 setBasalProfile(float32 basal[]);
    uint16 getBasalProfile();
    uint16 setBolusProfile(float32 amountTotal, float32 bolusRatio, float32 amountExtended, uint32 intervalExtended);
    uint16 getBolusProfile();
    uint16 setTemporaryProfile(float32 tempBasal, uint32 interval);
    uint16 setTemporaryPercentProfile(uint32 tempBasalPercent, uint32 interval);
    uint16 getSetting();
    uint16 setSetting(float32 value[]);
    uint16 setRewinding(float32 amount);
    uint16 setPriming(float32 amount);
    uint16 setBolusRatio(uint16 multiple, uint16 division);
    uint16 setCgmSn(const string &sn);
    uint16 setAutoMode(bool isOn);
    uint16 setGlucoseTarget(float32 targetUpper, float32 targetLower);
    uint16 setIsf(float32 isf);
    uint16 getOcclusion();

protected:
    uint16 getServiceUUID() const { return SERVICE_UUID; }
    uint16 getCharacteristicUUID() const { return CHARACTERISTIC_UUID; }
    uint16 getPrivateCharacteristicUUID() const { return  0; };
    uint8 getPacketLength() const { return PACKET_LENGTH; }
    uint getCommPort() const { return PumpPort::PORT_COMM; }
    uint getHostAddressLength() const {return HOST_ADDRESS_LENGTH;}
    uint getIdLength() const {return 0;}
    uint getKeyLength() const {return 0;}

    bool isEncryptionEnabled() const { return false; }
    bool isBufferEnabled() const { return false; }
    bool isAuthenticated() const { return authenticated; }
    bool isFrameEnabled() const { return frameEnable; }
    bool isAcknowledgement() const { return acknowledgement; }

    uint32 getBolusStep() const { return bolusStep; }

    void setBolusStep(uint32 bolusStep) { this->bolusStep = bolusStep; }

    bool handleCommand(uint8 port, uint8 op, uint8 param, const uint8 *data, uint16 length);
    void onReceive(uint16 op, bool success, const uint8 *data = 0, uint16 length = 0);

private:
    uint16 pariOp = PumpOperation::PAIR;
    uint32 bolusStep = BOLUS_STEP_DEFAULT;

    static uint16 basalToSteps(float32 basal);
};

#endif // PUMPCONTROLLER_H
