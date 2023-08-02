#include "ble.h"
#include "controller/blecontroller.h"
#include "controller/cgm/aidexxcontroller.h"
#include "devcomm/devcommclass.h"
#include "devcomm/CLibrary/lib_checksum.h"
#include "constant/globalconstants.h"
#include "constant/bleoperation.h"
#include "util/byteutils.h"
#include "util/ctimer.h"

#include <cstring>

#define ATT_MTU_SIZE    20

Ble::Ble()
{
    discoverTimeoutSeconds = 10;

    state = IDLE;
    connectWhenDiscovered = false;
    pairWhenConnected = false;

    buffer = new ReceiveBuffer(76);
    pSearchTimer = new CTimer();
    pAckTimer = new CTimer();
    pDisconTimer = new CTimer();
    pOnDisconTimer = new CTimer();
}

Ble::~Ble()
{
    delete buffer;
    delete pSearchTimer;
    delete pAckTimer;
    delete pDisconTimer;
    delete pOnDisconTimer;
}

uint16 Ble::getServiceUUID() {
    if (controller == NULL) return 0;
    return controller->getServiceUUID();
}

uint16 Ble::getCharacteristicUUID() {
    if (controller == NULL) return 0;
    return controller->getCharacteristicUUID();
}

uint16 Ble::getPrivateCharacteristicUUID() {
    if (controller == NULL) return 0;
    return controller->getPrivateCharacteristicUUID();
}

void Ble::setDiscoverTimeoutSeconds(uint16 seconds) {
    discoverTimeoutSeconds = seconds;
}

void Ble::onScanRespond(string address, int32 rssi, const char *data, uint16 length) {
    string name;
    string sn;
    uint8 mfrLength = 0;
    char mfrData[40] = {0};
    vector<uint8> params;

    for (int i = 0; i < length;) {
        int len = data[i++];
        if (len == 0 || i + len > length) {
            break;
        }
        uint8 flag = data[i++];
        if (flag == 0x09) {
            name = ByteUtils::bytesToUtf8String(data + i, len-1);
        } else if (flag == 0xFF) {
            mfrLength = len-1-2;
            ByteUtils::copy(mfrData, (const char*)data+i+2, len-1-2);
        }
        i += len-1;
    }
    
    if (name.empty()) {
        return;
    }
    
    //"GoChek", "Insight", "Exactive" LocalName有SN，ScanResponse无SN
    //"Equil" LocalName、ScanResponse都有SN
    //"AiDEX X" LocalName有SN，ScanResponse无SN
    //"AiDEX" LocalName无SN，ScanResponse有SN
    list<string> snInNameList {"GoChek", "Insight", "Exactive", "Equil", "AiDEX X"};
    string pump = "Equil";
    string aidex = "AiDEX";
    string aidexX = "AiDEX X";
    
    dev_type type = DEV_TYPE_UNKNOWN;
    bool found = false;
    for (list<string>::iterator it = snInNameList.begin(); it != snInNameList.end(); ++it) {
        string::size_type idx = name.find(it->data());
        if (idx != string::npos) {
            found = true;
            break;
        }
    }
    
    if (found) {
        string delimiter = "-";
        string tmp = name;
        string::size_type pos = tmp.find(delimiter);
        if (pos != string::npos) {
            tmp.erase(0, pos + delimiter.length());
            sn = ByteUtils::trim(tmp);
            name.erase(pos, tmp.length() + delimiter.length());
            name = ByteUtils::trim(name);
        }
        
        if (pump.compare(name) == 0) {
           type = DEV_TYPE_PUMP;
        } else {
            type = DEV_TYPE_BGM;
        }
    }
    
    if (aidex.compare(name) == 0) {
        type = DEV_TYPE_CGM;
        if (mfrLength >= 8) {
            sn = ByteUtils::bytesToSnString(mfrData, 6);
            params = vector<uint8>(mfrData+6, mfrData+mfrLength);
        }
    } else if (aidexX.compare(name) == 0) {
        type = DEV_TYPE_CGM_X;
        params = vector<uint8>(mfrData, mfrData+mfrLength);
    }
    
    if (sn.empty() || name.empty()) {
        return;
    }
    
    BleControllerInfo info;
    info.type = type;
    info.address = address;
    info.name = name;
    info.sn = sn;
    info.rssi = rssi;
    info.params = vector<uint8>(params);

    std::map<string, BleController*>::iterator iter = controllers.find(address);
    if (iter!=controllers.end()) {
        BleController *controller = iter->second;
        controller->setInfo(info);
    }

    if (connectWhenDiscovered) {
        if(isFoundCurrent(address, name, sn)) {
            pSearchTimer->Cancel();
            LOGD("executeStopScan 1");
            executeStopScan();
            executeConnect(address);
        }
        return;
    }

    LOGD("name: %s sn: %s", name.data(), sn.data());
    if (discoverCallback != NULL) {
        discoverCallback(info);
    }
}

