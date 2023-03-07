#include "pumpsettingparser.h"
#include "../../util/inputbytestream.h"
#include "../../controller/pump/pumpcontroller.h"


const float32 *PumpSettingParser::getSetting() {
    try {
        parse();
    } catch (...) {
        LOGE("Setting Parse Error");
    }
    return value;
}

void PumpSettingParser::parse() {
    value[PumpSetting::SETTING_INDEX_EXPIRATION_TIME] = ibs->readUnsignedInt();
    value[PumpSetting::SETTING_INDEX_AUTO_OFF_TIME] = ibs->readUnsignedInt();
    value[PumpSetting::SETTING_INDEX_RESERVOIR_LOW_LIMIT] = (float32)(ibs->readUnsignedShort()) / PumpController::VALUE_SCALE;
    value[PumpSetting::SETTING_INDEX_QUICK_BOLUS_STEP] = (float32)(ibs->readUnsignedShort()) / PumpController::VALUE_SCALE;
    value[PumpSetting::SETTING_INDEX_OCCLUSION_LIMIT] = ibs->readUnsignedShort();
    value[PumpSetting::SETTING_INDEX_UNIT_AMOUNT] = (float32)(ibs->readUnsignedShort()) / PumpController::VALUE_SCALE;
    value[PumpSetting::SETTING_INDEX_BASAL_RATE_LIMIT] = (float32)(ibs->readUnsignedShort()) / PumpController::VALUE_SCALE;
    value[PumpSetting::SETTING_INDEX_BOLUS_AMOUNT_LIMIT] = (float32)(ibs->readUnsignedShort()) / PumpController::VALUE_SCALE;
}
