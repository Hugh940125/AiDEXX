package com.microtech.aidexx.data

import com.microtech.aidexx.db.entity.event.MedicationEntity
import com.microtech.aidexx.db.repository.EventDbRepository

object CloudMedicineHistorySync: EventHistorySync<MedicationEntity>() {

    override suspend fun getNeedUploadData(): MutableList<MedicationEntity>? =
        EventDbRepository.getMedicineNeedUploadEvent()

}