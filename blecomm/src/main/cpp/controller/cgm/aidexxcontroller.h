#ifndef AIDEXXCONTROLLER_H
#define AIDEXXCONTROLLER_H

#include "../../devcomm/CLibrary/global.h"
#include "../../devcomm/devcommclass.h"
#include "../blecontroller.h"
#include "../../parser/cgm/aidexxentities.h"

class Ble;
class AidexXController : public BleController
{
public:
    static const uint16 SERVICE_UUID = 0x181F;
    static const uint16 CHARACTERISTIC_UUID = 0x2AFF;
    static const uint16 PRIVATE_CHARACTERISTIC_UUID = 0x2AFE;
    static const uint HOST_ADDRESS_LENGTH = 6;
    static const uint SECRET_LENGTH = 16;
    static const uint KEY_LENGTH = 16;

    explicit AidexXController();
    ~AidexXController();

    // getter
    const uint8 *getSecret() const override { return sn.size() ? snSecret1 : NULL; }
    bool isPaired() const override { return accessKey.size(); }

    // setter
    void setSn(const string &sn) override;

    bool startEncryption(const uint8 *key);

    uint16 pair();

    // CGM命令
    uint16 getDeviceInfo();
    uint16 getBroadcastData();

    uint16 newSensor(AidexXDatetimeEntity &datetime);
    uint16 getStartTime();
    uint16 getHistoryRange();
    uint16 getHistories(uint16 timeOffset);
    uint16 getRawHistories(uint16 timeOffset);
    uint16 calibration(uint16 glucose, uint16 timeOffset);
    uint16 getCalibrationRange();
    uint16 getCalibration(uint16 index);

    uint16 getDefaultParamData();
    uint16 setDefaultParamData(float32 value[]);
    uint16 getSensorCheck();

    uint16 reset();
    uint16 shelfMode();
    uint16 DeleteBond();
    uint16 ClearStorage();
    uint16 setGcBiasTrimming(uint16 value);
    uint16 setGcImeasTrimming(int16 zero, uint16 scale);

protected:
    uint8 snSecret1[SECRET_LENGTH] = {0};
    uint8 snSecret2[SECRET_LENGTH] = {0};

    uint16 getServiceUUID() const override { return SERVICE_UUID; }
    uint16 getCharacteristicUUID() const override { return CHARACTERISTIC_UUID; }
    uint16 getPrivateCharacteristicUUID() const override { return isPaired() ? 0 : PRIVATE_CHARACTERISTIC_UUID; };
    uint8 getPacketLength() const override { return 0; }
    uint getCommPort() const override { return 0xFF; }
    uint getHostAddressLength() const override {return HOST_ADDRESS_LENGTH;}
    uint getIdLength() const override {return 0;}
    uint getKeyLength() const override {return KEY_LENGTH;}
    uint getDevCommType() const override {return DEV_COMM_TYPE_1; }

    bool isEncryptionEnabled() const override { return false; }
    bool isBufferEnabled() const override { return false; }
    bool isAuthenticated() const { return authenticated; }
	bool isFrameEnabled() const { return frameEnable; }
	bool isAcknowledgement() const { return acknowledgement; }

    void setSendTimeout(int msec);
    bool sendCommand(uint8 op, uint8 *data, uint16 length, bool instantly = false);
    bool handleCommand(uint8 port, uint8 op, uint8 param, const uint8 *data, uint16 length) override;

private:
    class LongAttribute
    {
    public:
        LongAttribute(AidexXController *controller, uint8 maxNumber, uint8 setCode, uint8 getCode, uint16 setOp, uint16 getOp);
        ~LongAttribute();
        uint16 set(uint8 number, float32 value[]);
        uint16 get();

    private:
        friend class AidexXController;
        AidexXController *controller;
        uint8 maxNumber;
        uint8 setCode;
        uint8 getCode;
        uint16 setOp;
        uint16 getOp;
    
        uint8 setNumber;
        uint8 sendIndex;
        float32 *sendValue;
        uint8 *sendBuffer;

        uint8 queryIndex;
        uint8 *queryBuffer;

        const uint8 MAX_RESP_ERROR_COUNT = 3;
        uint8 sendRespErrorCount;
        uint8 queryRespErrorCount;

        bool send(bool instantly);
        bool sendResp(const uint8 *data, uint16 length);
        bool query(bool instantly);
        bool queryResp(const uint8 *data, uint16 length);
    };

    class DefaultParam : public LongAttribute
    {
    public:
        DefaultParam(AidexXController *controller);
        uint16 set(float32 value[]);
    };

    DefaultParam *defaultParam;
};

#endif // AIDEXXCONTROLLER_H
