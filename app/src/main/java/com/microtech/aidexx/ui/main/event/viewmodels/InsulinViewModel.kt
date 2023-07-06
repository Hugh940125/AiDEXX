package com.microtech.aidexx.ui.main.event.viewmodels

import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.entity.event.InsulinDetail
import com.microtech.aidexx.db.entity.event.InsulinEntity
import com.microtech.aidexx.db.entity.event.InsulinEntity_
import com.microtech.aidexx.db.entity.event.preset.InsulinPresetEntity
import com.microtech.aidexx.db.repository.EventDbRepository
import com.microtech.aidexx.ui.main.event.TYPE_SLOT_INSULIN

class InsulinViewModel
    : BaseEventViewModel<InsulinEntity, InsulinDetail, InsulinPresetEntity>() {

    override fun getEventSlotType(): Int = TYPE_SLOT_INSULIN

    override suspend fun queryPresetByName(name: String): List<InsulinPresetEntity> =
        EventDbRepository.queryInsulinPresetByName(name)

    override suspend fun genNewPreset(name: String): InsulinPresetEntity =
        InsulinPresetEntity().also {
            it.name = name
            it.isUserInputType = true
        }

    override suspend fun getDetailHistory(): List<InsulinDetail> {
        val entityList = EventDbRepository.queryHistory(InsulinEntity_.timestamp)

        return entityList?.let {
            it.flatMap { me ->
                me.expandList
            }
        } ?: listOf()
    }

    override suspend fun genEventEntityWhenSave(): InsulinEntity {
        val insulinEntity = InsulinEntity()
        insulinEntity.uploadState = 1
        insulinEntity.expandList.addAll(toSaveDetailList.map {
            it.unitStr = "U"
            it.insulinId = insulinEntity.insulinId
            it
        })
        insulinEntity.injectionTime = eventTime
        insulinEntity.moment = eventMomentTypeIndex
        insulinEntity.userId = UserInfoManager.instance().userId()
        insulinEntity.insulinDosage = toSaveDetailList.last().quantity.toFloat()
        insulinEntity.insulinName = toSaveDetailList.last().name
        return insulinEntity
    }



}