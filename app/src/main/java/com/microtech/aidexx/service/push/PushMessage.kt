package com.microtech.aidexx.service.push

import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.microtech.aidexx.common.getContext
import com.microtech.aidexx.db.entity.BloodGlucoseEntity
import com.microtech.aidexx.db.entity.event.DietEntity
import com.microtech.aidexx.db.entity.event.ExerciseEntity
import com.microtech.aidexx.db.entity.event.InsulinEntity
import com.microtech.aidexx.db.entity.event.MedicationEntity
import com.microtech.aidexx.db.entity.event.OthersEntity
import com.microtech.aidexx.db.repository.EventDbRepository
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.permission.PermissionGroups

open class PushMessage {
    protected val TAG = PushMessage::class.java.simpleName
    //taskid和messageid字段，是用于回执上报的必要参数。详情见下方文档“6.2 上报透传消息的展示和点击数据”
    var taskId: String? = null
    var messageId: String? = null

    //-1 cgm无效数据 0-cgm数据(暂不使用) 1高血糖 2-低血糖，3-紧急低
    // 4-饮食事件变更，5运动事件变更，6用药事件变更，7德岛素事件变更，8-其他事件变更，
    // 9-指血数据变更，10-日志上报
    protected val msgType: Int? = null
    val detail: String? = null
    fun getRealMsgByType(): PushMessage? {
        val gson = Gson()
        return kotlin.runCatching {
            when (msgType) {
                1,2,3 -> PushCgmMsg()
                4 -> PushDietMsg(gson.fromJson(detail, DietEntity::class.java))
                5 -> PushExerciseMsg(gson.fromJson(detail, ExerciseEntity::class.java))
                6 -> PushMedicationMsg(gson.fromJson(detail, MedicationEntity::class.java))
                7 -> PushInsulinMsg(gson.fromJson(detail, InsulinEntity::class.java))
                8 -> PushOtherMsg(gson.fromJson(detail, OthersEntity::class.java))
                9 -> PushBgMsg(gson.fromJson(detail, BloodGlucoseEntity::class.java))
                10 -> PushLogMsg()
                else -> null
            }
        }.getOrNull()
    }

    open suspend fun applyMsg(): Boolean = false
}

data class PushDietMsg(
    val data: DietEntity?
): PushMessage() {
    override suspend fun applyMsg(): Boolean {
        return data?.foodId?.let {
            EventDbRepository.removeEventByFrontId(it, DietEntity::class.java) == null
        } ?: false
    }
}
data class PushExerciseMsg(
    val data: ExerciseEntity?
): PushMessage() {
    override suspend fun applyMsg(): Boolean {
        return data?.exerciseId?.let {
            EventDbRepository.removeEventByFrontId(it, ExerciseEntity::class.java) == null
        } ?: false
    }
}
data class PushMedicationMsg(
    val data: MedicationEntity?
): PushMessage() {
    override suspend fun applyMsg(): Boolean {
        return data?.medicationId?.let {
            EventDbRepository.removeEventByFrontId(it, MedicationEntity::class.java) == null
        } ?: false
    }
}
data class PushInsulinMsg(
    val data: InsulinEntity?
): PushMessage() {
    override suspend fun applyMsg(): Boolean {
        return data?.insulinId?.let {
            EventDbRepository.removeEventByFrontId(it, InsulinEntity::class.java) == null
        } ?: false
    }
}
data class PushOtherMsg(
    val data: OthersEntity?
): PushMessage() {
    override suspend fun applyMsg(): Boolean {
        return data?.otherId?.let {
            EventDbRepository.removeEventByFrontId(it, OthersEntity::class.java) == null
        } ?: false
    }
}
data class PushBgMsg(
    val data: BloodGlucoseEntity?
): PushMessage() {
    override suspend fun applyMsg(): Boolean {
        return data?.bloodGlucoseId?.let {
            EventDbRepository.removeEventByFrontId(it, BloodGlucoseEntity::class.java) == null
        } ?: false
    }
}
class PushLogMsg: PushMessage() {
    override suspend fun applyMsg(): Boolean {
        var hasPermission = true
        val permissions = PermissionGroups.Storage
        for (permission in permissions) {
            val rl = ContextCompat.checkSelfPermission(getContext(), permission)
            if (rl == PackageManager.PERMISSION_DENIED) {
                hasPermission = false
                LogUtil.xLogE("用户未授权文件读写权限", TAG)
                break
            }
        }
        if (hasPermission) {
            com.tencent.mars.xlog.Log.appenderFlushSync(true)
            LogUtil.uploadLog(mute = true)
        }
        return hasPermission
    }
}
class PushCgmMsg: PushMessage() {
    override suspend fun applyMsg(): Boolean {
        //-1 cgm无效数据 0-cgm数据(暂不使用) 1高血糖 2-低血糖，3-紧急低
        return when (msgType) {
            1 -> false
            2 -> false
            3 -> false
            else -> false
        }
    }
}