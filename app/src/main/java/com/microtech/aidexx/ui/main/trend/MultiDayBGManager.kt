package com.microtech.aidexx.ui.main.trend

import android.content.Context
import android.graphics.Color
import com.microtech.aidexx.R
import com.microtech.aidexx.common.equal
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.ObjectBox.cgmHistoryBox
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity_
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
    val mutableListOf = mutableListOf<MultiDayBGItem>()

    companion object {
        private val instance = MultiDayBGManager()

        fun getInstance(): MultiDayBGManager {
            return instance
        }
    }

    fun findDataByDays(
        context: Context,
        startDate: Date,
        endDate: Date
    ): MutableList<MultiDayBGItem> {
        mutableListOf.clear()
        val calendarStart = Calendar.getInstance()
        calendarStart.time = startDate
        val calendarEnd = Calendar.getInstance()
        calendarEnd.time = endDate
        while (calendarEnd.after(calendarStart)) {
            val list = cgmHistoryBox!!.query().between(
                RealCgmHistoryEntity_.deviceTime,
                calendarStart.time.time,
                calendarStart.time.time + oneDayMillis
            ).equal(RealCgmHistoryEntity_.eventType, 7)
                .or().equal(RealCgmHistoryEntity_.eventType, 8)
                .equal(
                    RealCgmHistoryEntity_.userId,
                    UserInfoManager.getCurShowUserId(),
                )
                .notEqual(RealCgmHistoryEntity_.eventWarning, -1).or()
                .isNull(RealCgmHistoryEntity_.eventWarning)
                .order(RealCgmHistoryEntity_.deviceId)
                .order(RealCgmHistoryEntity_.sensorIndex)
                .order(RealCgmHistoryEntity_.eventIndex)
                .orderDesc(RealCgmHistoryEntity_.deviceTime).build()
                .find()
            val multiDayBGItem =
                MultiDayBGItem(
                    String.format(
                        context.getString(
                            R.string.date_format,
                            if (calendarStart.get(Calendar.MONTH) + 1 > 9) calendarStart.get(
                                Calendar.MONTH
                            ) + 1 else "0${
                                calendarStart.get(Calendar.MONTH) + 1
                            }",
                            if (calendarStart.get(Calendar.DAY_OF_MONTH) > 9) calendarStart.get(
                                Calendar.DAY_OF_MONTH
                            ).toString() else "0${
                                calendarStart.get(Calendar.DAY_OF_MONTH)
                            }"
                        )
                    ),
                    false,
                    startDate.time,
                    endDate.time,
                    list,
                    Color.parseColor(if (mutableListOf.size < 32) colorSet[mutableListOf.size] else colorSet[mutableListOf.size % 32])
                )
            mutableListOf.add(multiDayBGItem)
            calendarStart.add(Calendar.DAY_OF_MONTH, 1)
        }
        mutableListOf.reverse()
        return mutableListOf
    }
}