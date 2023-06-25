package com.microtech.aidexx.ui.main.event.viewmodels

import com.microtech.aidexx.common.stripTrailingZerosWithoutPointer
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.data.resource.EventUnitManager
import com.microtech.aidexx.db.entity.event.DietDetail
import com.microtech.aidexx.db.entity.event.DietEntity
import com.microtech.aidexx.db.entity.event.DietEntity_
import com.microtech.aidexx.db.entity.event.preset.DietPresetEntity
import com.microtech.aidexx.db.repository.EventDbRepository
import com.microtech.aidexx.ui.main.event.TYPE_SLOT_DIET
import kotlin.math.roundToInt

class DietViewModel
    : BaseEventViewModel<DietEntity, DietDetail, DietPresetEntity>() {

    override fun getEventSlotType(): Int = TYPE_SLOT_DIET

    override suspend fun queryPresetByName(name: String): List<DietPresetEntity> =
        EventDbRepository.queryDietPresetByName(name)

    override suspend fun genNewPreset(name: String): DietPresetEntity =
        DietPresetEntity().also {
            it.name = name
            it.isUserInputType = true
        }

    override suspend fun getDetailHistory(): List<DietDetail> {
        val entityList = EventDbRepository.queryHistory(DietEntity_.timestamp)

        return entityList?.let {
            it.flatMap { me ->
                me.expandList
            }
        } ?: listOf()
    }

    override suspend fun genEventEntityWhenSave(): DietEntity {
        val dietEntity = DietEntity()
        dietEntity.uploadState = 1
        toSaveDetailList.forEach {
            setScale(it)
            it.unitStr = EventUnitManager.getDietUnit(it.unit)
            it.foodId = dietEntity.foodId
        }
        dietEntity.expandList.addAll(toSaveDetailList)
        dietEntity.mealTime = eventTime
        dietEntity.moment = eventMomentTypeIndex
        dietEntity.mealRemark = toSaveDetailList.last().name
        dietEntity.carbohydrate = toSaveDetailList.last().quantity.roundToInt()
        dietEntity.userId = UserInfoManager.instance().userId()

        return dietEntity
    }

    fun setScale(dietDetailEntity: DietDetail) {
        dietDetailEntity.carbohydrate =
            dietDetailEntity.carbohydrate.stripTrailingZerosWithoutPointer(3).toDoubleOrNull()
                ?: 0.0
        dietDetailEntity.fat =
            dietDetailEntity.fat.stripTrailingZerosWithoutPointer(3).toDoubleOrNull() ?: 0.0
        dietDetailEntity.protein =
            dietDetailEntity.protein.stripTrailingZerosWithoutPointer(3).toDoubleOrNull() ?: 0.0
    }



}