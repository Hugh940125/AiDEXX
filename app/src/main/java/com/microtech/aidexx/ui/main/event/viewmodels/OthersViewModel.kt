package com.microtech.aidexx.ui.main.event.viewmodels

import com.microtech.aidexx.common.getMutableListType
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.entity.event.BaseEventDetail
import com.microtech.aidexx.db.entity.event.OthersEntity
import com.microtech.aidexx.db.entity.event.OthersEntity_
import com.microtech.aidexx.db.entity.event.preset.BasePresetEntity
import com.microtech.aidexx.db.repository.EventDbRepository
import java.lang.reflect.Type

class OthersViewModel
    : BaseEventViewModel<OthersEntity, BaseEventDetail, BasePresetEntity>() {
    override suspend fun queryPresetByName(name: String): List<BasePresetEntity> = listOf()

    var content: String = ""

    override fun getEventSlotType(): Int? = null

    override suspend fun genNewPreset(name: String): BasePresetEntity = BasePresetEntity()

    override suspend fun getDetailHistory(): List<BaseEventDetail> {
        val entityList = EventDbRepository.queryHistory(OthersEntity_.timestamp)

        return entityList?.let {
            it.map {  oe ->
                object : BaseEventDetail(){
                    override fun getCurrClassMutableListType(): Type = getMutableListType<BaseEventDetail>()
                    override fun getEventDesc(splitter: String?): String = name
                }.apply {
                    name = oe.content
                }
            }
        } ?: listOf()
    }

    override suspend fun genEventEntityWhenSave(): OthersEntity {
        val othersEntity = OthersEntity()
        othersEntity.uploadState = 1
        othersEntity.content = content
        othersEntity.userId = UserInfoManager.instance().userId()
        othersEntity.setTimeInfo(eventTime)
        return othersEntity
    }



}