void Ble::onScanRespondDecoded(string address, string name, int32 rssi, const char *data, uint16 length) {
    if (name.empty()) {
        return;
    }
    
    string sn;
    vector<uint8> params;
    name = ByteUtils::trim(name);
    
    //"GoChek", "Insight", "Exactive" LocalName有SN，ScanResponse无SN
    //"Equil" LocalName、ScanResponse都有SN
    //"AiDEX X" LocalName有SN，ScanResponse无SN
    //"AiDEX" LocalName无SN，ScanResponse有SN
    list<string> snInNameList {"GoChek", "Insight", "Exactive", "Equil", "AiDEX X"};
    string pump = "Equil";
    string aidex = "AiDEX";
    string aidexX = "AiDEX X";
    
    dev_type type = DEV_TYPE_UNKNOWN;
    bool found = false;
    for (list<string>::iterator it = snInNameList.begin(); it != snInNameList.end(); ++it) {
        string::size_type idx = name.find(it->data());
        if (idx != string::npos) {
            found = true;
            break;
        }
    }
    
    if (found) {
        string delimiter = "-";
        string tmp = name;
        string::size_type pos = tmp.find(delimiter);
        if (pos != string::npos) {
            tmp.erase(0, pos + delimiter.length());
            sn = ByteUtils::trim(tmp);
            name.erase(pos, tmp.length() + delimiter.length());
            name = ByteUtils::trim(name);
        }
        
        if (pump.compare(name) == 0) {
           type = DEV_TYPE_PUMP;
        } else {
            type = DEV_TYPE_BGM;
        }
    }
    
    if (aidex.compare(name) == 0) {
        type = DEV_TYPE_CGM;
        if (length >= 2+BROADCAST_LENGTH+6) {
            sn = ByteUtils::bytesToSnString(data+2+BROADCAST_LENGTH, 6);
            params = vector<uint8>(data+2+BROADCAST_LENGTH+6, data+length);
        }
    } else if (aidexX.compare(name) == 0) {
        type = DEV_TYPE_CGM_X;
        if (length > 2 + BROADCAST_LENGTH) {
            params = vector<uint8>(data+2+BROADCAST_LENGTH, data+length);
        }
    }
    
    if (sn.empty() || name.empty()) {
        return;
    }
    
    BleControllerInfo info;
    info.type = type;
    info.address = address;
    info.name = name;
    info.sn = sn;
    info.rssi = rssi;
    info.params = vector<uint8>(params);

    std::map<string, BleController*>::iterator iter = controllers.find(address);
    if (iter!=controllers.end()) {
        BleController *controller = iter->second;
        controller->setInfo(info);
    }
    
    if (connectWhenDiscovered && isFoundCurrent(address, name, sn)) {
        pSearchTimer->Cancel();
        LOGD("executeStopScan 2");
        executeStopScan();
        executeConnect(address);
        return;
    }

    LOGD("name: %s sn: %s", name.data(), sn.data());
    if (discoverCallback != NULL) {
        discoverCallback(info);
    }
}

