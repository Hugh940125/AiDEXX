package com.microtech.aidexx.data

import com.microtech.aidexx.db.entity.event.OthersEntity
import com.microtech.aidexx.db.repository.EventDbRepository

object CloudOthersHistorySync: EventHistorySync<OthersEntity>() {

    override suspend fun getNeedUploadData(): MutableList<OthersEntity>? =
        EventDbRepository.getOthersNeedUploadEvent()

}