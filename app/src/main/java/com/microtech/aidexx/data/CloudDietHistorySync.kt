package com.microtech.aidexx.data

import com.microtech.aidexx.db.entity.event.DietEntity
import com.microtech.aidexx.db.repository.EventDbRepository

object CloudDietHistorySync: EventHistorySync<DietEntity>() {

    override suspend fun getNeedUploadData(): MutableList<DietEntity>? =
        EventDbRepository.getDietNeedUploadEvent()

}