void Ble::onAdvertise(string address, int32 rssi, const char *data, uint16 length) {
    if (state != SCANNING) return;
    
    std::map<string, BleController*>::iterator iter = controllers.find(address);
    if (iter==controllers.end()) {
        return;
    }
    
    BleController *controller = iter->second;
    controller->setRssi(rssi);
    
    uint8 mfrLength = 0;
    char mfrData[40] = {0};
    
    for (int i = 0; i < length;) {
        int len = data[i++];
        if (len == 0 || i + len > length) {
            break;
        }
        uint8 flag = data[i++];
        if (flag == 0xFF) {
            mfrLength = len-1-2;
            ByteUtils::copy(mfrData, (const char*)data+i+2, len-1-2);
            break;
        }
        i += len-1;
    }
    
    if (mfrLength != BROADCAST_LENGTH) {
        return;
    }
    
    if (typeid(*controller) == typeid(AidexXController)) {
        uint32 u32_ChecksumBase = 0;
        uint8 i;
        for (i = 0; i < BROADCAST_LENGTH - 4; i += 4) {
            u32_ChecksumBase += LittleEndianByteUtils::byteToUnsignedInt((const uint8*)mfrData+i);
        }
        u32_ChecksumBase = u32_ChecksumBase % PRIME_NUM;
        
        uint32 crc = LittleEndianByteUtils::byteToUnsignedInt((const uint8*)mfrData+BROADCAST_LENGTH-4);
        if (crc == LibChecksum_GetChecksum32Bit((const uint8 *)mfrData, BROADCAST_LENGTH-4, u32_ChecksumBase)) {
            controller->onReceive(BleOperation::DISCOVER, true, (const uint8 *)mfrData, BROADCAST_LENGTH-4);
        }
    } else {
        uint8 crc = (uint8)mfrData[BROADCAST_LENGTH-1];
        if (crc == LibChecksum_GetChecksum8Bit((const uint8 *)mfrData, BROADCAST_LENGTH-1)) {
            controller->onReceive(BleOperation::DISCOVER, true, (const uint8 *)mfrData, BROADCAST_LENGTH-1);
        }
    }
}

void Ble::onAdvertiseDecoded(string address, string name, int32 rssi, const char *data, uint16 length) {
    if (state != SCANNING) return;

    std::map<string, BleController*>::iterator iter = controllers.find(address);
    if (iter==controllers.end()) {
        return;
    }

     BleController *controller = iter->second;
    //没必要，onScanRespondDecoded 已设置
    // controller->setRssi(rssi);
    // controller->setName(name);
    
    if (length < BROADCAST_LENGTH + 2) {
        return;
    }
    
    char mfrData[BROADCAST_LENGTH] = {0};
    ByteUtils::copy(mfrData, (const char*)data+2, BROADCAST_LENGTH);

    if (typeid(*controller) == typeid(AidexXController)) {
        uint32 u32_ChecksumBase = 0;
        uint8 i;
        for (i = 0; i < BROADCAST_LENGTH - 4; i += 4) {
            u32_ChecksumBase += LittleEndianByteUtils::byteToUnsignedInt((const uint8*)mfrData+i);
        }
        u32_ChecksumBase = u32_ChecksumBase % PRIME_NUM;
        
        uint32 crc = LittleEndianByteUtils::byteToUnsignedInt((const uint8*)mfrData+BROADCAST_LENGTH-4);
        if (crc == LibChecksum_GetChecksum32Bit((const uint8 *)mfrData, BROADCAST_LENGTH-4, u32_ChecksumBase)) {
            controller->onReceive(BleOperation::DISCOVER, true, (const uint8 *)mfrData, BROADCAST_LENGTH-4);
        }
    } else {
        uint8 crc = (uint8)mfrData[BROADCAST_LENGTH-1];
        if (crc == LibChecksum_GetChecksum8Bit((const uint8 *)mfrData, BROADCAST_LENGTH-1)) {
            controller->onReceive(BleOperation::DISCOVER, true, (const uint8 *)mfrData, BROADCAST_LENGTH-1);
        }
    }
}

