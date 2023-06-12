package com.microtech.aidexx.ui.main.event.viewmodels

import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.data.EventUnitManager
import com.microtech.aidexx.db.entity.event.MedicationDetail
import com.microtech.aidexx.db.entity.event.MedicationEntity
import com.microtech.aidexx.db.entity.event.MedicationEntity_
import com.microtech.aidexx.db.entity.event.preset.MedicinePresetEntity
import com.microtech.aidexx.db.repository.EventDbRepository
import com.microtech.aidexx.ui.main.event.TYPE_SLOT_MEDICINE

class MedicineViewModel
    : BaseEventViewModel<MedicationEntity, MedicationDetail, MedicinePresetEntity>() {

    override fun getEventSlotType(): Int = TYPE_SLOT_MEDICINE

    override suspend fun queryPresetByName(name: String): List<MedicinePresetEntity> =
        EventDbRepository.queryMedicinePresetByName(name)

    override suspend fun genNewPreset(name: String): MedicinePresetEntity =
        MedicinePresetEntity().also {
            it.name = name
            it.isUserInputType = true
        }

    override suspend fun getDetailHistory(): List<MedicationDetail> {
        val entityList = EventDbRepository.queryHistory(MedicationEntity_.timestamp)

        return entityList?.let {
            it.flatMap { me ->
                me.expandList
            }
        } ?: listOf()
    }

    override suspend fun genEventEntityWhenSave(): MedicationEntity {
        val medicationEntity = MedicationEntity()
        medicationEntity.uploadState = 1
        medicationEntity.takenTime = eventTime
        medicationEntity.expandList.addAll(toSaveDetailList.map {
            it.unitStr = EventUnitManager.getMedicationUnit(it.unit)
            it.medicationId = medicationEntity.medicationId
            it
        })

        medicationEntity.moment = eventMomentTypeIndex

        // 优先取最后一个输入的自定义药物 没有的话取最后一个系统预设用药
        val lastDetail = toSaveDetailList.sortedBy {
            it.presetType
        }.last()
        medicationEntity.medicineName = lastDetail.name
        medicationEntity.medicineDosage = lastDetail.quantity.toFloat()

        medicationEntity.userId = UserInfoManager.instance().userId()

        return medicationEntity
    }



}