package com.microtech.aidexx.ui.main.history

import androidx.annotation.StringRes
import com.microtech.aidexx.R
import com.microtech.aidexx.common.getContext
import com.microtech.aidexx.common.stripTrailingZeros
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.entity.BloodGlucoseEntity
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import com.microtech.aidexx.db.entity.event.DietEntity
import com.microtech.aidexx.db.entity.event.ExerciseEntity
import com.microtech.aidexx.db.entity.event.InsulinEntity
import com.microtech.aidexx.db.entity.event.MedicationEntity
import com.microtech.aidexx.db.entity.event.OthersEntity
import com.microtechmd.blecomm.constant.History

private fun str(@StringRes id: Int) = getContext().getString(id)
private fun getDeletable(): Boolean {
    return UserInfoManager.shareUserInfo == null
}
fun DietEntity.toHistoryDetailModel(): HistoryDetailModel {
    val historyDetailModel = HistoryDetailModel(clazz = this.javaClass)
    historyDetailModel.idForRealEntity = idx
    historyDetailModel.time = timestamp
    historyDetailModel.resourceId = R.drawable.ic_have_food
    historyDetailModel.deletable = getDeletable()
    if (expandList.isNotEmpty()) {
        var fat = 0.0
        var protein = 0.0
        var carbohydrate = 0.0

        val unitStr = str(R.string.unit_g)
        val splitStr = ": "

        expandList.forEach { dietDetailEntity ->
            fat += dietDetailEntity.fat
            protein += dietDetailEntity.protein
            carbohydrate += dietDetailEntity.carbohydrate
        }

        historyDetailModel.contentList.add(
            buildString {
                append(str(R.string.carb))
                append(splitStr)
                append(carbohydrate.stripTrailingZeros(3))
                append(unitStr)
            }
        )
        historyDetailModel.contentList.add(
            buildString {
                append(str(R.string.protein))
                append(splitStr)
                append( protein.stripTrailingZeros(3))
                append(unitStr)
            }
        )
        historyDetailModel.contentList.add(
            buildString {
                append(str(R.string.fat))
                append(splitStr)
                append(fat.stripTrailingZeros(3))
                append(unitStr)
            }
        )
    }
    historyDetailModel.title = getEventDescription(getContext().resources)
    return historyDetailModel
}
fun ExerciseEntity.toHistoryDetailModel(): HistoryDetailModel {
    val historyDetailModel = HistoryDetailModel(clazz = this.javaClass)
    historyDetailModel.idForRealEntity = idx
    historyDetailModel.time = timestamp
    historyDetailModel.resourceId = R.drawable.ic_excise
    historyDetailModel.title = getEventDescription(getContext().resources)
    historyDetailModel.deletable = getDeletable()
    return historyDetailModel
}
fun MedicationEntity.toHistoryDetailModel(): HistoryDetailModel {
    val historyDetailModel = HistoryDetailModel(clazz = this.javaClass)
    historyDetailModel.idForRealEntity = idx
    historyDetailModel.time = timestamp
    historyDetailModel.resourceId = R.drawable.ic_use_medical
    historyDetailModel.title = getEventDescription(getContext().resources)
    historyDetailModel.deletable = getDeletable()
    return historyDetailModel
}
fun InsulinEntity.toHistoryDetailModel(): HistoryDetailModel {
    val historyDetailModel = HistoryDetailModel(clazz = this.javaClass)
    historyDetailModel.idForRealEntity = idx
    historyDetailModel.resourceId = R.drawable.ic_yds
    historyDetailModel.time = timestamp
    historyDetailModel.title = getEventDescription(getContext().resources)
    historyDetailModel.deletable = getDeletable()
    return historyDetailModel
}
fun OthersEntity.toHistoryDetailModel(): HistoryDetailModel {
    val historyDetailModel = HistoryDetailModel(clazz = this.javaClass)
    historyDetailModel.idForRealEntity = idx
    historyDetailModel.time = timestamp
    historyDetailModel.resourceId = R.drawable.ic_other_mark
    historyDetailModel.title = content
    historyDetailModel.deletable = getDeletable()
    return historyDetailModel
}
fun RealCgmHistoryEntity.toHistoryDetailModel(): HistoryDetailModel {
    val historyDetailModel = HistoryDetailModel(clazz = this.javaClass)
    historyDetailModel.idForRealEntity = idx
    historyDetailModel.time = timestamp
    historyDetailModel.deletable = false
    when (getHighOrLowGlucoseType()) {
        History.HISTORY_LOCAL_HYPER -> {
            historyDetailModel.resourceId = R.drawable.ic_yellow_alert
        }
        History.HISTORY_LOCAL_HYPO, History.HISTORY_LOCAL_URGENT_HYPO -> {
            historyDetailModel.resourceId = R.drawable.ic_red_alert
        }
    }
    historyDetailModel.title = getEventDescription(getContext().resources)
    return historyDetailModel
}
fun BloodGlucoseEntity.toHistoryDetailModel(): HistoryDetailModel {
    val historyDetailModel = HistoryDetailModel(clazz = this.javaClass)
    historyDetailModel.idForRealEntity = idx
    historyDetailModel.time = timestamp
    historyDetailModel.resourceId = R.drawable.ic_bg_cal
    historyDetailModel.deletable = getDeletable()
    historyDetailModel.title =
        buildString {
            append( getEventDescription(getContext().resources))
            append(" ")
            append(getValueDescription(getContext().resources))
        }
    return historyDetailModel
}