void Ble::onAdvertiseWithAndroidRawBytes(string address, int32 rssi, const char *data, uint16 length) {
//    if (state != SCANNING) return;

    string name;
    string sn;
    vector<uint8> params;

    uint8 mfrLength1 = 0;
    char mfrData1[40] = {0};
    
    uint8 mfrLength2 = 0;
    char mfrData2[40] = {0};
    
    bool isFirstMfrData = true;
    for (int i = 0; i < length;) {
        int len = data[i++];
        if (len == 0 || i + len > length) {
            break;
        }
        uint8 flag = data[i++];
        if (flag == 0x09) {
            name = ByteUtils::bytesToUtf8String(data+i, len-1);
        } else if (flag == 0xFF) {
            if (isFirstMfrData) {
                mfrLength1 = len-1-2;
                ByteUtils::copy(mfrData1, (const char*)data+i+2, len-1-2);
            } else {
                mfrLength2 = len-1-2;
                ByteUtils::copy(mfrData2, (const char*)data+i+2, len-1-2);
                break;
            }
            isFirstMfrData = false;
        }
        i += len-1;
    }
    
    std::map<string, BleController*>::iterator iter = controllers.find(address);
    if (iter!=controllers.end()) {
        BleController *controller = iter->second;
        
        if (mfrLength1 == BROADCAST_LENGTH) {
            if (typeid(*controller) == typeid(AidexXController)) {
                uint32 u32_ChecksumBase = 0;
                uint8 i;
                for (i = 0; i < BROADCAST_LENGTH - 4; i += 4) {
                    u32_ChecksumBase += LittleEndianByteUtils::byteToUnsignedInt((const uint8*)mfrData1+i);
                }
                u32_ChecksumBase = u32_ChecksumBase % PRIME_NUM;
                
                uint32 crc = LittleEndianByteUtils::byteToUnsignedInt((const uint8*)mfrData1+BROADCAST_LENGTH-4);
                if (crc == LibChecksum_GetChecksum32Bit((const uint8 *)mfrData1, BROADCAST_LENGTH-4, u32_ChecksumBase)) {
                    controller->onReceive(BleOperation::DISCOVER, true, (const uint8 *)mfrData1, BROADCAST_LENGTH-4);
                }
            } else {
                uint8 crc = (uint8)mfrData1[BROADCAST_LENGTH-1];
                if (crc == LibChecksum_GetChecksum8Bit((const uint8 *)mfrData1, BROADCAST_LENGTH-1)) {
                    controller->onReceive(BleOperation::DISCOVER, true, (const uint8 *)mfrData1, BROADCAST_LENGTH-1);
                }
            }
        }
    }
    
    //"GoChek", "Insight", "Exactive" LocalName有SN，ScanResponse无SN
    //"Equil" LocalName、ScanResponse都有SN
    //"AiDEX X" LocalName有SN，ScanResponse无SN
    //"AiDEX" LocalName无SN，ScanResponse有SN
    list<string> snInNameList {"GoChek", "Insight", "Exactive", "Equil", "AiDEX X"};
    string pump = "Equil";
    string aidex = "AiDEX";
    string aidexX = "AiDEX X";
    
    dev_type type = DEV_TYPE_UNKNOWN;
    bool found = false;
    for (list<string>::iterator it = snInNameList.begin(); it != snInNameList.end(); ++it) {
        string::size_type idx = name.find(it->data());
        if (idx != string::npos) {
            found = true;
            break;
        }
    }
    
    if (found) {
        string delimiter = "-";
        string tmp = name;
        string::size_type pos = tmp.find(delimiter);
        if (pos != string::npos) {
            tmp.erase(0, pos + delimiter.length());
            sn = ByteUtils::trim(tmp);
            name.erase(pos, tmp.length() + delimiter.length());
            name = ByteUtils::trim(name);
        }
        
        if (pump.compare(name) == 0) {
           type = DEV_TYPE_PUMP;
        } else {
            type = DEV_TYPE_BGM;
        }
    }
    
    if (aidex.compare(name) == 0) {
        type = DEV_TYPE_CGM;
        if (mfrLength2 >= 8) {
            sn = ByteUtils::bytesToSnString(mfrData2, 6);
            params = vector<uint8>(mfrData2+6, mfrData2+mfrLength2);
        }
    } else if (aidexX.compare(name) == 0) {
        type = DEV_TYPE_CGM_X;
        params = vector<uint8>(mfrData2, mfrData2+mfrLength2);
    }
    
    if (sn.empty() || name.empty()) {
        return;
    }
    
    BleControllerInfo info;
    info.type = type;
    info.address = address;
    info.name = name;
    info.sn = sn;
    info.rssi = rssi;
    info.params = vector<uint8>(params);

    std::map<string, BleController*>::iterator iter2 = controllers.find(address);
    if (iter2!=controllers.end()) {
        BleController *controller = iter->second;
        controller->setInfo(info);
    }

    if (connectWhenDiscovered) {
        if(isFoundCurrent(address, name, sn)) {
            pSearchTimer->Cancel();
            LOGD("executeStopScan 1");
            executeStopScan();
            executeConnect(address);
        }
        return;
    }

    LOGD("name: %s sn: %s", name.data(), sn.data());
    if (discoverCallback != NULL) {
        discoverCallback(info);
    }
}

