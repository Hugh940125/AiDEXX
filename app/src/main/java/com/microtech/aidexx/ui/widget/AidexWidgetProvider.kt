package com.microtech.aidexx.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.microtech.aidexx.APP_DEFAULT_PACKAGE_NAME
import com.microtech.aidexx.R
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.utils.TimeUtils.dateHourMinute
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.utils.toGlucoseStringWithLowAndHigh
import java.util.Date


class AidexWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        val views = RemoteViews(context?.packageName, R.layout.layout_aidex_widget)
        val model = TransmitterManager.instance().getDefault()
        if (model == null || !model.isPaired() || !model.isDataValid()
            || model.malFunctionList.isNotEmpty() || !UserInfoManager.instance().isLogin()
        ) {
            views.setTextViewText(R.id.widget_glucose_value, "--")
            views.setTextViewText(R.id.widget_unit, "")
            views.setTextViewText(
                R.id.widget_update_time, "--"
            )
            views.setImageViewResource(R.id.iv_widget_bg, R.drawable.bg_widget_white)
            views.setImageViewResource(R.id.widget_trend, 0)
        } else {
            context?.let {
                views.setTextViewText(
                    R.id.widget_glucose_value,
                    model.glucose?.toGlucoseStringWithLowAndHigh(
                        context.resources
                    )
                )
                views.setTextViewText(
                    R.id.widget_unit,
                    UnitManager.glucoseUnit.text
                )
                WidgetUpdateManager.instance().updateBgAndTrend(views)
            }
            views.setTextViewText(
                R.id.widget_update_time, Date().dateHourMinute()
            )
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            context?.packageManager?.getLaunchIntentForPackage(
                context.packageName ?: APP_DEFAULT_PACKAGE_NAME
            ),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
        appWidgetManager?.updateAppWidget(appWidgetIds, views)
    }
}