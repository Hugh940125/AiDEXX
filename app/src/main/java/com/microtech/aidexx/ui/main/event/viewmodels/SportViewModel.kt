package com.microtech.aidexx.ui.main.event.viewmodels

import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.data.resource.EventUnitManager
import com.microtech.aidexx.db.entity.event.ExerciseDetail
import com.microtech.aidexx.db.entity.event.ExerciseEntity
import com.microtech.aidexx.db.entity.event.ExerciseEntity_
import com.microtech.aidexx.db.entity.event.preset.SportPresetEntity
import com.microtech.aidexx.db.repository.EventDbRepository
import kotlin.math.roundToInt

class SportViewModel
    : BaseEventViewModel<ExerciseEntity, ExerciseDetail, SportPresetEntity>() {

    override fun getEventSlotType(): Int? = null

    override suspend fun queryPresetByName(name: String): List<SportPresetEntity> =
        EventDbRepository.querySportPresetByName(name)

    override suspend fun genNewPreset(name: String): SportPresetEntity =
        SportPresetEntity().also {
            it.name = name
            it.isUserInputType = true
        }

    override suspend fun getDetailHistory(): List<ExerciseDetail> {
        val entityList = EventDbRepository.queryHistory(ExerciseEntity_.timestamp)

        return entityList?.let {
            it.flatMap { me ->
                me.expandList
            }
        } ?: listOf()
    }

    override suspend fun genEventEntityWhenSave(): ExerciseEntity {
        val sportEntity = ExerciseEntity().also {
            it.uploadState = 1
            it.startTime = eventTime

            toSaveDetailList.forEach { d ->
                d.unitStr = EventUnitManager.getTimeUnit(d.unit)
                d.exerciseId = it.exerciseId
            }
            it.expandList.addAll(toSaveDetailList)
            it.userId = UserInfoManager.instance().userId()
            it.duration = toSaveDetailList.last().quantity.roundToInt()
            it.intensity = 1
        }

        return sportEntity
    }



}