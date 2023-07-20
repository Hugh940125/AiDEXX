package com.microtech.aidexx.ui.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import com.microtech.aidexx.R
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.ble.device.model.DeviceModel
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.utils.TimeUtils.dateHourMinute
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.utils.toGlucoseStringWithLowAndHigh
import java.util.Date


class WidgetUpdateManager private constructor() {

    companion object {
        private val INSTANCE = WidgetUpdateManager()

        fun instance(): WidgetUpdateManager {
            return INSTANCE
        }
    }

    fun updateBgAndTrend(remoteViews: RemoteViews) {
        val defaultModel = TransmitterManager.instance().getDefault()
        val glucoseTrend = defaultModel?.glucoseTrend
        val glucoseLevel = defaultModel?.glucoseLevel
        when (glucoseLevel) {
            DeviceModel.GlucoseLevel.LOW -> {
                remoteViews.setImageViewResource(R.id.iv_widget_bg, R.drawable.bg_widget_red)
            }

            DeviceModel.GlucoseLevel.NORMAL -> {
                remoteViews.setImageViewResource(R.id.iv_widget_bg, R.drawable.bg_widget_green)
            }

            DeviceModel.GlucoseLevel.HIGH -> {
                remoteViews.setImageViewResource(R.id.iv_widget_bg, R.drawable.bg_widget_yellow)
            }

            else -> {
                remoteViews.setImageViewResource(R.id.iv_widget_bg, R.drawable.bg_widget_white)
            }
        }
        if (glucoseLevel != null && glucoseTrend != null) {
            updateTrend(remoteViews, glucoseLevel, glucoseTrend)
        }
    }

