package com.microtech.aidexx.db.entity.event.preset

import com.microtech.aidexx.db.entity.event.DietDetail
import com.microtech.aidexx.db.entity.event.ExerciseDetail
import com.microtech.aidexx.db.entity.event.InsulinDetail
import com.microtech.aidexx.db.entity.event.MedicationDetail


fun DietPresetEntity.toDietDetailEntity(): DietDetail =
    DietDetail().also {
        it.foodPresetId = getPresetId()
        it.name = name
        it.presetType = if (isUserPreset()) 1 else 0
        it.carbohydrate = carbohydrate
        it.protein = protein
        it.unit = unit
        it.fat = fat
        it.quantity = quantity
    }

fun SportPresetEntity.toExerciseDetailEntity(): ExerciseDetail =
    ExerciseDetail().also {
        it.exercisePresetId = getPresetId()
        it.name = name
        it.presetType = if (isUserPreset()) 1 else 0
        it.intensity_category_name = intensityCategoryName
        it.hour_kcal_per_kg = hourKcalPerKg
    }

fun MedicinePresetEntity.toMedicineDetailEntity(): MedicationDetail =
    MedicationDetail().also {
        it.medicationPresetId = getPresetId()
        it.name = name
        it.presetType = if (isUserPreset()) 1 else 0
        it.category_name = categoryName
        it.english_name = englishName
        it.manufacturer = manufacturer ?: ""
        it.tradeName = tradeName ?: ""
    }


fun InsulinPresetEntity.toInsulinDetailEntity(): InsulinDetail =
    InsulinDetail().also {
        it.insulinPresetId = getPresetId()
        it.name = name
        it.presetType = if (isUserPreset()) 1 else 0
        it.categoryName = categoryName ?: ""
        it.manufacturer = manufacturer ?: ""
        it.tradeName = tradeName ?: ""
        it.comment = comment
    }

