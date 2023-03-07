#include "cgmdefaultparamparser.h"

const CgmDefaultParamEntity *CgmDefaultParamParser::getCgmDefaultParam()
{
    try {
        parse();
    } catch (...) {
        LOGE("DefaultParam Parse Error");
    }
    return &defaultParam;
}

void CgmDefaultParamParser::parse() {
    value[CgmDefaultParam::DP_EXPIRATION_TIME] = (float)ibs->readInt() / 86400.0;
    int i;
    for(i = CgmDefaultParam::DP_CAL_FACTOR_DEFAULT; i <= CgmDefaultParam::DP_REF_IMAG_SENS_CHANGED; i++)
    {
        value[i] = (float)ibs->readShort() / 100.0;
    }

    defaultParam.et     = value[CgmDefaultParam::DP_EXPIRATION_TIME];
    defaultParam.cf     = value[CgmDefaultParam::DP_CAL_FACTOR_DEFAULT];
    defaultParam.ofs    = value[CgmDefaultParam::DP_OFFSET_DEFAULT];
    defaultParam.cf1    = value[CgmDefaultParam::DP_CAL_FACTOR_1];
    defaultParam.cf2    = value[CgmDefaultParam::DP_CAL_FACTOR_2];
    defaultParam.cf3    = value[CgmDefaultParam::DP_CAL_FACTOR_3];
    defaultParam.cf4    = value[CgmDefaultParam::DP_CAL_FACTOR_4];
    defaultParam.cf5    = value[CgmDefaultParam::DP_CAL_FACTOR_5];
    defaultParam.cfh2   = value[CgmDefaultParam::DP_CAL_FACTOR_HOURS2];
    defaultParam.cfh3   = value[CgmDefaultParam::DP_CAL_FACTOR_HOURS3];
    defaultParam.cfh4   = value[CgmDefaultParam::DP_CAL_FACTOR_HOURS4];
    defaultParam.ofs1   = value[CgmDefaultParam::DP_OFFSET_1];
    defaultParam.ofs2   = value[CgmDefaultParam::DP_OFFSET_2];
    defaultParam.ofs3   = value[CgmDefaultParam::DP_OFFSET_3];
    defaultParam.ofs4   = value[CgmDefaultParam::DP_OFFSET_4];
    defaultParam.ofs5   = value[CgmDefaultParam::DP_OFFSET_5];
    defaultParam.ofsh2  = value[CgmDefaultParam::DP_OFFSET_HOURS2];
    defaultParam.ofsh3  = value[CgmDefaultParam::DP_OFFSET_HOURS3];
    defaultParam.ofsh4  = value[CgmDefaultParam::DP_OFFSET_HOURS4];
    defaultParam.ib     = value[CgmDefaultParam::DP_INITIALIZAION_BIAS];
    defaultParam.ird    = value[CgmDefaultParam::DP_ISIG_REF_DEFAULT];
    defaultParam.inl1   = value[CgmDefaultParam::DP_ISIG_NONLINEAR_C1];
    defaultParam.inl0   = value[CgmDefaultParam::DP_ISIG_NONLINEAR_C0];
    defaultParam.cfls   = value[CgmDefaultParam::DP_CAL_FACTOR_LOWER_SCALE];
    defaultParam.cfus   = value[CgmDefaultParam::DP_CAL_FACTOR_UPPER_SCALE];
    defaultParam.sfl    = value[CgmDefaultParam::DP_SENS_FACTOR_LOWER];
    defaultParam.sfu    = value[CgmDefaultParam::DP_SENS_FACTOR_UPPER];
    defaultParam.rl     = value[CgmDefaultParam::DP_REF_REAL_LOWER];
    defaultParam.ru     = value[CgmDefaultParam::DP_REF_REAL_UPPER];
    defaultParam.rns    = value[CgmDefaultParam::DP_REF_REAL_NEW_SENSOR];
    defaultParam.rr     = value[CgmDefaultParam::DP_REF_REAL_DEFAULT];
    defaultParam.rrf    = value[CgmDefaultParam::DP_REF_REAL_FACTOR];
    defaultParam.rrcsh  = value[CgmDefaultParam::DP_REF_REAL_CHANGE_START_HOUR];
    defaultParam.rrcph  = value[CgmDefaultParam::DP_REF_REAL_CHANGE_PER_HOUR];
    defaultParam.rrsc   = value[CgmDefaultParam::DP_REF_REAL_SENS_CHANGED];
    defaultParam.il     = value[CgmDefaultParam::DP_REF_IMAG_LOWER];
    defaultParam.iu     = value[CgmDefaultParam::DP_REF_IMAG_UPPER];
    defaultParam.ir     = value[CgmDefaultParam::DP_REF_IMAG_DEFAULT];
    defaultParam.irf    = value[CgmDefaultParam::DP_REF_IMAG_FACTOR];
    defaultParam.irsc   = value[CgmDefaultParam::DP_REF_IMAG_SENS_CHANGED];
    defaultParam.irofs  = value[CgmDefaultParam::DP_REF_IMAG_OFFSET];
}