void Ble::onConnectSuccess() {
    buffer->flush();

    state = CONNECTED;
    connectWhenDiscovered = false;
    pSearchTimer->Cancel();
    pAckTimer->Cancel();
    pDisconTimer->Cancel();

    if (controller == NULL) return;

    DevComm::select(controller->getDevCommType());
    DevComm::getInstance()->setPacketLength(controller->getPacketLength());
    DevComm::getInstance()->turnOffEncryption();
    DevComm::getInstance()->setFrameOn(controller->isFrameEnabled());

    controller->onReceive(BleOperation::CONNECT, true);

    if (pairWhenConnected) {
        if (controller->getDevCommType() == DEV_COMM_TYPE_0) {
            sendPairCommand();
        } else {
            const uint8 *secret = controller->getSecret();
            write(controller->getPrivateCharacteristicUUID(), secret, controller->getKeyLength());
        }
    } else {
        if (controller->isAuthenticated()) {
            continueSending();
        } else {
            if (!commandList.empty() && commandList.front().port == 0xFF && commandList.front().op == 0xFF) {
                //Force Operation
                BleCommand command = commandList.front();
                write(controller->getPrivateCharacteristicUUID(), &(command.data)[0], command.data.size());
            } else {
                if (controller->getDevCommType() == DEV_COMM_TYPE_0) {
                    sendBondCommand();
                }
                else {
                    if (controller->isPaired() && !commandList.empty()) {
                        executeReadCharacteristic(controller->getCharacteristicUUID());
                    } else {
                        disconnect();
                    }
                }
            }
        }
    }
}

void Ble::onConnectFailure() {
    state = IDLE;
    connectWhenDiscovered = false;
    pSearchTimer->Cancel();
    pAckTimer->Cancel();
    pDisconTimer->Cancel();

    commandList.clear();
    if (controller != NULL) {
        controller->onReceive(BleOperation::CONNECT, false);
        controller = NULL;
    }
    startScan();
}

void Ble::onDisconnected() {
    state = IDLE;
    connectWhenDiscovered = false;
    pSearchTimer->Cancel();
    pAckTimer->Cancel();
    pDisconTimer->Cancel();

    commandList.clear();
    if (controller != NULL) {
        controller->onReceive(BleOperation::DISCONNECT, true);
        controller = NULL;
    }
    startScan();
}

void Ble::onReceiveData(const char *data, uint16 length) {
    if (controller == NULL) return;
    if (controller->isBufferEnabled()) {
        if (buffer->push((uint8*)data, length)) {
            DevComm::getInstance()->receive(&(buffer->receiveData)[0], buffer->receiveData.size());
        }
    } else {
        //一体机型号血糖仪（蓝牙连接后，主动发送数据）存在状态未connected时，就收到数据的情况
        //原因 iOS enableNotify 回调延迟导致onConnectSuccess时，数据正在接收中
        if (controller->isFrameEnabled() && (state != CONNECTED && state != DISCONNECTING)) return;
        DevComm::getInstance()->receive((const uint8*)data, length);
    }
}

