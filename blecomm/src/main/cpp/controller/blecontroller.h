#ifndef BLECONTROLLER_H
#define BLECONTROLLER_H

#include "blecontrollerinfo.h"
#include "../devcomm/CLibrary/global.h"

#include <string>
#include <vector>
#include <map>
#include <functional>

using namespace std;

class Ble;
class BleController
{
public:
    // 设置CgmBle具体类
    static void setBleAdapter(Ble *adapter);

    // 开始、停止扫描
    static void startScan();
    static void stopScan();

    // 设置发现蓝牙发射器回调
    typedef void (*DiscoverCallback)(const BleControllerInfo &);
    static void setDiscoveredCallback(DiscoverCallback discoverCallback);
    static void setDiscoveredCallback(function<void(const BleControllerInfo &)> discoverCallback);

    explicit BleController();
    virtual ~BleController() = default;

    virtual uint16 getServiceUUID() const = 0;
    virtual uint16 getCharacteristicUUID() const = 0;
    virtual uint8 getPacketLength() const = 0;
    virtual uint getCommPort() const = 0;
    virtual uint getHostAddressLength() const = 0;
    virtual uint getIdLength() const = 0;
    virtual uint getKeyLength() const = 0;
    virtual uint getDevCommType() const { return 0; }

    // getter
    const string &getMac() const { return mac; }
    const string &getName() const { return name; }
    const string &getSn() const { return sn; }
    const char *getHostAddress() const { return hostAddress.size() ? (char*)hostAddress.data() : NULL; }
    const char *getId() const { return accessId.size() ? (char*)accessId.data() : NULL; }
    const char *getKey() const { return accessKey.size() ? (char*)accessKey.data() : NULL; }
    bool isPaired() const { return accessId.size() && accessKey.size(); }
    const vector<uint8> &getPairParameters() const { return pairParameter; }
    uint16 getMtu() const { return this->mtu; }
    uint8 getRxRate() const { return rxRate; }
    int32 getRssi() const { return rssi; }
    bool isAuthenticated() const { return authenticated; }
	bool isFrameEnabled() const { return frameEnable; }
	bool isAcknowledgement() const { return acknowledgement; }
    bool isAutoDisconnect() const { return autoDisconnect; }
    bool isAutoSending() const { return autoSending; }

    // setter
    void setMac(const string &mac);
    void setName(const string &name) { this->name = name; }
    virtual void setSn(const string &sn);
    void setHostAddress(const char *hostAddress) { hostAddress == NULL ? this->hostAddress.clear() : (void)(this->hostAddress = vector<uint8>(hostAddress, hostAddress+getHostAddressLength())); }
    void setId(const char *accessId) { accessId == NULL ? this->accessId.clear() : (void)(this->accessId = vector<uint8>(accessId, accessId+getIdLength())); }
    void setKey(const char *accessKey) { accessKey == NULL ? this->accessKey.clear() : (void)(this->accessKey = vector<uint8>(accessKey, accessKey+getKeyLength())); }
    void setMtu(uint16 mtu) { this->mtu = mtu; }
    void setRxRate(uint8 rxRate) { this->rxRate = rxRate; }
    void setRssi(int32 rssi) { this->rssi = rssi; }
    virtual void setInfo(const BleControllerInfo &info);
    void setAutoDisconnect(bool autoDisconnect) { this->autoDisconnect = autoDisconnect; }

    // 设置消息回调
    typedef void (*MessageCallback)(uint16, bool, const char*, uint16);
    void setMessageCallback(MessageCallback messageCallback);
    void setMessageCallback(const function<void(uint16, bool, const char*, uint16)> &messageCallback);

    // 注册、反注册广播包消息回调
    void doregister();
    void unregister();

	// 蓝牙操作
	void connect();
    void disconnect();
	
	//CGM命令
    uint16 pair();
    uint16 unpair();

protected:
    friend class Ble;

    static Ble *ble;

    string mac;
    string name;
    string sn;
    vector<uint8> hostAddress;
    vector<uint8> accessId;
    vector<uint8> accessKey;
    vector<uint8> pairParameter;

    uint16 mtu;
    uint8 rxRate;
    int32 rssi;
	
    bool authenticated;
	bool frameEnable;
	bool acknowledgement;
    bool autoDisconnect;
    bool autoSending;

    function<void(uint16, bool, const char*, uint16)> messageCallback;

    virtual bool isEncryptionEnabled() const = 0;
    virtual bool isBufferEnabled() const = 0;
    virtual bool handleCommand(uint8 port, uint8 op, uint8 param, const uint8 *data, uint16 length) { return false; }
    virtual void onReceive(uint16 op, bool success, const uint8 *data = 0, uint16 length = 0);

    bool send(uint8 port, uint8 op, uint8 param, const uint8 *data, uint16 length);
};

#endif // BLECONTROLLER_H
