package com.microtech.aidexx.ui.main.trend

import android.graphics.Color
import com.microtech.aidexx.ble.device.model.GLUCOSE_NUM_ONE_DAY
import com.microtech.aidexx.common.equal
import com.microtech.aidexx.common.getEndOfTheDay
import com.microtech.aidexx.common.getStart
import com.microtech.aidexx.common.millisToDays
import com.microtech.aidexx.common.minutesToMillis
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.ObjectBox.cgmHistoryBox
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity_
import com.microtech.aidexx.utils.toGlucoseValue
import java.util.Calendar
import java.util.Date

const val oneDayMillis = 24 * 60 * 60 * 1000
const val oneDayMinutes = 24 * 60
const val maxShowDefault = 15

class MultiDayBGManager {

    val colorSet = mutableListOf(
        "#0089ff",
        "#26c2d3",
        "#ffed00",
        "#0089ff",
        "#63ac19",
        "#00e7ff",
        "#0600ff",
        "#12cea5",
        "#ffcb00",
        "#6016b6",
        "#94ab0d",
        "#00ffc8",
        "#8b00ff",
        "#336f53",
        "#ffa400",
        "#2118bd",
        "#69720e",
        "#fb00ff",
        "#53cb41",
        "#ff7e00",
        "#aa17a8",
        "#d7b612",
        "#ff0085",
        "#aaff00",
        "#ff4800",
        "#80325b",
        "#c89659",
        "#ff0000",
        "#6570d9",
        "#769ac1",
        "#099514",
        "#9f340c"
    )
    val mutableListOf = mutableListOf<MultiDayBgItem>()

    companion object {
        private val instance = MultiDayBGManager()

        fun getInstance(): MultiDayBGManager {
            return instance
        }
    }

    suspend fun findDataByDays(
        startDate: Date,
        endDate: Date
    ): DataInfoForTrends? {
        return ObjectBox.awaitCallInTx {
            mutableListOf.clear()
            var dayIndex = 0
            var arrayIndex = 0
            val dayCount = (endDate.time - startDate.time).millisToDays()
            val glucoseArray = Array(dayCount) { DoubleArray(GLUCOSE_NUM_ONE_DAY) }
            val start = startDate.getStart()
            val end = endDate.getStart()
            var historyCount = 0
            var totalHistory = 0
            while (end.after(start)) {
                val tempList = cgmHistoryBox!!.query().between(
                    RealCgmHistoryEntity_.timestamp,
                    start.time.time,
                    start.time.getEndOfTheDay().time
                ).equal(RealCgmHistoryEntity_.glucoseIsValid, 1)
                    .equal(RealCgmHistoryEntity_.userId, UserInfoManager.getCurShowUserId())
                    .notEqual(RealCgmHistoryEntity_.eventWarning, -1)
                    .order(RealCgmHistoryEntity_.timestamp).build()
                    .find()
                totalHistory += tempList.size
                val list = mutableListOf<RealCgmHistoryEntity>()
                var lastHistoryTime = 0L
                for (history in tempList) {
                    if (history.timestamp - lastHistoryTime < 5.minutesToMillis()) {
                        continue
                    }
                    list.add(history)
                    history.glucose?.let {
                        if (arrayIndex <= GLUCOSE_NUM_ONE_DAY) {
                            if (it.toGlucoseValue() > 25) {
                                glucoseArray[dayIndex][arrayIndex++] = 25.0
                            } else if (it.toGlucoseValue() < 2) {
                                glucoseArray[dayIndex][arrayIndex++] = 2.0
                            } else {
                                glucoseArray[dayIndex][arrayIndex++] = it.toGlucoseValue().toDouble()
                            }
                        }
                    }
                    lastHistoryTime = history.timestamp
                }
                val multiDayBGItem =
                    MultiDayBgItem(
                        Pair(start.get(Calendar.MONTH) + 1, start.get(Calendar.DAY_OF_MONTH)),
                        false,
                        list,
                        Color.parseColor(if (mutableListOf.size < 32) colorSet[mutableListOf.size] else colorSet[mutableListOf.size % 32])
                    )
                historyCount += list.size
                mutableListOf.add(multiDayBGItem)
                start.add(Calendar.DAY_OF_MONTH, 1)
                dayIndex++
                arrayIndex = 0
            }
            DataInfoForTrends(mutableListOf, glucoseArray, historyCount, totalHistory)
        }
    }
}