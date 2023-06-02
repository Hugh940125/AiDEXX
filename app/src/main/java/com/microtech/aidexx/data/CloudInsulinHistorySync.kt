package com.microtech.aidexx.data

import com.microtech.aidexx.db.entity.event.InsulinEntity
import com.microtech.aidexx.db.repository.EventDbRepository

object CloudInsulinHistorySync: EventHistorySync<InsulinEntity>() {

    override suspend fun getNeedUploadData(): MutableList<InsulinEntity>? =
        EventDbRepository.getInsulinNeedUploadEvent()

}