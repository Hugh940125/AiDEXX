package com.microtech.aidexx.utils

import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.repository.DbRepository
import com.microtech.aidexx.db.dao.CalerateDao
import com.microtech.aidexx.db.entity.CalerateEntity


object CalibrateManager {

    suspend fun getCalibrateHistorys(): MutableList<CalerateEntity> {

        val mutableListOf = mutableListOf<CalerateEntity>()

        val uid = UserInfoManager.shareUserInfo?.id ?: UserInfoManager.instance().userId()

        val calListFromHistory = DbRepository.queryCgmByUid(uid) ?: mutableListOf()

        for (history in calListFromHistory) {
            val calerateEntity = CalerateEntity()
            calerateEntity.deviceId = history.deviceId ?: ""
            calerateEntity.calTime = history.deviceTime
            calerateEntity.sensorIndex = history.sensorIndex
            calerateEntity.referenceGlucose = history.eventData ?: 0f
            mutableListOf.add(calerateEntity)
        }

        val calList = CalerateDao.queryByUid(uid) ?: mutableListOf()

        LogUtils.eAiDex("-----2|||$mutableListOf")
        mutableListOf.addAll(calList)
        LogUtils.eAiDex("-----|||$mutableListOf")
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