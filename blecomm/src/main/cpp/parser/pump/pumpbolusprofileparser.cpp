#include "pumpbolusprofileparser.h"
#include "../../util/inputbytestream.h"
#include "../../controller/pump/pumpcontroller.h"
#include "../../constant/pump/pumpconstants.h"

static const uint32 HOURS_PER_DAY = 24;

const float32 *PumpBolusProfileParser::getBolusProfile() {
    try {
        parse();
    } catch (...) {
        LOGE("Setting Parse Error");
    }
    return value;
}

void PumpBolusProfileParser::parse() {
    value[PumpBolus::BOLUS_AMOUNT_NOW] =
            (float32)(ibs->readUnsignedInt()) * PumpController::INSULIN_MIN / PumpController::STEP_MIN / PumpController::VALUE_SCALE;
    value[PumpBolus::BOLUS_INTERVAL_NOW] =
            (float32)(ibs->readUnsignedInt());
    value[PumpBolus::BOLUS_AMOUNT_EXTENDED] =
            (float32)(ibs->readUnsignedInt()) * PumpController::INSULIN_MIN / PumpController::STEP_MIN / PumpController::VALUE_SCALE;
    value[PumpBolus::BOLUS_INTERVAL_EXTENDED] =
            (float32)(ibs->readUnsignedInt());

}
