#include "pumpcontroller.h"
#include "../../ble.h"
#include "../../devcomm/devcommclass.h"
#include "../../util/byteutils.h"

static const uint32 SECONDS_PER_HOUR = 3600;
static const uint32 HOURS_PER_DAY = 24;

PumpController::PumpController() : BleController()
{
    type = DEV_TYPE_PUMP;
    authenticated = true;
	frameEnable = false;
	acknowledgement = true;
    autoDisconnect = false;
}

uint16 PumpController::setAddress() {
    uint8 data[HOST_ADDRESS_LENGTH];
    ByteUtils::copy((char *)data, (char *)&hostAddress[0], HOST_ADDRESS_LENGTH);
    ByteUtils::snToBytes((char *)data, HOST_ADDRESS_LENGTH);
    if (send(PumpPort::PORT_COMM,
             Global::OPERATION_SET,
             PumpComm::PARAM_ADDRESS,
             data,
             HOST_ADDRESS_LENGTH)) {
        pariOp = PumpOperation::PAIR;
        return PumpOperation::PAIR;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 PumpController::clearAddress() {
    uint8 data[6] = {0};
    if (send(PumpPort::PORT_COMM,
             Global::OPERATION_SET,
             PumpComm::PARAM_ADDRESS,
             data,
             HOST_ADDRESS_LENGTH)) {
        pariOp = PumpOperation::UNPAIR;
        return PumpOperation::UNPAIR;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 PumpController::getDeviceInfo() {
    if (send(PumpPort::PORT_SYSTEM,
             Global::OPERATION_GET,
             PumpSystem::PARAM_DEVICE,
             0,
             0)) {
        return PumpOperation::GET_DEVICE_INFO;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 PumpController::getHistory(uint16 index) {
    uint8 data[2];
    LittleEndianByteUtils::unsignedShortToBytes(index, data);
    if (send(PumpPort::PORT_MONITOR,
             Global::OPERATION_GET,
             PumpMonitor::PARAM_HISTORY,
             data,
             2)) {
        return PumpOperation::GET_HISTORY;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 PumpController::setEventConfirmed(uint16 eventIndex, uint32 event, uint8 value) {
    uint8 data[6];
    LittleEndianByteUtils::unsignedShortToBytes(eventIndex, data);
    data[2] = (event >> 16) & 0xFF;
    data[3] = (event >> 8) & 0xFF;
    data[4] = (event) & 0xFF;
    data[5] = value;
    if (send(PumpPort::PORT_MONITOR,
             Global::OPERATION_SET,
             PumpMonitor::PARAM_EVENT,
             data,
             6)) {
        return PumpOperation::SET_EVENT_CONFIRMED;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 PumpController::setDatetime(const string &dateTime) {
    int32 year;
    int32 mon;
    int32 day;
    int32 hour;
    int32 min;
    int32 sec;
    sscanf(dateTime.data(), "%4d-%2d-%2d %2d:%2d:%2d", &year, &mon, &day, &hour, &min, &sec);

    year -= PumpHistory::YEAR_2000;
    uint8 data[6] = {(uint8)year, (uint8)mon, (uint8)day, (uint8)hour, (uint8)min, (uint8)sec};
    if (send(PumpPort::PORT_MONITOR,
             Global::OPERATION_SET,
             PumpMonitor::PARAM_DATE_TIME,
             data,
             6)) {
        return PumpOperation::SET_DATETIME;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 PumpController::getMode() {
    if (send(PumpPort::PORT_DELIVERY,
             Global::OPERATION_GET,
             PumpDeliver::PARAM_MODE,
             0,
             0)) {
        return PumpOperation::GET_MODE;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 PumpController::setMode(uint32 mode) {
    uint8 data[4];
    LittleEndianByteUtils::unsignedIntToBytes(mode, data);
    if (send(PumpPort::PORT_DELIVERY,
             Global::OPERATION_SET,
             PumpDeliver::PARAM_MODE,
             data,
             4)) {
        return PumpOperation::SET_MODE;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 PumpController::getDeliveryBusy() {
    if (send(PumpPort::PORT_DELIVERY,
             Global::OPERATION_GET,
             PumpDeliver::PARAM_BUSY,
             0,
             0)) {
        return PumpOperation::GET_DELIVERY_BUSY;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 PumpController::setBasalProfile(float32 basal[]) {
    uint16 steps[BASAL_PROFILE_COUNT] = {0};
    uint8 i;
    for (i = 0; i < BASAL_PROFILE_COUNT; i++) {
        steps[i] = basalToSteps(basal[i]);
        if (steps[i] < STEP_UNIT && i < BASAL_PROFILE_COUNT - 1 && steps[i + 1] < STEP_UNIT) {
            steps[i] = basalToSteps(0);
            steps[i + 1] = basalToSteps(basal[i] + basal[i + 1]);
            i++;
        }
    }

    uint8 data[2 * BASAL_PROFILE_COUNT];
    for (i = 0; i < BASAL_PROFILE_COUNT; i++) {
        LittleEndianByteUtils::unsignedShortToBytes(steps[i], data + 2 * i);
    }
    if (send(PumpPort::PORT_DELIVERY,
             Global::OPERATION_SET,
             PumpDeliver::PARAM_PROFILE_BASAL,
             data,
             2 * BASAL_PROFILE_COUNT)) {
        return PumpOperation::SET_BASAL_PROFILE;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 PumpController::getBasalProfile() {
    if (send(PumpPort::PORT_DELIVERY,
             Global::OPERATION_GET,
             PumpDeliver::PARAM_PROFILE_BASAL,
             0,
             0)) {
        return PumpOperation::GET_BASAL_PROFILE;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 PumpController::setBolusProfile(float32 amountTotal, float32 bolusRatio, float32 amountExtended, uint32 intervalExtended) {
    uint32 u32_amountTotal = ((uint32)(amountTotal * VALUE_SCALE) + INSULIN_MIN - 1) / INSULIN_MIN * INSULIN_MIN;
    uint32 u32_amountExtended = (uint32)(amountExtended * VALUE_SCALE);
    uint32 u32_amountNow = 0;
    if (u32_amountTotal > u32_amountExtended) {
        u32_amountNow = u32_amountTotal - u32_amountExtended;
    }
    u32_amountNow = (u32_amountNow + INSULIN_MIN - 1) / INSULIN_MIN * INSULIN_MIN;
    u32_amountExtended = u32_amountTotal - u32_amountNow;
    uint32 intervalNow = (u32_amountNow + INSULIN_UNIT - 1) / INSULIN_UNIT;
    intervalNow = (uint32)((float32)intervalNow / bolusRatio);
    if (intervalNow > 1)
    {
        if ((u32_amountNow * STEP_MIN * SECONDS_PER_HOUR) / (INSULIN_MIN * intervalNow) <= BOLUS_EXTENDED_RATE_MAX) {
            intervalNow--;
        }
    }

    uint8 data[16];
    u32_amountNow = u32_amountNow * STEP_MIN / INSULIN_MIN;
    u32_amountExtended = u32_amountExtended * STEP_MIN / INSULIN_MIN;
    LittleEndianByteUtils::unsignedIntToBytes(u32_amountNow, data);
    LittleEndianByteUtils::unsignedIntToBytes(intervalNow, data + 4);
    LittleEndianByteUtils::unsignedIntToBytes(u32_amountExtended, data + 8);
    LittleEndianByteUtils::unsignedIntToBytes(intervalExtended, data + 12);
    if (send(PumpPort::PORT_DELIVERY,
             Global::OPERATION_SET,
             PumpDeliver::PARAM_PROFILE_BOLUS,
             data,
             16)) {
        return PumpOperation::SET_BOLUS_PROFILE;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 PumpController::getBolusProfile() {
    if (send(PumpPort::PORT_DELIVERY,
             Global::OPERATION_GET,
             PumpDeliver::PARAM_PROFILE_BOLUS,
             0,
             0)) {
        return PumpOperation::GET_BOLUS_PROFILE;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 PumpController::setTemporaryProfile(float32 tempBasal, uint32 interval) {
    uint32 u32_tempBasal = ((uint32)(tempBasal * VALUE_SCALE) + INSULIN_MIN - 1) / INSULIN_MIN * INSULIN_MIN;
    u32_tempBasal = (u32_tempBasal * interval) / SECONDS_PER_HOUR;
    u32_tempBasal = (u32_tempBasal * STEP_MIN + INSULIN_MIN - 1) / INSULIN_MIN;

    uint8 data[8];
    LittleEndianByteUtils::unsignedIntToBytes(u32_tempBasal, data);
    LittleEndianByteUtils::unsignedIntToBytes(interval, data + 4);
    if (send(PumpPort::PORT_DELIVERY,
             Global::OPERATION_SET,
             PumpDeliver::PARAM_PROFILE_TEMPORARY,
             data,
             8)) {
        return PumpOperation::SET_TEMPORARY_PROFILE;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 PumpController::setTemporaryPercentProfile(uint32 tempBasalPercent, uint32 interval) {
    if (tempBasalPercent > 0)
        tempBasalPercent |= 0x80000000;

    uint8 data[8];
    LittleEndianByteUtils::unsignedIntToBytes(tempBasalPercent, data);
    LittleEndianByteUtils::unsignedIntToBytes(interval, data + 4);
    if (send(PumpPort::PORT_DELIVERY,
             Global::OPERATION_SET,
             PumpDeliver::PARAM_PROFILE_TEMPORARY_PERCENT,
             data,
             8)) {
        return PumpOperation::SET_TEMPORARY_PERCENT_PROFILE;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 PumpController::getSetting() {
    uint8 data[20] = {0};
    if (send(PumpPort::PORT_DELIVERY,
             Global::OPERATION_GET,
             PumpDeliver::PARAM_SETTING,
             data,
             20)) {
        return PumpOperation::GET_SETTING;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 PumpController::setSetting(float32 value[]) {
    uint32 u32_value[PumpSetting::SETTING_COUNT] = {0};
    u32_value[PumpSetting::SETTING_INDEX_EXPIRATION_TIME] =
            (uint32)value[PumpSetting::SETTING_INDEX_EXPIRATION_TIME];
    u32_value[PumpSetting::SETTING_INDEX_AUTO_OFF_TIME] =
            (uint32)value[PumpSetting::SETTING_INDEX_AUTO_OFF_TIME];
    u32_value[PumpSetting::SETTING_INDEX_RESERVOIR_LOW_LIMIT] =
            (uint32)(value[PumpSetting::SETTING_INDEX_RESERVOIR_LOW_LIMIT] * VALUE_SCALE * STEP_MIN / INSULIN_MIN);
    u32_value[PumpSetting::SETTING_INDEX_QUICK_BOLUS_STEP] =
            (uint32)(value[PumpSetting::SETTING_INDEX_QUICK_BOLUS_STEP] * VALUE_SCALE * STEP_MIN / INSULIN_MIN);
    u32_value[PumpSetting::SETTING_INDEX_OCCLUSION_LIMIT] =
            (uint32)value[PumpSetting::SETTING_INDEX_OCCLUSION_LIMIT];
    u32_value[PumpSetting::SETTING_INDEX_UNIT_AMOUNT] =
            (uint32)(value[PumpSetting::SETTING_INDEX_UNIT_AMOUNT] * VALUE_SCALE * STEP_MIN / INSULIN_MIN);
    u32_value[PumpSetting::SETTING_INDEX_BASAL_RATE_LIMIT] =
            (uint32)(value[PumpSetting::SETTING_INDEX_BASAL_RATE_LIMIT] * VALUE_SCALE * STEP_MIN / INSULIN_MIN);
    u32_value[PumpSetting::SETTING_INDEX_BOLUS_AMOUNT_LIMIT] =
            (uint32)(value[PumpSetting::SETTING_INDEX_BOLUS_AMOUNT_LIMIT] * VALUE_SCALE * STEP_MIN / INSULIN_MIN);

    uint8 data[20];
    LittleEndianByteUtils::unsignedIntToBytes(u32_value[PumpSetting::SETTING_INDEX_EXPIRATION_TIME], data);
    LittleEndianByteUtils::unsignedIntToBytes(u32_value[PumpSetting::SETTING_INDEX_AUTO_OFF_TIME], data+4);
    LittleEndianByteUtils::unsignedShortToBytes(u32_value[PumpSetting::SETTING_INDEX_RESERVOIR_LOW_LIMIT], data+8);
    LittleEndianByteUtils::unsignedShortToBytes(u32_value[PumpSetting::SETTING_INDEX_QUICK_BOLUS_STEP], data+10);
    LittleEndianByteUtils::unsignedShortToBytes(u32_value[PumpSetting::SETTING_INDEX_OCCLUSION_LIMIT], data+12);
    LittleEndianByteUtils::unsignedShortToBytes(u32_value[PumpSetting::SETTING_INDEX_UNIT_AMOUNT], data+14);
    LittleEndianByteUtils::unsignedShortToBytes(u32_value[PumpSetting::SETTING_INDEX_BASAL_RATE_LIMIT], data+16);
    LittleEndianByteUtils::unsignedShortToBytes(u32_value[PumpSetting::SETTING_INDEX_BOLUS_AMOUNT_LIMIT], data+18);
    if (send(PumpPort::PORT_DELIVERY,
             Global::OPERATION_SET,
             PumpDeliver::PARAM_SETTING,
             data,
             20)) {
        return PumpOperation::SET_SETTING;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 PumpController::setRewinding(float32 amount) {
    uint32 u32_amout = (uint32)(amount * VALUE_SCALE) * STEP_MIN / INSULIN_MIN;
    uint8 data[4];
    LittleEndianByteUtils::unsignedIntToBytes(u32_amout, data);
    if (send(PumpPort::PORT_DELIVERY,
             Global::OPERATION_SET,
             PumpDeliver::PARAM_REWINDING,
             data,
             4)) {
        return PumpOperation::SET_REWINDING;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 PumpController::setPriming(float32 amount) {
    uint32 u32_amout = (uint32)(amount * VALUE_SCALE) * STEP_MIN / INSULIN_MIN;
    uint8 data[4];
    LittleEndianByteUtils::unsignedIntToBytes(u32_amout, data);
    if (send(PumpPort::PORT_DELIVERY,
             Global::OPERATION_SET,
             PumpDeliver::PARAM_PRIMING,
             data,
             4)) {
        return PumpOperation::SET_PRIMING;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 PumpController::setBolusRatio(uint16 multiple, uint16 division) {
    uint8 data[4];
    LittleEndianByteUtils::unsignedShortToBytes(division, data);
    LittleEndianByteUtils::unsignedShortToBytes(multiple, data+2);
    if (send(PumpPort::PORT_DELIVERY,
             Global::OPERATION_SET,
             PumpDeliver::PARAM_BOLUS_RATIO,
             data,
             4)) {
        return PumpOperation::SET_BOLUS_RATIO;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 PumpController::setCgmSn(const string &sn) {
    uint8 data[6];
    ByteUtils::copy((char *)data, sn.data(), 6);
    ByteUtils::snToBytes((char *)data, 6);
    if (send(PumpPort::PORT_COMM,
             Global::OPERATION_SET,
             PumpComm::PARAM_CGM_SN,
             data,
             6)) {
        return PumpOperation::SET_CGM_SN;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 PumpController::setAutoMode(bool isOn) {
    uint32 u32_on = isOn ? 1 : 0;
    uint8 data[4];
    LittleEndianByteUtils::unsignedIntToBytes(u32_on, data);
    if (send(PumpPort::PORT_GLUCOSE,
             Global::OPERATION_SET,
             PumpGlucose::PARAM_AUTO_MODE,
             data,
             4)) {
        return PumpOperation::SET_AUTO_MODE;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 PumpController::setGlucoseTarget(float32 targetUpper, float32 targetLower) {
    uint16 u16_targetUpper = targetUpper;
    uint16 u16_targetLower = targetLower;
    uint8 data[4];
    LittleEndianByteUtils::unsignedShortToBytes(u16_targetUpper, data);
    LittleEndianByteUtils::unsignedShortToBytes(u16_targetLower, data+2);
    if (send(PumpPort::PORT_GLUCOSE,
             Global::OPERATION_SET,
             PumpGlucose::PARAM_GLUCOSE_TARGET,
             data,
             4)) {
        return PumpOperation::SET_GLUCOSE_TARGET;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 PumpController::setIsf(float32 isf) {
    uint32 u32_isf = isf * 10000;
    uint8 data[4];
    LittleEndianByteUtils::unsignedIntToBytes(u32_isf, data);
    if (send(PumpPort::PORT_GLUCOSE,
             Global::OPERATION_SET,
             PumpGlucose::PARAM_ISF,
             data,
             4)) {
        return PumpOperation::SET_ISF;
    } else {
        return BleOperation::BUSY;
    }
}

uint16 PumpController::getOcclusion() {
    if (send(PumpPort::PORT_OCCLUSION,
             Global::OPERATION_GET,
             PumpOcclusion::PARAM_OCCLUSION,
             0,
             0)) {
        return PumpOperation::GET_OCCLUSION;
    } else {
        return BleOperation::BUSY;
    }
}

bool PumpController::handleCommand(uint8 port, uint8 op, uint8 param, const uint8 *data, uint16 length) {
    int pumpOp = PumpOperation::UNKNOWN;
    bool success = (op==Global::OPERATION_ACKNOWLEDGE) || (op==Global::OPERATION_NOTIFY);

    switch (port) {
    case PumpPort::PORT_SYSTEM:
        if (op==Global::OPERATION_NOTIFY && param==PumpSystem::PARAM_DEVICE) pumpOp = PumpOperation::GET_DEVICE_INFO;
        break;
    case PumpPort::PORT_COMM:
        if (op==Global::OPERATION_ACKNOWLEDGE && param==PumpComm::PARAM_ADDRESS && pariOp == PumpOperation::PAIR) pumpOp = PumpOperation::PAIR;
        else if (op==Global::OPERATION_ACKNOWLEDGE && param==PumpComm::PARAM_ADDRESS && pariOp == PumpOperation::UNPAIR) pumpOp = PumpOperation::UNPAIR;
        else if (op==Global::OPERATION_ACKNOWLEDGE && param==PumpComm::PARAM_CGM_SN) pumpOp = PumpOperation::SET_CGM_SN;
        break;
    case PumpPort::PORT_GLUCOSE:
        if (op==Global::OPERATION_ACKNOWLEDGE && param==PumpGlucose::PARAM_AUTO_MODE) pumpOp = PumpOperation::SET_AUTO_MODE;
        else if (op==Global::OPERATION_ACKNOWLEDGE && param==PumpGlucose::PARAM_GLUCOSE_TARGET) pumpOp = PumpOperation::SET_GLUCOSE_TARGET;
        else if (op==Global::OPERATION_ACKNOWLEDGE && param==PumpGlucose::PARAM_ISF) pumpOp = PumpOperation::SET_ISF;
        break;
    case PumpPort::PORT_DELIVERY:
        if (op==Global::OPERATION_NOTIFY && param==PumpDeliver::PARAM_MODE) pumpOp = PumpOperation::GET_MODE;
        else if (op==Global::OPERATION_ACKNOWLEDGE && param==PumpDeliver::PARAM_MODE) pumpOp = PumpOperation::SET_MODE;
        else if (op==Global::OPERATION_NOTIFY && param==PumpDeliver::PARAM_BUSY) pumpOp = PumpOperation::GET_DELIVERY_BUSY;
        else if (op==Global::OPERATION_NOTIFY && param==PumpDeliver::PARAM_PROFILE_BASAL) pumpOp = PumpOperation::GET_BASAL_PROFILE;
        else if (op==Global::OPERATION_ACKNOWLEDGE && param==PumpDeliver::PARAM_PROFILE_BASAL) pumpOp = PumpOperation::SET_BASAL_PROFILE;
        else if (op==Global::OPERATION_NOTIFY && param==PumpDeliver::PARAM_PROFILE_BOLUS) pumpOp = PumpOperation::GET_BOLUS_PROFILE;
        else if (op==Global::OPERATION_ACKNOWLEDGE && param==PumpDeliver::PARAM_PROFILE_BOLUS) pumpOp = PumpOperation::SET_BOLUS_PROFILE;
        else if (op==Global::OPERATION_ACKNOWLEDGE && param==PumpDeliver::PARAM_PROFILE_TEMPORARY) pumpOp = PumpOperation::SET_TEMPORARY_PROFILE;
        else if (op==Global::OPERATION_ACKNOWLEDGE && param==PumpDeliver::PARAM_PROFILE_TEMPORARY_PERCENT) pumpOp = PumpOperation::SET_TEMPORARY_PERCENT_PROFILE;
        else if (op==Global::OPERATION_NOTIFY && param==PumpDeliver::PARAM_SETTING) pumpOp = PumpOperation::GET_SETTING;
        else if (op==Global::OPERATION_ACKNOWLEDGE && param==PumpDeliver::PARAM_SETTING) pumpOp = PumpOperation::SET_SETTING;
        else if (op==Global::OPERATION_ACKNOWLEDGE && param==PumpDeliver::PARAM_REWINDING) pumpOp = PumpOperation::SET_REWINDING;
        else if (op==Global::OPERATION_ACKNOWLEDGE && param==PumpDeliver::PARAM_PRIMING) pumpOp = PumpOperation::SET_PRIMING;
        else if (op==Global::OPERATION_ACKNOWLEDGE && param==PumpDeliver::PARAM_BOLUS_RATIO) pumpOp = PumpOperation::SET_BOLUS_RATIO;
        break;
    case PumpPort::PORT_MONITOR:
        if (op==Global::OPERATION_NOTIFY && param==PumpMonitor::PARAM_HISTORY) pumpOp = PumpOperation::GET_HISTORY;
        else if (op==Global::OPERATION_ACKNOWLEDGE && param==PumpMonitor::PARAM_EVENT) pumpOp = PumpOperation::SET_EVENT_CONFIRMED;
        else if (op==Global::OPERATION_ACKNOWLEDGE && param==PumpMonitor::PARAM_DATE_TIME) pumpOp = PumpOperation::SET_DATETIME;
        break;
    case PumpPort::PORT_OCCLUSION:
        if (op==Global::OPERATION_NOTIFY && param==PumpOcclusion::PARAM_OCCLUSION) pumpOp = PumpOperation::GET_OCCLUSION;
        break;
    default:
        break;
    }

    onReceive(pumpOp, success, data, length);

    return success;
}

void PumpController::onReceive(uint16 op, bool success, const uint8 *data, uint16 length) {
    BleController::onReceive(op, success, data, length);
}

uint16 PumpController::basalToSteps(float32 basal)
{
    return ((uint32)(basal * VALUE_SCALE) * STEP_MIN) / (INSULIN_MIN *(BASAL_PROFILE_COUNT / HOURS_PER_DAY));
}
