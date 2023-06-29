package com.microtech.aidexx.utils

import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.dao.CalibrateDao
import com.microtech.aidexx.db.entity.CalibrateEntity
import com.microtech.aidexx.db.repository.CgmCalibBgRepository


object CalibrateManager {

    suspend fun getCalibrateHistorys(): MutableList<CalibrateEntity> {

        val mutableListOf = mutableListOf<CalibrateEntity>()

        val uid = UserInfoManager.shareUserInfo?.dataProviderId ?: UserInfoManager.instance().userId()

        val calListFromHistory = CgmCalibBgRepository.queryAllCgm(uid) ?: mutableListOf()

        for (history in calListFromHistory) {
            val calibrateEntity = CalibrateEntity()
            calibrateEntity.deviceId = history.deviceId ?: ""
            calibrateEntity.calTime = history.deviceTime
            calibrateEntity.sensorIndex = history.sensorIndex
            calibrateEntity.referenceGlucose = history.glucose ?: 0f
            mutableListOf.add(calibrateEntity)
        }
        val calList = CalibrateDao.queryByUid(uid) ?: mutableListOf()
        mutableListOf.addAll(calList)
        return mutableListOf
    }




    fun calCalibrate(sensorGlucose: Float, referenceGlucose: Float): FloatArray {

        val CAL_FACTOR_LOWER_SCALE = 0.8;
        val CAL_FACTOR_UPPER_SCALE = 1.2;

        var offset = 0f
        var calFactor = referenceGlucose / sensorGlucose;

        if (calFactor > 1) {
            var start = CAL_FACTOR_UPPER_SCALE;
            val range = (start - 1) / 2;
            val nonlinear = 2 / range;
            start -= range
            if (calFactor <= start) {
                return floatArrayOf(calFactor, offset);
            }
            calFactor =
                (2 * range / (1 + Math.exp((start - calFactor) * nonlinear)) + start - range).toFloat();
            offset = referenceGlucose / calFactor - sensorGlucose;
        } else {
            var start = CAL_FACTOR_LOWER_SCALE
            val range = (1 - start) / 2
            val nonlinear = 2 / range
            start += range
            if (calFactor >= start) {
                return floatArrayOf(calFactor, offset)
            }
            calFactor =
                (2 * range / (1 + Math.exp((start - calFactor) * nonlinear)) + start - range).toFloat();
            offset = referenceGlucose / calFactor - sensorGlucose;
        }
        LogUtils.data("calibrate calFactor:${calFactor} ,offset:${offset} ,sensorGlucose $sensorGlucose ,sreferenceGlucose ${referenceGlucose}")
        return floatArrayOf(calFactor, offset)
    }

}