#include "cgmcontroller.h"
#include "../../ble.h"
#include "../../devcomm/devcommclass.h"
#include "../../devcomm/CLibrary/lib_checksum.h"
#include "../../constant/cgm/cgmoperation.h"
#include "../../util/byteutils.h"
#include "../../parser/cgm/cgmbroadcastparser.h"

CgmController::CgmController() : BleController()
{
    initialize();
}

CgmController::CgmController(const BleControllerInfo &info) : BleController()
{
    initialize();
    setInfo(info);
}

void CgmController::initialize()
{
    type = DEV_TYPE_CGM;
    authenticated = false;
    frameEnable = false;
    acknowledgement = true;
    autoDisconnect = false;

    hypo = 0;
    hyper = 0;
    sensorIndex = 0;
}

void CgmController::initialSettings(float32 hypo, float32 hyper) {
    if (hypo  <  0.0) { hypo  =  0.0;}
    if (hypo  > 25.5) { hypo  = 25.5;}
    if (hyper <  0.0) { hyper =  0.0;}
    if (hyper > 25.5) { hyper = 25.5;}

    this->hypo = hypo;
    this->hyper = hyper;

    uint8 parameters[2] = {(uint8)(hypo*10), (uint8)(hyper*10)};
    this->pairParameter = vector<uint8>(parameters, parameters+2);
}