void Ble::onReceiveData(uint16 uuid, const char *data, uint16 length) {
    if (controller == NULL) return;
    
    if (uuid == controller->getCharacteristicUUID()) {
        
        if (controller->getDevCommType() == DEV_COMM_TYPE_0) {
            onReceiveData(data, length);
            return;
        }
        
        if (controller->isAuthenticated()) {
            onReceiveData(data, length);
            return;
        }
        if (length == controller->getKeyLength() + 1) {
            bool success = controller->handleCommand(0xFF, 0xFF, 0xFF, (const uint8 *)data, length);
            if (success) {
                if (!commandList.empty()) {
                    continueSending();
                    return;
                }
            }
        }
        disconnect();
        return;
    }
    
    if (controller->getDevCommType() == DEV_COMM_TYPE_1 && uuid == controller->getPrivateCharacteristicUUID() && controller->isAuthenticated()) {
        if (controller == NULL) return;
        if (controller->isBufferEnabled()) {
            if (buffer->push((uint8*)data, length)) {
                DevComm::getInstance()->receive(uuid, &(buffer->receiveData)[0], buffer->receiveData.size());
            }
        } else {
            //一体机型号血糖仪（蓝牙连接后，主动发送数据）存在状态未connected时，就收到数据的情况
            //原因 iOS enableNotify 回调延迟导致onConnectSuccess时，数据正在接收中
            if (controller->isFrameEnabled() && (state != CONNECTED && state != DISCONNECTING)) return;
            DevComm::getInstance()->receive(uuid, (const uint8*)data, length);
        }
        return;
    }


    if (controller->getDevCommType() == DEV_COMM_TYPE_0) {
        if (!commandList.empty() && commandList.front().port == 0xFF && commandList.front().op == 0xFF) {
            BleCommand command = commandList.front();
            commandList.pop_front();
            bool success = controller->handleCommand(0xFF, 0xFF, command.param, (const uint8 *)data, length);
            if (success && !commandList.empty()) {
                if (commandList.front().port == 0xFF && commandList.front().op == 0xFF) {
                    //Force Operation
                    BleCommand command = commandList.front();
                    write(controller->getPrivateCharacteristicUUID(), &(command.data)[0], command.data.size());
                }
            } else {
                disconnect();
            }
            return;
        }
    }
    
    bool success = controller->handleCommand(0xFF, 0xFF, 0xFF, (const uint8 *)data, length);
    if (success) {
        if (!commandList.empty()) {
            executeReadCharacteristic(controller->getCharacteristicUUID());
            return;
        }
    }
    continueSending();
}

void Ble::startScan() {
    if (!isInConnection()) {
        state = SCANNING;
        executeStartScan();
    }
}

void Ble::stopScan() {
    state = IDLE;
    LOGD("executeStopScan 3");
    executeStopScan();
}

bool Ble::pair(BleController *controller) {
    if (controller == NULL) return false;
    this->controller = controller;
    if (!isInConnection()) {
        pairWhenConnected = true;
        connect();
        return true;
    } else if (!this->controller->isAuthenticated()) {
        sendPairCommand();
        return true;
    } else {
        return false;
    }
}

bool Ble::send(BleController *controller, uint8 port, uint8 op, uint8 param, const uint8 *data, uint16 length) {
    if (controller == NULL) return false;
    if (state == DISCONNECTING) {
        LOGE("Ble DISCONNECTING");
        return false;
    }
    BleCommand command(port, op, param, data, length);
    if (!isInConnection()) {
        this->controller = controller;
        commandList.push_back(command);
        pairWhenConnected = false;
        connect();
        return true;
    } else if (this->controller == controller || this->controller->getMac() == controller->getMac()) {
        pAckTimer->Cancel();
        pDisconTimer->Cancel();
        LOGD("send, pAckTimer、pDisconTimer cancel");

        if (controller->isAuthenticated() && commandList.empty() && !DevComm::getInstance()->isBusy()) {
            DevComm::getInstance()->send(
                        command.port,
                        Global::MODE_ACKNOWLEDGE,
                        command.op,
                        command.param,
                        &(command.data)[0],
                        command.data.size());
        } else {
            commandList.push_back(command);
            if (controller->getDevCommType() == DEV_COMM_TYPE_1 && controller->isPaired() && !controller->isAuthenticated()) {
                executeReadCharacteristic(controller->getCharacteristicUUID());
            }
        }
        return true;
    } else {
        return false;
    }
}

bool Ble::isInConnection() {
    return state == CONNECTING || state == CONNECTED;
}

bool Ble::isFoundCurrent(string address, string name, string sn) {
    if (controller == NULL) return false;
    if (sn == controller->getSn() && ByteUtils::trim(name) == ByteUtils::trim(controller->getName())) {
        controller->setMac(address);
        return true;
    } else {
        return false;
    }
}

void Ble::connect(BleController *controller) {
    if (controller == NULL) return;
    this->controller = controller;
    this->connect();
}

void Ble::connect() {
    if (controller == NULL) return;
    if (isInConnection()) {
        controller->onReceive(BleOperation::CONNECT, false);
        return;
    }
    state = CONNECTING;
    string mac = controller->getMac();
    if (isReadyToConnect(mac)) {
        LOGD("executeStopScan 4");
        executeStopScan();
        executeConnect(mac);
    } else {
        pSearchTimer->Cancel();
        pSearchTimer->AsyncOnce(discoverTimeoutSeconds*1000, [this](){
            if (connectWhenDiscovered) {
                connectWhenDiscovered = false;
                if (controller != NULL) {
                    controller->onReceive(BleOperation::DISCOVER, false);
                }
                state = SCANNING;
                executeStartScan();
            }
        });
        connectWhenDiscovered = true;
        executeStartScan();
    }
}