    fun updateTrend(
        remoteViews: RemoteViews,
        level: DeviceModel.GlucoseLevel,
        trend: DeviceModel.GlucoseTrend
    ) {
        trend.let {
            if (level == DeviceModel.GlucoseLevel.LOW && trend == DeviceModel.GlucoseTrend.FAST_FALL) {
                remoteViews.setImageViewResource(R.id.widget_trend, R.drawable.ic_t4_l)
            } else if (level == DeviceModel.GlucoseLevel.NORMAL && trend == DeviceModel.GlucoseTrend.FAST_FALL) {
                remoteViews.setImageViewResource(R.id.widget_trend, R.drawable.ic_t4)
            } else if (level == DeviceModel.GlucoseLevel.HIGH && trend == DeviceModel.GlucoseTrend.FAST_FALL) {
                remoteViews.setImageViewResource(R.id.widget_trend, R.drawable.ic_t4_h)
            } else if (level == DeviceModel.GlucoseLevel.LOW && trend == DeviceModel.GlucoseTrend.FALL) {
                remoteViews.setImageViewResource(R.id.widget_trend, R.drawable.ic_t3_l)
            } else if (level == DeviceModel.GlucoseLevel.NORMAL && trend == DeviceModel.GlucoseTrend.FALL) {
                remoteViews.setImageViewResource(R.id.widget_trend, R.drawable.ic_t3)
            } else if (level == DeviceModel.GlucoseLevel.HIGH && trend == DeviceModel.GlucoseTrend.FALL) {
                remoteViews.setImageViewResource(R.id.widget_trend, R.drawable.ic_t3_h)
            } else if (level == DeviceModel.GlucoseLevel.LOW && trend == DeviceModel.GlucoseTrend.SLOW_FALL) {
                remoteViews.setImageViewResource(R.id.widget_trend, R.drawable.ic_t2_l)
            } else if (level == DeviceModel.GlucoseLevel.NORMAL && trend == DeviceModel.GlucoseTrend.SLOW_FALL) {
                remoteViews.setImageViewResource(R.id.widget_trend, R.drawable.ic_t2)
            } else if (level == DeviceModel.GlucoseLevel.HIGH && trend == DeviceModel.GlucoseTrend.SLOW_FALL) {
                remoteViews.setImageViewResource(R.id.widget_trend, R.drawable.ic_t2_h)
            } else if (level == DeviceModel.GlucoseLevel.LOW && trend == DeviceModel.GlucoseTrend.SLOW_UP) {
                remoteViews.setImageViewResource(R.id.widget_trend, R.drawable.ic_t5_l)
            } else if (level == DeviceModel.GlucoseLevel.NORMAL && trend == DeviceModel.GlucoseTrend.SLOW_UP) {
                remoteViews.setImageViewResource(R.id.widget_trend, R.drawable.ic_t5)
            } else if (level == DeviceModel.GlucoseLevel.HIGH && trend == DeviceModel.GlucoseTrend.SLOW_UP) {
                remoteViews.setImageViewResource(R.id.widget_trend, R.drawable.ic_t5_h)
            } else if (level == DeviceModel.GlucoseLevel.LOW && trend == DeviceModel.GlucoseTrend.UP) {
                remoteViews.setImageViewResource(R.id.widget_trend, R.drawable.ic_t7_l)
            } else if (level == DeviceModel.GlucoseLevel.NORMAL && trend == DeviceModel.GlucoseTrend.UP) {
                remoteViews.setImageViewResource(R.id.widget_trend, R.drawable.ic_t7)
            } else if (level == DeviceModel.GlucoseLevel.HIGH && trend == DeviceModel.GlucoseTrend.UP) {
                remoteViews.setImageViewResource(R.id.widget_trend, R.drawable.ic_t7_h)
            } else if (level == DeviceModel.GlucoseLevel.LOW && trend == DeviceModel.GlucoseTrend.FAST_UP) {
                remoteViews.setImageViewResource(R.id.widget_trend, R.drawable.ic_t6_l)
            } else if (level == DeviceModel.GlucoseLevel.NORMAL && trend == DeviceModel.GlucoseTrend.FAST_UP) {
                remoteViews.setImageViewResource(R.id.widget_trend, R.drawable.ic_t6)
            } else if (level == DeviceModel.GlucoseLevel.HIGH && trend == DeviceModel.GlucoseTrend.FAST_UP) {
                remoteViews.setImageViewResource(R.id.widget_trend, R.drawable.ic_t6_h)
            } else if (level == DeviceModel.GlucoseLevel.LOW && trend == DeviceModel.GlucoseTrend.STEADY) {
                remoteViews.setImageViewResource(R.id.widget_trend, R.drawable.ic_t1_l)
            } else if (level == DeviceModel.GlucoseLevel.NORMAL && trend == DeviceModel.GlucoseTrend.STEADY) {
                remoteViews.setImageViewResource(R.id.widget_trend, R.drawable.ic_t1)
            } else if (level == DeviceModel.GlucoseLevel.HIGH && trend == DeviceModel.GlucoseTrend.STEADY) {
                remoteViews.setImageViewResource(R.id.widget_trend, R.drawable.ic_t1_h)
            } else {
                remoteViews.setImageViewResource(R.id.widget_trend, 0)
            }
        }
    }

    fun update(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val remoteViews = RemoteViews(context.packageName, R.layout.layout_aidex_widget)
        val model = TransmitterManager.instance().getDefault()
        if (model == null || !model.isPaired() || !model.isDataValid()
            || model.malFunctionList.isNotEmpty() || !UserInfoManager.instance().isLogin()
        ) {
            remoteViews.setTextViewText(R.id.widget_glucose_value, "--")
            remoteViews.setTextViewText(R.id.widget_unit, "")
            remoteViews.setTextViewText(R.id.widget_update_time, "--")
            remoteViews.setImageViewResource(R.id.iv_widget_bg, R.drawable.bg_widget_white)
            remoteViews.setImageViewResource(R.id.widget_trend, 0)
        } else {
            remoteViews.setTextViewText(
                R.id.widget_glucose_value,
                model.glucose?.toGlucoseStringWithLowAndHigh(
                    context.resources
                )
            )
            remoteViews.setTextViewText(
                R.id.widget_unit,
                UnitManager.glucoseUnit.text
            )
            updateBgAndTrend(remoteViews)
            remoteViews.setTextViewText(
                R.id.widget_update_time, Date().dateHourMinute()
            )
        }
        appWidgetManager.updateAppWidget(
            ComponentName(
                context,
                AidexWidgetProvider::class.java
            ), remoteViews
        )
    }
}