uint16 CgmController::getDeviceInfo() {
    if (send(CgmPort::PORT_SYSTEM,
             Global::OPERATION_GET,
             CgmSystem::PARAM_DEVICE,
             0,
             0)) {
        return CgmOperation::GET_DEVICE_INFO;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 CgmController::getSingleHistory(uint16 index) {
    uint8 data[2];
    LittleEndianByteUtils::unsignedShortToBytes(index, data);
    if (send(CgmPort::PORT_MONITOR,
             Global::OPERATION_GET,
             CgmMonitor::PARAM_HISTORY,
             data,
             2)) {
        return CgmOperation::GET_SINGLE_HISTORY;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 CgmController::getHistories(uint16 index) {
    if (sensorIndex <= 0 && getSingleHistory(1) == BleOperation::BUSY) {
        return BleOperation::BUSY;
    }
    
    uint8 data[2];
    LittleEndianByteUtils::unsignedShortToBytes(index, data);
    if (send(CgmPort::PORT_MONITOR,
             Global::OPERATION_GET,
             CgmMonitor::PARAM_HISTORIES,
             data,
             2)) {
        return CgmOperation::GET_HISTORIES;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 CgmController::getFullHistories(uint16 index) {
    if (sensorIndex <= 0 && getSingleHistory(1) == BleOperation::BUSY) {
        return BleOperation::BUSY;
    }
    
    uint8 data[2];
    LittleEndianByteUtils::unsignedShortToBytes(index, data);
    if (send(CgmPort::PORT_MONITOR,
             Global::OPERATION_GET,
             CgmMonitor::PARAM_HISTORIES_FULL,
             data,
             2)) {
        return CgmOperation::GET_HISTORIES_FULL;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 CgmController::newSensor(bool isNew, int64 datetime) {
    uint8 data[5];
    uint16 length = 1;
    data[0] = isNew ? 1 : 0;
    if (datetime >= CgmHistory::YEAR_2000) {
        LittleEndianByteUtils::unsignedIntToBytes((uint32)(datetime - CgmHistory::YEAR_2000), data+1);
        length += 4;
    }
    if (send(CgmPort::PORT_GLUCOSE,
             Global::OPERATION_SET,
             CgmGlucose::PARAM_NEW_SENSOR,
             data,
             length)) {
        return CgmOperation::SET_NEW_SENSOR;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 CgmController::setDatetime(int64 datetime) {
    uint8 data[4];
    LittleEndianByteUtils::unsignedIntToBytes((uint32)(datetime - CgmHistory::YEAR_2000), data);
    if (send(CgmPort::PORT_MONITOR,
             Global::OPERATION_SET,
             CgmMonitor::PARAM_DATE_TIME,
             data,
             4)) {
        return CgmOperation::SET_DATETIME;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 CgmController::recordBg(float32 glucose, int64 datetime) {
    uint8 data[6];
    LittleEndianByteUtils::unsignedIntToBytes((uint32)(datetime - CgmHistory::YEAR_2000), data);
    LittleEndianByteUtils::unsignedShortToBytes((unsigned int)(glucose*10), data+4);
    if (send(CgmPort::PORT_GLUCOSE,
             Global::OPERATION_SET,
             CgmGlucose::PARAM_GLUCOSE,
             data,
             6)) {
        return CgmOperation::RECORD_BG;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 CgmController::calibration(float32 glucose, int64 datetime) {
    uint8 data[6];
    LittleEndianByteUtils::unsignedIntToBytes((uint32)(datetime - CgmHistory::YEAR_2000), data);
    LittleEndianByteUtils::unsignedShortToBytes((unsigned int)(glucose*10), data+4);
    if (send(CgmPort::PORT_GLUCOSE,
             Global::OPERATION_SET,
             CgmGlucose::PARAM_CALIBRATON,
             data,
             6)) {
        return CgmOperation::CALIBRATION;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 CgmController::setHyper(float32 hyper) {
    uint8 data[1];
    data[0] = (uint8)(hyper*10);
    if (send(CgmPort::PORT_GLUCOSE,
             Global::OPERATION_SET,
             CgmGlucose::PARAM_HYPER,
             data,
             1)) {
        return CgmOperation::SET_HYPER;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 CgmController::setHypo(float32 hypo) {
    uint8 data[1];
    data[0] = (uint8)(hypo*10);
    if (send(CgmPort::PORT_GLUCOSE,
             Global::OPERATION_SET,
             CgmGlucose::PARAM_HYPO,
             data,
             1)) {
        return CgmOperation::SET_HYPO;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 CgmController::getDeviceCheck() {
    if (send(CgmPort::PORT_GLUCOSE,
             Global::OPERATION_GET,
             CgmGlucose::PARAM_CONTROL,
             0,
             0)) {
        return CgmOperation::GET_DEVICE_CHECK;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 CgmController::getDefaultParamData() {
    if (send(CgmPort::PORT_GLUCOSE,
             Global::OPERATION_GET,
             CgmGlucose::PARAM_DEFAULT_PARAM,
             0,
             0)) {
        return CgmOperation::GET_DEFAULT_PARAM;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 CgmController::setDefaultParamData(const uint8 *data, uint16 length) {
    if (send(CgmPort::PORT_GLUCOSE,
             Global::OPERATION_SET,
             CgmGlucose::PARAM_DEFAULT_PARAM,
             data,
             length)) {
        return CgmOperation::SET_DEFAULT_PARAM;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 CgmController::setDefaultParamData(float32 value[]) {
    uint8 data[84] = {0};
    LittleEndianByteUtils::unsignedIntToBytes((uint32)(value[CgmDefaultParam::DP_EXPIRATION_TIME] * 86400), data);
    int i;
    for(i = CgmDefaultParam::DP_CAL_FACTOR_DEFAULT; i <= CgmDefaultParam::DP_REF_IMAG_OFFSET; i++)
    {
        if (value[i] >= 0)
            value[i] += 0.005;
        else
            value[i] -= 0.005;
        LittleEndianByteUtils::unsignedShortToBytes((uint16)(int16)(value[i] * 100), data + 4 + (i-1)*2);
    }
    if (send(CgmPort::PORT_GLUCOSE,
             Global::OPERATION_SET,
             CgmGlucose::PARAM_DEFAULT_PARAM,
             data,
             84)) {
        return CgmOperation::SET_DEFAULT_PARAM;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 CgmController::setGcBiasTrimming(uint16 value) {
    uint8 data[2];
    LittleEndianByteUtils::unsignedShortToBytes(value, data);
    if (send(CgmPort::PORT_GLUCOSE,
             Global::OPERATION_SET,
             CgmGlucose::PARAM_GC_BIAS_TRIMMING,
             data,
             2)) {
        return CgmOperation::SET_GC_BIAS_TRIMMING;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 CgmController::setGcImeasTrimming(uint8 channel, int16 zero, uint16 scale) {
    uint8 data[5];
    data[0] = channel;
    LittleEndianByteUtils::shortToBytes(zero, data+1);
    LittleEndianByteUtils::unsignedShortToBytes(scale, data+3);
    if (send(CgmPort::PORT_GLUCOSE,
             Global::OPERATION_SET,
             CgmGlucose::PARAM_GC_IMEAS_TRIMMING,
             data,
             5)) {
        return CgmOperation::SET_GC_IMEAS_TRIMMING;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 CgmController::getBroadcastData() {
    uint8 data[18];
    if (send(CgmPort::PORT_COMM,
             Global::OPERATION_GET,
             CgmComm::PARAM_BROADCAST_DATA,
             data,
             sizeof(data))) {
        return CgmOperation::GET_BROADCAST_DATA;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 CgmController::setCalFactor(float32 calFactor) {
    uint8 data[2];
    float32 v = calFactor;
    if (v >= 0)
        v += 0.005;
    else
        v -= 0.005;
    LittleEndianByteUtils::unsignedShortToBytes((uint16)(int16)(v * 100), data);

    if (send(CgmPort::PORT_GLUCOSE,
             Global::OPERATION_SET,
             CgmGlucose::PARAM_CAL_FACTOR,
             data,
             sizeof(data))) {
        return CgmOperation::SET_CAL_FACTOR;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 CgmController::setOffset(float32 offset) {
    uint8 data[2];
    float32 v = offset;
    if (v >= 0)
        v += 0.005;
    else
        v -= 0.005;
    LittleEndianByteUtils::unsignedShortToBytes((uint16)(int16)(v * 100), data);
    
    if (send(CgmPort::PORT_GLUCOSE,
             Global::OPERATION_SET,
             CgmGlucose::PARAM_OFFSET,
             data,
             sizeof(data))) {
        return CgmOperation::SET_OFFSET;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 CgmController::getCalFactor() {
    if (send(CgmPort::PORT_GLUCOSE,
             Global::OPERATION_GET,
             CgmGlucose::PARAM_CAL_FACTOR,
             0,
             0)) {
        return CgmOperation::GET_CAL_FACTOR;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 CgmController::getOffset() {
    if (send(CgmPort::PORT_GLUCOSE,
             Global::OPERATION_GET,
             CgmGlucose::PARAM_OFFSET,
             0,
             0)) {
        return CgmOperation::GET_OFFSET;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 CgmController::forceUnpair() {
    char data[] = "force unpair all";
    uint16 length = sizeof(data) - 1;
    if (send(0xFF,
             0xFF,
             CgmParamExt::PARAM_EXT_FORCE_UNPAIR,
             (const uint8 *)data,
             length)) {
        return CgmOperation::EXT_FORCE_UNPAIR;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 CgmController::forceReboot() {
    char data[] = "force reboot all";
    uint16 length = sizeof(data) - 1;
    if (send(0xFF,
             0xFF,
             CgmParamExt::PARAM_EXT_FORCE_REBOOT,
             (const uint8 *)data,
             length)) {
        return CgmOperation::EXT_FORCE_REBOOT;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 CgmController::pairWithForceUnpair() {
    char data[] = "force unpair all";
    uint16 length = sizeof(data) - 1;
    if (send(0xFF,
             0xFF,
             CgmParamExt::PARAM_EXT_FORCE_UNPAIR_AND_PAIR,
             (const uint8 *)data,
             length)) {
        return CgmOperation::EXT_FORCE_UNPAIR;
    } else {
        return BleOperation::BUSY;
    }
}

void CgmController::setInfo(const BleControllerInfo &info) {
    BleController::setInfo(info);
    int size = (int)info.params.size();
    if (size > 0) {
        hypo = (float32)info.params[0] / 10.0;
    }
    if (size > 1) {
        hyper = (float32)info.params[1] / 10.0;
    }
}

bool CgmController::handleCommand(uint8 port, uint8 op, uint8 param, const uint8 *data, uint16 length) {
    if (port == 0xFF && op == 0xFF) {
        if (param == CgmParamExt::PARAM_EXT_FORCE_UNPAIR) {
            onReceive(CgmOperation::EXT_FORCE_UNPAIR, true, data, length);
            return true;
        }
        if (param == CgmParamExt::PARAM_EXT_FORCE_REBOOT) {
            onReceive(CgmOperation::EXT_FORCE_REBOOT, true, data, length);
            return true;
        }
        if (param == CgmParamExt::PARAM_EXT_FORCE_UNPAIR_AND_PAIR) {
            onReceive(CgmOperation::EXT_FORCE_UNPAIR, true, data, length);
            this->pair();
            return true;
        }
    }
    
    int cgmOp = CgmOperation::UNKNOWN;
    bool success = (op==Global::OPERATION_ACKNOWLEDGE) || (op==Global::OPERATION_NOTIFY);
    switch (port) {
    case CgmPort::PORT_SYSTEM:
        if (op==Global::OPERATION_NOTIFY && param==CgmSystem::PARAM_DEVICE) cgmOp = CgmOperation::GET_DEVICE_INFO;
        break;
   case CgmPort::PORT_COMM:
        if (op==Global::OPERATION_NOTIFY && param==CgmComm::PARAM_BROADCAST_DATA) cgmOp = CgmOperation::GET_BROADCAST_DATA;
        break;
    case CgmPort::PORT_MONITOR:
        if (op==Global::OPERATION_NOTIFY && param==CgmMonitor::PARAM_DATE_TIME) cgmOp = CgmOperation::GET_DATETIME;
        else if (op==Global::OPERATION_ACKNOWLEDGE && param==CgmMonitor::PARAM_DATE_TIME) cgmOp = CgmOperation::SET_DATETIME;
        else if (op==Global::OPERATION_NOTIFY && param==CgmMonitor::PARAM_HISTORIES) cgmOp = CgmOperation::GET_HISTORIES;
        else if (op==Global::OPERATION_NOTIFY && param==CgmMonitor::PARAM_HISTORIES_FULL) cgmOp = CgmOperation::GET_HISTORIES_FULL;
        else if (op==Global::OPERATION_NOTIFY && param==CgmMonitor::PARAM_HISTORY) cgmOp = CgmOperation::GET_SINGLE_HISTORY;
        break;
    case CgmPort::PORT_GLUCOSE:
        if (op==Global::OPERATION_ACKNOWLEDGE && param==CgmGlucose::PARAM_NEW_SENSOR) cgmOp = CgmOperation::SET_NEW_SENSOR;
        else if (op==Global::OPERATION_NOTIFY && param==CgmGlucose::PARAM_HYPO) cgmOp = CgmOperation::GET_HYPO;
        else if (op==Global::OPERATION_ACKNOWLEDGE && param==CgmGlucose::PARAM_HYPO) cgmOp = CgmOperation::SET_HYPO;
        else if (op==Global::OPERATION_NOTIFY && param==CgmGlucose::PARAM_HYPER) cgmOp = CgmOperation::GET_HYPER;
        else if (op==Global::OPERATION_ACKNOWLEDGE && param==CgmGlucose::PARAM_HYPER) cgmOp = CgmOperation::SET_HYPER;
        else if (op==Global::OPERATION_ACKNOWLEDGE && param==CgmGlucose::PARAM_GLUCOSE) cgmOp = CgmOperation::RECORD_BG;
        else if (op==Global::OPERATION_ACKNOWLEDGE && param==CgmGlucose::PARAM_CALIBRATON) cgmOp = CgmOperation::CALIBRATION;
        else if (op==Global::OPERATION_NOTIFY && param==CgmGlucose::PARAM_CONTROL) cgmOp = CgmOperation::GET_DEVICE_CHECK;
        else if (op==Global::OPERATION_NOTIFY && param==CgmGlucose::PARAM_DEFAULT_PARAM) cgmOp = CgmOperation::GET_DEFAULT_PARAM;
        else if (op==Global::OPERATION_ACKNOWLEDGE && param==CgmGlucose::PARAM_DEFAULT_PARAM) cgmOp = CgmOperation::SET_DEFAULT_PARAM;
        else if (op==Global::OPERATION_ACKNOWLEDGE && param==CgmGlucose::PARAM_GC_BIAS_TRIMMING) cgmOp = CgmOperation::SET_GC_BIAS_TRIMMING;
        else if (op==Global::OPERATION_ACKNOWLEDGE && param==CgmGlucose::PARAM_GC_IMEAS_TRIMMING) cgmOp = CgmOperation::SET_GC_IMEAS_TRIMMING;
        else if (op==Global::OPERATION_ACKNOWLEDGE && param==CgmGlucose::PARAM_CAL_FACTOR) cgmOp = CgmOperation::SET_CAL_FACTOR;
        else if (op==Global::OPERATION_NOTIFY && param==CgmGlucose::PARAM_CAL_FACTOR) cgmOp = CgmOperation::GET_CAL_FACTOR;
        else if (op==Global::OPERATION_ACKNOWLEDGE && param==CgmGlucose::PARAM_OFFSET) cgmOp = CgmOperation::SET_OFFSET;
        else if (op==Global::OPERATION_NOTIFY && param==CgmGlucose::PARAM_OFFSET) cgmOp = CgmOperation::GET_OFFSET;
        break;
    default:
        break;
    }
    
    LOGD("CgmOperation: %04X; success: %d", cgmOp, success);
    
    if (success && cgmOp == CgmOperation::GET_SINGLE_HISTORY) {
        sensorIndex = data[6];
        startUp = LittleEndianByteUtils::byteToUnsignedInt(data);

        LOGD("GET_SINGLE_HISTORY: %d - %d", startUp, sensorIndex);
        return success;
    }
    if (success && (cgmOp == CgmOperation::GET_HISTORIES || cgmOp == CgmOperation::GET_HISTORIES_FULL)) {
        uint8 buffer[length + 6];
        buffer[0] = sensorIndex;
        buffer[1] = 0xFF;
        
        uint8 timestamp[4];
        LittleEndianByteUtils::unsignedIntToBytes(startUp, timestamp);
        
        ByteUtils::copy((char*)buffer + 2, (const char*)timestamp, 4);
        ByteUtils::copy((char*)buffer + 6, (const char*)data, length);
        
        onReceive(cgmOp, success, buffer, length + 6);

        return success;
    }
    
    if (success && cgmOp == CgmOperation::GET_BROADCAST_DATA && length == CgmBroadcastParser::MIN_BYTES_LENGTH - 2) {
        uint16 broadcastLength = CgmBroadcastParser::MIN_BYTES_LENGTH;

        uint8 broadcast[broadcastLength];
        ByteUtils::copy((char*)broadcast, (const char*)data, length);

        broadcast[broadcastLength - 2] = 0xFF;
        broadcast[broadcastLength - 1] = LibChecksum_GetChecksum8Bit(broadcast, broadcastLength - 1);
        
        onReceive(cgmOp, success, broadcast, broadcastLength);
        
        return success;
    }
    onReceive(cgmOp, success, data, length);
    
    return success;
}

void CgmController::onReceive(uint16 op, bool success, const uint8 *data, uint16 length) {
    switch (op) {
        case BleOperation::CONNECT:
        case BleOperation::DISCONNECT:
        {
            authenticated = false;
            sensorIndex = 0;
            startUp = 0;
        }
            break;
        case BleOperation::BOND:
        {
            authenticated = success;
            sensorIndex = 0;
            startUp = 0;
        }
            break;
    }
    BleController::onReceive(op, success, data, length);
}
