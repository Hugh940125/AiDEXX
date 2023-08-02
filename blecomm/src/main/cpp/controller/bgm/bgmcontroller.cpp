#include "bgmcontroller.h"
#include "../../ble.h"
#include "../../devcomm/devcommclass.h"
#include "../../constant/bgm/bgmoperation.h"
#include "../../util/byteutils.h"


BgmController::BgmController() : BleController()
{
    type = DEV_TYPE_BGM;
    authenticated = true;
    frameEnable = true;
    acknowledgement = false;
    autoDisconnect = false;
}

uint16 BgmController::getDeviceInfo() {
    if (send(BgmPort::PORT_SYSTEM,
             Global::OPERATION_GET,
             BgmSystem::PARAM_DEVICE,
             0,
             0)) {
        return BgmOperation::GET_DEVICE_INFO;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 BgmController::getHistory(uint16 index) {
    uint8 data[2];
    BigEndianByteUtils::unsignedShortToBytes(index, data);
    if (send(BgmPort::PORT_MONITOR,
             Global::OPERATION_GET,
             BgmMonitor::PARAM_HISTORY,
             data,
             2)) {
        return BgmOperation::GET_HISTORY;
    } else {
        return BleOperation::BUSY;
    }
}

bool BgmController::handleCommand(uint8 port, uint8 op, uint8 param, const uint8 *data, uint16 length) {
    int bgOp = BgmOperation::UNKNOWN;
    bool success = (op==Global::OPERATION_ACKNOWLEDGE) || (op==Global::OPERATION_NOTIFY);
    switch (port) {
    case BgmPort::PORT_SYSTEM:
        if (op==Global::OPERATION_NOTIFY && param==BgmSystem::PARAM_DEVICE) bgOp = BgmOperation::GET_DEVICE_INFO;
        break;
    case BgmPort::PORT_MONITOR:
        if (op==Global::OPERATION_NOTIFY && param==BgmMonitor::PARAM_HISTORY) bgOp = BgmOperation::GET_HISTORY;
        else if (op==Global::OPERATION_NOTIFY && param==BgmMonitor::PARAM_ERROR_CODE) bgOp = BgmOperation::GET_ERROR_CODE;
        else if (op==Global::OPERATION_NOTIFY && param==BgmMonitor::PARAM_COUNT_DOWN) bgOp = BgmOperation::GET_COUNT_DOWN;
        else if (op==Global::OPERATION_NOTIFY && param==BgmMonitor::PARAM_BLOOD_SAMPLE) bgOp = BgmOperation::GET_BLOOD_SAMPLE;
        else if (op==Global::OPERATION_NOTIFY && param==BgmMonitor::PARAM_CONTROL_SOLUTION) bgOp = BgmOperation::GET_CONTROL_SOLUTION;
        else if (op==Global::OPERATION_NOTIFY && param==BgmMonitor::PARAM_BLOOD_LO) bgOp = BgmOperation::GET_BLOOD_LO;
        else if (op==Global::OPERATION_NOTIFY && param==BgmMonitor::PARAM_BLOOD_HI) bgOp = BgmOperation::GET_BLOOD_HI;
        break;
    default:
        break;
    }

    acknowledgement = true;
    autoDisconnect = true;
    if (port == BgmPort::PORT_MONITOR && param >= BgmMonitor::PARAM_ERROR_CODE) {
        acknowledgement = false;
        autoDisconnect = false;
    }
    
    if (op==Global::OPERATION_NOTIFY && param==BgmMonitor::PARAM_ERROR_CODE) {
        uint16 value = BigEndianByteUtils::byteToUnsignedShort(data) - 1000;
        
        uint8 tmp[2];
        BigEndianByteUtils::unsignedShortToBytes(value, tmp);
        data = tmp;
    }

    onReceive(bgOp, success, data, length);

    return success;
}

void BgmController::onReceive(uint16 op, bool success, const uint8 *data, uint16 length) {
    switch (op) {
    case BleOperation::CONNECT:
    case BleOperation::DISCONNECT:
        acknowledgement = false;
        break;
    }
    BleController::onReceive(op, success, data, length);
}
