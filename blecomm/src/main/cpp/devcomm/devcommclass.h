#ifndef DEVCOMMCLASS_H
#define DEVCOMMCLASS_H

#ifdef __cplusplus

#include "../devcomm/CLibrary/global.h"

#define ENABLE_CRC16_CCITT  1
#define ENABLE_ENCRYPTION   1

#define DEV_COMM_TYPE_0         0   //AiDEX
#define DEV_COMM_TYPE_1         1   //AiDEX X
#define DEV_COMM_TYPE_COUNT     2

class AesEncryptor;
class DevComm
{
public:
    class Callback {
    public:
        virtual int onHandleEvent(uint8 event) = 0;
        virtual int onHandleCommand(uint8 sourcePort, uint8 operation, uint8 parameter, const uint8 *data, uint8 length) = 0;
        virtual int writeDevice(const uint8 *data, uint8 length) = 0;
    };

    static void select(int instance);
    static DevComm *getInstance();
    static Callback *sCallback;

    static DevComm *instance[DEV_COMM_TYPE_COUNT];
    static uint instanceNum;

    virtual int setPacketLength(uint8 packetLength) = 0;
    virtual int send(int targetPort, int mode, int operation, int parameter, uint8 *data, uint16 length) = 0;
    virtual int receive(const uint8 *data, uint16 length) = 0;
    virtual int receive(uint16 uuid, const uint8 *data, uint16 length) = 0;
    virtual AesEncryptor *getEncryptor() { return NULL; }
    virtual void turnOffEncryption() = 0;
    virtual void readyForEncryption(const uint8 *data, uint16 length) = 0;
    virtual void updateEncryption(const uint8 *data, uint16 length) = 0;
    virtual int setFrameOn(bool on) = 0;
    virtual bool isBusy() = 0;

    void setSendTimeout(int msec) { sendTimeout = msec; }
    void setRetryCount(int count) { retryCount = count; }

protected:
    int sendTimeout;
    int retryCount;

    DevComm() = default;
    DevComm(const DevComm&) = default;
    virtual DevComm& operator=(const DevComm&) = default;
    virtual ~DevComm() = default;
};


class DevComm1 : public DevComm
{
public:
    int setPacketLength(uint8 packetLength) override;
    int send(int targetPort, int mode, int operation, int parameter, uint8 *data, uint16 length) override;
    int receive(const uint8 *data, uint16 length) override;
    int receive(uint16 uuid, const uint8 *data, uint16 length) override;
    void turnOffEncryption() override;
    void readyForEncryption(const uint8 *data, uint16 length) override;
    void updateEncryption(const uint8 *data, uint16 length) override;
    int setFrameOn(bool on) override;
    bool isBusy() override;
private:
    friend class DevComm;
    DevComm1();
    DevComm1(const DevComm1&);
    DevComm1& operator=(const DevComm1&);
    ~DevComm1();
};


class CTimer;
class DevComm2 : public DevComm
{
public:
    int setPacketLength(uint8 packetLength) override;
    int send(int targetPort, int mode, int operation, int parameter, uint8 *data, uint16 length) override;
    int receive(const uint8 *data, uint16 length) override;
    int receive(uint16 uuid, const uint8 *data, uint16 length) override;
    AesEncryptor *getEncryptor() override { return encryptor; }
    void turnOffEncryption() override;
    void readyForEncryption(const uint8 *data, uint16 length) override;
    void updateEncryption(const uint8 *data, uint16 length) override;
    int setFrameOn(bool on) override;
    bool isBusy() override;
private:
    uint8 buffer[512] = {0};
    uint8 currentOp = 0xFF;
    CTimer *retryTimer;
    AesEncryptor *encryptor;

    friend class DevComm;
    DevComm2();
    DevComm2(const DevComm2&);
    DevComm2& operator=(const DevComm2&);
    ~DevComm2();
};

#endif

#endif // DEVCOMMCLASS_H
