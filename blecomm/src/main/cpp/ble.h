#ifndef BLE_H
#define BLE_H

#include "controller/blecontrollerinfo.h"
#include "devcomm/CLibrary/global.h"
#include "constant/globalconstants.h"

#include <string>
#include <vector>
#include <list>
#include <map>
#include <functional>

using namespace std;

class CTimer;
class BleController;
class Ble
{
public:
    static const uint8 DEVICE_TYPE = Comm::PARAM_PAIR_DEVICE_1;

    explicit Ble();
    virtual ~Ble();

protected:
    // 需实现以下蓝牙操作
    virtual void executeStartScan() = 0;
    virtual void executeStopScan() = 0;
    virtual bool isReadyToConnect(string mac) = 0;
    virtual void executeConnect(string mac) = 0;
    virtual void executeDisconnect() = 0;
    virtual void executeWrite(const char *data, uint16 length) = 0;

    // 服务UUID
    uint16 getServiceUUID();
    
    // 特征UUID
    uint16 getCharacteristicUUID();
    // 第一次连接前的扫描超时设置
    void setDiscoverTimeoutSeconds(uint16 seconds);

    // 蓝牙状态发生变化时需调用以下函数
    void onScanRespond(string address, int32 rssi, const char *data, uint16 length);
    void onScanRespondDecoded(string address, string name, int32 rssi, const char *data, uint16 length);
    void onAdvertise(string address, int32 rssi, const char *data, uint16 length);
    void onAdvertiseDecoded(string address, string name, int32 rssi, const char *data, uint16 length);
    void onConnectSuccess();
    void onConnectFailure();
    void onDisconnected();
    void onReceiveData(const char *data, uint16 length);

private:
    friend class BleController;

    class ReceiveBuffer {
    public:
        int mMaxLength;
        int mPointer;
        int mIndex;
        vector<uint8> dataBuffer;
        vector<uint8> receiveData;

        ReceiveBuffer(int maxLength);
        bool push(const uint8 *data, uint16 length);
        void flush();
    };

    class BleCommand
    {
    public:
        BleCommand(uint8 port, uint8 op, uint8 param);
        BleCommand(uint8 port, uint8 op, uint8 param, const uint8 *data, uint16 length);
        uint8 port;
        uint8 op;
        uint8 param;
        vector<uint8> data;
    };

    enum BleState {IDLE, SCANNING, CONNECTING, CONNECTED, DISCONNECTING};

    uint32 discoverTimeoutSeconds;

    BleState state;
    bool connectWhenDiscovered;
    bool pairWhenConnected;

    BleController *controller;
    std::map<string, BleController*> controllers;

    ReceiveBuffer *buffer;
    CTimer *pSearchTimer;
    CTimer *pAckTimer;
    CTimer *pDisconTimer;
    CTimer *pOnDisconTimer;
    list<BleCommand> commandList;

    function<void(const BleControllerInfo &)> discoverCallback;

    Ble(const Ble&) = delete;
    Ble& operator=(const Ble&) = delete;
    
    void startScan();
    void stopScan();
    bool pair(BleController *controller);
    bool send(BleController *controller, uint8 port, uint8 op, uint8 param, const uint8 *data, uint16 length);

    bool isInConnection();
    bool isFoundCurrent(string address, string name, string sn);
	void connect(BleController *controller);
    void connect();
    void disconnect();
    void sendPairCommand();
    void sendBondCommand();

    void handleEvent(uint8 event);
    void handleCommand(uint8 port, uint8 op, uint8 param, const uint8 *data, uint16 length);
    void write(const uint8 *data, uint16 length);
    void continueSending();
};

#endif // BLE_H
