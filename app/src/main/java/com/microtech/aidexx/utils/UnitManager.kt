package com.microtech.aidexx.utils

import android.content.Context
import com.microtech.aidexx.utils.mmkv.MmkvManager
import com.tencent.mmkv.MMKV
import java.text.DecimalFormat

object UnitManager {

    enum class GlucoseUnit(
        val index: Int,
        val text: String,
        val unit: String,
        val formatter: DecimalFormat
    ) {
        MMOL_PER_L(1, "mmol/L", "mmol", DecimalFormat("0.0")),
        MG_PER_DL(2, "mg/dL", "mg", DecimalFormat("0"))
    }


    var glucoseUnit = glucoseUnitByIndex(MmkvManager.getGlucoseUnit())
        set(glucoseUnit) {
            field = glucoseUnit
            MmkvManager.saveGlucoseUnit(glucoseUnit.index)
        }

    fun glucoseUnitByIndex(index: Int): GlucoseUnit {
        return when (index) {
            GlucoseUnit.MMOL_PER_L.index -> GlucoseUnit.MMOL_PER_L
            GlucoseUnit.MG_PER_DL.index -> GlucoseUnit.MG_PER_DL
            else -> GlucoseUnit.MMOL_PER_L
        }
    }

    fun formatterUnitByIndex(): DecimalFormat {
        return when (glucoseUnit.index) {
            1 -> DecimalFormat("0.0")
            2 -> DecimalFormat("0")
            else -> DecimalFormat("0")
        }
    }
}