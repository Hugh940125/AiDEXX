#include "blecontroller.h"
#include "../ble.h"
#include "../devcomm/devcommclass.h"
#include "../constant/globalconstants.h"
#include "../constant/bleoperation.h"
#include "../util/byteutils.h"

static list<string> snList {
};


Ble *BleController::ble = NULL;

void BleController::setBleAdapter(Ble *adapter) {
    ble = adapter;

    class Callback : public DevComm::Callback
    {
    public:
        int onHandleEvent(uint8 event) {
            LOGI("HandleEvent: event: %d", event);
            ble->handleEvent(event);
            return FUNCTION_OK;
        }
        int onHandleCommand(uint8 sourcePort, uint8 operation, uint8 parameter, const uint8 *data, uint8 length) {
            LOGI("HandleCommand: sourcePort: %d operation: %d parameter: %d success: %d",
                 sourcePort, operation, parameter, data[0]);
            ble->handleCommand(sourcePort, operation, parameter, data, length);
            return FUNCTION_OK;
        }
        int writeDevice(const uint8 *data, uint8 length) {
            ble->write(data, length);
            return FUNCTION_OK;
        }
    };
    DevComm::sCallback = new Callback();
}

void BleController::setMac(const string &mac)
{
//    this->mac = "";
//    if (!sn.empty())
//        this->mac = mac;
    this->mac = mac;
}

void BleController::setSn(const string &sn) {
//    this->sn = "";
//    list<string>::iterator iter;
//    for(iter = snList.begin(); iter != snList.end(); iter++)
//    {
//        if (sn == *iter)
//            this->sn = sn;
//    }
    this->sn = sn;
}

void BleController::startScan() {
    ble->startScan();
}

void BleController::stopScan() {
    ble->stopScan();
}

void BleController::connect() {
    ble->connect(this);
}

void BleController::disconnect() {
    ble->disconnect();
}

BleController::BleController() {
    mtu = 20;
    rxRate = 0;
    authenticated = false;
    autoSending = true;
    messageCallback = NULL;
}

void BleController::setInfo(const BleControllerInfo &info) {
    mac = info.address;
    name = info.name;
    sn = info.sn;
    rssi = info.rssi;
}

void BleController::doregister() {
    unregister();
    ble->controllers.insert(std::map<string, BleController*>::value_type(mac, this));
}

void BleController::unregister() {
    std::map<string, BleController*>::iterator iter = ble->controllers.find(mac);
    if(iter != ble->controllers.end()) {
        ble->controllers.erase(iter);
    }
}

uint16 BleController::pair() {
    if (ble->pair(this)) {
        return BleOperation::PAIR;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 BleController::unpair() {
    if (ble->send(this,
                  getCommPort(),
                  Global::OPERATION_UNPAIR,
                  Ble::DEVICE_TYPE,
                  0,
                  0)) {
        return BleOperation::UNPAIR;
    } else {
        return BleOperation::BUSY;
    }
}

void BleController::setDiscoveredCallback(DiscoverCallback discoverCallback) {
    setDiscoveredCallback([=](const BleControllerInfo &info){
        if (discoverCallback != NULL) {
            discoverCallback(info);
        }
    });
}

void BleController::setDiscoveredCallback(function<void(const BleControllerInfo &)> discoverCallback) {
    ble->discoverCallback = discoverCallback;
}

void BleController::setMessageCallback(MessageCallback messageCallback) {
    if (messageCallback != NULL) {
        setMessageCallback([=](uint16 op, bool success, const char *data, uint16 length){
            messageCallback(op, success, data, length);
        });
    } else {
        this->messageCallback = NULL;
    }
}

void BleController::setMessageCallback(const function<void(uint16, bool, const char*, uint16)> &messageCallback) {
    this->messageCallback = messageCallback;
}

void BleController::onReceive(uint16 op, bool success, const uint8 *data, uint16 length) {
    if (messageCallback != NULL) {
        messageCallback(op, success, (char*)data, length);
    }
}

bool BleController::send(uint8 port, uint8 op, uint8 param, const uint8 *data, uint16 length) {
    return ble->send(this, port, op, param, data, length);
}
