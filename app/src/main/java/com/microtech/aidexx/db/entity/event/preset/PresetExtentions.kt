package com.microtech.aidexx.db.entity.event.preset

import com.microtech.aidexx.db.entity.event.ExerciseDetail
import com.microtech.aidexx.db.entity.event.MedicationDetail


fun SportPresetEntity.toExerciseDetailEntity(): ExerciseDetail =
    ExerciseDetail().also {
        it.exec_preset_id = id
        it.name = name
        it.presetType = if (fkUser.isEmpty()) 0 else 1
        it.intensity_category_name = intensityCategoryName
        it.hour_kcal_per_kg = hourKcalPerKg
    }

fun MedicinePresetEntity.toMedicineDetailEntity(): MedicationDetail =
    MedicationDetail().also {
        it.medicine_preset_id = id
        it.name = name
        it.presetType = if (fkUser.isEmpty()) 0 else 1
        it.category_name = categoryName
        it.english_name = englishName
        it.manufacturer = manufacturer ?: ""
        it.tradeName = tradeName ?: ""
    }