void Ble::disconnect() {
    if (controller != NULL && controller->isAutoDisconnect()) {
        state = DISCONNECTING;
    }
    commandList.clear();
    executeDisconnect();
    bool ok = pDisconTimer->AsyncOnce(2000, [this](){
        LOGI("pOnDisconTimer execute");
        if (state == DISCONNECTING) {
            onDisconnected();
        }
    });
    LOGI("pOnDisconTimer SET %d", ok);
}

void Ble::sendPairCommand() {
    if (controller == NULL) return;
    vector<uint8> data = vector<uint8>(controller->hostAddress);
    data.insert(data.end(), controller->pairParameter.begin(), controller->pairParameter.end());
    DevComm::getInstance()->send(
                controller->getCommPort(),
                Global::MODE_ACKNOWLEDGE,
                Global::OPERATION_PAIR,
                DEVICE_TYPE,
                &data[0],
                data.size());
}

void Ble::sendBondCommand() {
    if (controller == NULL || !controller->isPaired()) return;
    vector<uint8> data = vector<uint8>(controller->accessId);
    uint8 rxRate = controller->getRxRate();
    if (rxRate > 0) data.push_back(rxRate);
    DevComm::getInstance()->send(
                controller->getCommPort(),
                Global::MODE_ACKNOWLEDGE,
                Global::OPERATION_BOND,
                DEVICE_TYPE,
                &data[0],
                data.size());
    if (controller->isEncryptionEnabled()) {
        DevComm::getInstance()->readyForEncryption((uint8*)(controller->accessKey.data()), controller->accessKey.size());
    }
}

void Ble::handleEvent(uint8 event) {
    if (event == Global::EVENT_TIMEOUT) {
        if (controller != NULL) {
            controller->onReceive(BleOperation::DISCONNECT, false);
            if (controller->isAutoDisconnect()) {
                controller = NULL;
            }
        }
//        if (controller->isAutoDisconnect()) {
            disconnect();
//        }
    }
}

void Ble::handleCommand(uint8 port, uint8 op, uint8 param, const uint8 *data, uint16 length) {
    if (controller == NULL) return;
    if (state != Ble::CONNECTED) return;

    int bleOp = BleOperation::UNKNOWN;
    bool success = false;
    uint keyLength = controller->getKeyLength();

    if (port == controller->getCommPort()) {
        success = data[0] == Global::FUNCTION_OK;
        if (op==Global::OPERATION_PAIR) bleOp = BleOperation::PAIR;
        if (op==Global::OPERATION_UNPAIR) bleOp = BleOperation::UNPAIR;
        if (op==Global::OPERATION_BOND) {
            bleOp = BleOperation::BOND;
            success |= length >= 1 + keyLength;
        }
    }

    if (bleOp == BleOperation::PAIR) {
        if (success) {
            controller->setId((char*)data+1);
            controller->setKey((char*)data+1+controller->getIdLength());
        }
    } else if (!controller->isAuthenticated()) {
        if (bleOp == BleOperation::BOND) {
            if (length > 1) {
                success &= length >= 1 + keyLength;
                if (success) {
                    success &= data[keyLength] == LibChecksum_GetChecksum8Bit(data, keyLength);
                }
                if (success) {
                    DevComm::getInstance()->updateEncryption(data, keyLength);
                }
            }
        } else {
            bleOp = BleOperation::BOND;
            success = false;
        }
    }

    if (bleOp == BleOperation::UNKNOWN) {
        success = controller->handleCommand(port, op, param, data, length);
    } else {
        controller->onReceive(bleOp, success, data, length);
    }

    if (success && bleOp == BleOperation::UNPAIR) {
        controller->setId(NULL);
        controller->setKey(NULL);
    }
    
    if (success && bleOp != BleOperation::UNPAIR) {
        if (controller->isAuthenticated()) {
            if (controller->isAutoSending())
                continueSending();
        } else {
            sendBondCommand();
        }
    } else {
        disconnect();
    }
}

