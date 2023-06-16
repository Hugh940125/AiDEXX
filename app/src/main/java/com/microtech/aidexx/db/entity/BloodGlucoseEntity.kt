package com.microtech.aidexx.db.entity

import android.content.res.Resources
import com.microtech.aidexx.R
import com.microtech.aidexx.data.LocalManager
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.utils.roundOffDecimal
import com.microtech.aidexx.utils.toGlucoseValue
import io.objectbox.annotation.Entity
import java.util.Date
import java.util.UUID


@Entity
class BloodGlucoseEntity : BaseEventEntity {

    var bloodGlucoseId: String? = UUID.randomUUID().toString().replace("-", "")
    var testTag: Int = 0
    var bloodGlucoseMg: Float = 0f

    @Transient
    var calibration: Boolean = false

    constructor()
    constructor(testTime: Date, bloodGlucose: Float) {
        setTimeInfo(testTime)
        this.bloodGlucoseMg = roundOffDecimal(bloodGlucose)
        this.language = LocalManager.getCurLanguageTag()
    }

    override fun getEventDescription(res: Resources): String =
        if (calibration) res.getString(R.string.suggest_calibration) else {
            val text = getTagText(res)
            if (!text.isNullOrEmpty()) {
                res.getString(R.string.title_bg_record) + "(${text})"
            } else {
                res.getString(R.string.title_bg_record)
            }
        }

    fun getTagText(res: Resources): String? =
        if (calibration) res.getString(R.string.suggest_calibration) else
            when (testTag) {
                1 -> res.getString(R.string.empty_stomach)
                2 -> res.getString(R.string.after_breakfast)
                3 -> res.getString(R.string.before_lunch)
                4 -> res.getString(R.string.after_lunch)
                5 -> res.getString(R.string.before_dinner)
                6 -> res.getString(R.string.after_dinner)
                7 -> res.getString(R.string.before_sleep)
                8 -> res.getString(R.string.morning)
                255 -> res.getString(R.string.random)
                0 -> ""
                else -> null
            }

    override fun getValueDescription(res: Resources): String {
        val unit = UnitManager.glucoseUnit.text
        return when (UnitManager.glucoseUnit) {
            UnitManager.GlucoseUnit.MMOL_PER_L -> "${roundOffDecimal(bloodGlucoseMg / 18)}$unit"
            UnitManager.GlucoseUnit.MG_PER_DL -> "$bloodGlucoseMg$unit"
        }
    }

    override fun toString(): String {
        return "BloodGlucoseEntity(userId=$userId, idx=$idx, state=$state, recordIndex=$recordIndex, recordId=$recordId, deleteStatus=$deleteStatus, bloodGlucoseId=$bloodGlucoseId, id=$id, testTag=$testTag, bloodGlucoseMg=$bloodGlucoseMg, bloodGlucoseMg=$bloodGlucoseMg, createTime=$createTime, calibration=$calibration, language='$language', uploadState=$uploadState)"
    }
    override fun hashCode(): Int {
        return bloodGlucoseId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other?.let {
            it is BloodGlucoseEntity && it.bloodGlucoseId == this.bloodGlucoseId
        } ?: false
    }
    fun getGlucoseValue(): Float {
        if (bloodGlucoseMg < 2 * 18) {
            return (2f * 18).toGlucoseValue()
        }
        if (UnitManager.glucoseUnit == UnitManager.GlucoseUnit.MMOL_PER_L) {
            if (bloodGlucoseMg > 30 * 18) {
                return (30f * 18).toGlucoseValue()
            } else {
                bloodGlucoseMg.toGlucoseValue()
            }
        } else {
            return if (bloodGlucoseMg >= 600) {
                600f.toGlucoseValue()
            } else {
                bloodGlucoseMg.toGlucoseValue()
            }
        }
        return bloodGlucoseMg.toGlucoseValue()
    }
}
