package com.microtech.aidexx.db.entity

import android.content.res.Resources
import com.microtech.aidexx.R
import com.microtech.aidexx.widget.dialog.x.util.toGlucoseStringWithUnit
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.util.*


@Entity
class BloodGlucoseEntity :
    EventEntity {
    override var authorizationId: String? = null

    @Id
    override var idx: Long? = null
    override var state: Int = 0
    override var recordIndex: Long? = null
    override var deleteStatus: Int = 0
    var recordUuid: String? = UUID.randomUUID().toString().replace("-", "")
    override var id: String? = null
    var testTime: Date = Date()
    var testTag: Int = 0
    var bloodGlucose: Float = -1f

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

    constructor()
    constructor(testTime: Date, bloodGlucose: Float) {
        this.testTime = testTime
        this.bloodGlucose = bloodGlucose
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

    override fun getValueDescription(res: Resources): String =
        bloodGlucose.toGlucoseStringWithUnit()

    override fun toString(): String {
        return "BloodGlucoseEntity(authorizationId=$authorizationId, idx=$idx, state=$state, recordIndex=$recordIndex, deleteStatus=$deleteStatus, recordUuid=$recordUuid, id=$id, testTime=$testTime, testTag=$testTag, bloodGlucose=$bloodGlucose, calibration=$calibration, time=$time)"
    }


}