void Ble::write(const uint8 *data, uint16 length) {
    static uint8 index = 0;
    static char buffer[ATT_MTU_SIZE] = {0};

    if (controller == NULL || (state != CONNECTED && state != DISCONNECTING)) return;

    if (controller->isBufferEnabled()) {
        index++;
        uint8 ctn = (index << 4) | 0x00;
        uint8 end = (index << 4) | 0x01;

        uint8 i = 0;
        while (length >= i + ATT_MTU_SIZE) {
            ByteUtils::copy(buffer, (const char*)data + i, ATT_MTU_SIZE - 1);
            buffer[ATT_MTU_SIZE - 1] = ctn;
            executeWrite(buffer, ATT_MTU_SIZE);
            i += ATT_MTU_SIZE - 1;
        }
        ByteUtils::copy(buffer, (const char*)data + i, length - i);
        buffer[length - i] = end;
        executeWrite(buffer, length - i + 1);
    } else {
        executeWrite((const char*)data, length);
    }
    
    if (state != DISCONNECTING) {
        pAckTimer->Cancel();
        pDisconTimer->Cancel();
        LOGD("pAckTimer、pDisconTimer Cancel");
    }

    if (state == DISCONNECTING && controller != NULL && controller->isAutoDisconnect()) {
        bool ok;
        ok = pDisconTimer->AsyncOnce(200, [this]() {
            LOGD("pDisconTimer execute");
            disconnect();
        });
        LOGD("pDisconTimer SET %d", ok);
    }
}

void Ble::write(uint16 uuid, const uint8 *data, uint16 length) {
    if (controller == NULL || (state != CONNECTED && state != DISCONNECTING)) return;

    executeWriteCharacteristic(uuid, (const char*)data, length);
}

void Ble::continueSending() {
    if (controller == NULL) return;
    pDisconTimer->Cancel();
    LOGD("pDisconTimer Cancel");
    if (!commandList.empty()) {
        pAckTimer->Cancel();
        LOGD("pAckTimer Cancel");
        BleCommand command = commandList.front();
        commandList.pop_front();
        DevComm::getInstance()->send(command.port, 0, command.op, command.param, &(command.data)[0], command.data.size());
    } else if (controller->isAutoDisconnect() || controller->isAcknowledgement()) {
        bool ok;
        ok = pAckTimer->AsyncOnce(200, [this](){
            LOGI("pAckTimer execute");
            
            if (controller != NULL && controller->isAutoDisconnect()) {
                LOGI("DISCONNECTING");
                state = DISCONNECTING;
            }
            if (controller != NULL && controller->isAcknowledgement()) {
                DevComm::getInstance()->send(controller->getCommPort(), Global::MODE_NO_ACKNOWLEDGE, Global::OPERATION_ACKNOWLEDGE, 0, 0, 0);
                LOGI("ACK");
            } else if (controller != NULL && controller->isAutoDisconnect()){
                LOGI("DISCONNECT NO_ACK");
                disconnect();
            }
        });
        LOGI("pAckTimer SET %d", ok);
    }
}

Ble::ReceiveBuffer::ReceiveBuffer(int maxLength) {
    mMaxLength = maxLength;
    mPointer = 0;
    mIndex = 0;
}

void Ble::ReceiveBuffer::flush() {
    dataBuffer.clear();
    mPointer = 0;
}

bool Ble::ReceiveBuffer::push(const uint8 *data, uint16 length) {
    int index = (data[length - 1] & 0xf0) >> 4;
    int last = data[length - 1] & 0x0f;

    vector<uint8> add(data, data+length);
    add.pop_back();

    if (index != mIndex || (mPointer + (int)add.size()) > mMaxLength) {
        mIndex = index;
        flush();
    }
    dataBuffer.insert(dataBuffer.end(), add.begin(), add.end());
    mPointer += add.size();
    if (last != 0) {
        receiveData = vector<uint8>(dataBuffer);
        flush();
        return true;
    }
    return false;
}

Ble::BleCommand::BleCommand(uint8 port, uint8 op, uint8 param) {
    this->port = port;
    this->op = op;
    this->param = param;
    this->data = vector<uint8>(0);
}

Ble::BleCommand::BleCommand(uint8 port, uint8 op, uint8 param, const uint8 *data, uint16 length) {
    this->port = port;
    this->op = op;
    this->param = param;
    this->data = vector<uint8>(data, data+length);
}
