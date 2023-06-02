package com.microtech.aidexx.data

import com.microtech.aidexx.db.entity.event.ExerciseEntity
import com.microtech.aidexx.db.repository.EventDbRepository

object CloudExerciseHistorySync: EventHistorySync<ExerciseEntity>() {

    override suspend fun getNeedUploadData(): MutableList<ExerciseEntity>? =
        EventDbRepository.getExerciseNeedUploadEvent()

}