package com.microtech.aidexx.db.entity

import android.content.res.Resources
import com.microtech.aidexx.R
import com.microtech.aidexx.utils.LanguageUnitManager
import com.microtech.aidexx.utils.UnitManager
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.IndexType
import java.util.Date
import java.util.UUID


@Entity
class BloodGlucoseEntity : EventEntity {
    @Index(type = IndexType.HASH)
    override var userId: String? = null

    @Id
    override var idx: Long? = null
    override var state: Int = 0
    override var recordIndex: Long? = null
    override var recordId: String? = null
    override var deleteStatus: Int = 0
    var bloodGlucoseId: String? = UUID.randomUUID().toString().replace("-", "")
    override var id: String? = null
    var testTime: Date = Date()
    var testTag: Int = 0
    var bloodGlucoseMg: Float = 0f
    override var createTime: Date = Date()

    @Index
    var autoIncrementColumn: Long? = null

    @Transient
    var calibration: Boolean = false

    @Transient
    override var time: Date = testTime
        get() {
            return testTime
        }
        set(time) {
            field = time
            testTime = time
        }
    override var language: String = ""
    override var uploadState: Int = 0

    constructor()
    constructor(testTime: Date, bloodGlucose: Float) {
        this.testTime = testTime
        this.bloodGlucoseMg = bloodGlucose
        this.language = LanguageUnitManager.getCurrentLanguageCode()
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
            UnitManager.GlucoseUnit.MMOL_PER_L -> "${bloodGlucoseMg / 18}$unit"
            UnitManager.GlucoseUnit.MG_PER_DL -> "$bloodGlucoseMg$unit"
        }
    }

    override fun toString(): String {
        return "BloodGlucoseEntity(userId=$userId, idx=$idx, state=$state, recordIndex=$recordIndex, recordId=$recordId, deleteStatus=$deleteStatus, bloodGlucoseId=$bloodGlucoseId, id=$id, testTime=$testTime, testTag=$testTag, bloodGlucoseMg=$bloodGlucoseMg, bloodGlucoseMg=$bloodGlucoseMg, createTime=$createTime, calibration=$calibration, language='$language', uploadState=$uploadState)"
    }
}
