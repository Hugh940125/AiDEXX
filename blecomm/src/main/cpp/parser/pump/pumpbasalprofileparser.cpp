#include "pumpbasalprofileparser.h"
#include "../../util/inputbytestream.h"
#include "../../controller/pump/pumpcontroller.h"

static const uint32 HOURS_PER_DAY = 24;

const float32 *PumpBasalProfileParser::getBasalProfile() {
    try {
        parse();
    } catch (...) {
        LOGE("Setting Parse Error");
    }
    return value;
}

void PumpBasalProfileParser::parse() {
    uint32 i;
    for(i = 0; i < PumpController::BASAL_PROFILE_COUNT; i++) {
        value[i] = ibs->readUnsignedShort();
        value[i] = ((float32)value[i]) *(PumpController::BASAL_PROFILE_COUNT / HOURS_PER_DAY) * PumpController::INSULIN_MIN / PumpController::STEP_MIN / PumpController::VALUE_SCALE;
    }
}
