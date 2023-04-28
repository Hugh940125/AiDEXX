package com.microtech.aidexx.service

import android.app.PendingIntent
import android.content.Context
import android.widget.RemoteViews
import com.microtech.aidexx.R
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.utils.toGlucoseStringWithLowAndHigh

class ForegroundServiceNotification(
    val context: Context,
    pendingIntent: PendingIntent,
    packageName: String?
) : RemoteViews(
    packageName,
    R.layout.remoteviews_notification
) {
    init {
        setOnClickPendingIntent(R.id.notice, pendingIntent)
    }

    fun setGlucose(datetime: String, glucose: Float?) {
        setTextViewText(R.id.tv_time, datetime)
        setTextViewText(
            R.id.tv_glucose,
            glucose?.toGlucoseStringWithLowAndHigh(context.resources) ?: "--"
        )
        setTextViewText(R.id.tv_unit, UnitManager.glucoseUnit.text)
    }
}