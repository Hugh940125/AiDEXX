package com.microtech.aidexx.service

import android.app.*
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.R
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.ble.device.model.DeviceModel
import com.microtech.aidexx.ui.setting.alert.*
import com.microtech.aidexx.utils.ContextUtil
import com.microtech.aidexx.utils.eventbus.AlertInfo
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.utils.mmkv.MmkvManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

/**
 *@date 2023/3/6
 *@author Hugh
 *@desc
 */

private const val FOREGROUND_ID: Int = 10010

class MainService : Service(), LifecycleOwner {
    private lateinit var foregroundNotification: Notification
    private lateinit var remoteViews: ForegroundServiceNotification
    private lateinit var serviceMainScope: CoroutineScope
    private val alertChannelId = "com.microtech.aidexx.alert"
    private val foregroundChannelId = "com.microtech.aidexx.foreground"
    private val mLifecycleRegistry = LifecycleRegistry(this)
    private var pendingIntent: PendingIntent? = null
        get() {
            if (field == null) {
                field = PendingIntent.getActivity(
                    this, 0, this.packageManager?.getLaunchIntentForPackage(
                        this.packageName ?: "com.microtechmd.cgms"
                    ), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
            return field
        }
    private var notificationManager: NotificationManager? = null
        get() {
            if (field == null) {
                field =
                    getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            }
            return field
        }

    override fun onCreate() {
        super.onCreate()
        serviceMainScope = MainScope()
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) initNotificationChannel()
        EventBusManager.onReceive<Boolean>(EventBusKey.UPDATE_NOTIFICATION, this) {
            updateNotification(it)
        }
    }

    private fun startForeground() {
        remoteViews = ForegroundServiceNotification(
            this, pendingIntent!!, packageName
        )
        buildNotification(remoteViews)
        startForeground(FOREGROUND_ID, foregroundNotification)
    }

    private fun buildNotification(view: RemoteViews) {
        foregroundNotification =
            NotificationCompat.Builder(this, foregroundChannelId).setContent(view)
                .setOnlyAlertOnce(true).setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.mipmap.ic_launcher_weitai2).build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initNotificationChannel() {
        val foregroundChannel = NotificationChannel(
            foregroundChannelId,
            getString(R.string.title_natification_foreground),
            NotificationManager.IMPORTANCE_LOW
        )
        val alertChannel = NotificationChannel(
            alertChannelId,
            getString(R.string.title_natification_alarm),
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager?.createNotificationChannel(foregroundChannel)
        notificationManager?.createNotificationChannel(alertChannel)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground()
        TransmitterManager.setOnTransmitterChangeListener { model ->
            if (model.isPaired()) {
                executeAlert(model)
            }
        }
        return START_STICKY
    }

    private fun updateNotification(normal: Boolean) {
        val model = TransmitterManager.instance().getDefault()
        model?.let {
            if (::foregroundNotification.isInitialized && ::remoteViews.isInitialized) {
                if (model.isDataValid() && normal) {
                    remoteViews.setGlucose(
                        if (model.minutesAgo == 0) "" else "${model.minutesAgo.toString()}${
                            ContextUtil.getResources()?.getString(R.string.min_ago)
                        }", model.glucose
                    )
                } else {
                    remoteViews.setGlucose("--", null)
                }
                buildNotification(remoteViews)
                notificationManager?.notify(FOREGROUND_ID, foregroundNotification)
            }
        }
    }

    private fun executeAlert(model: DeviceModel) {
        model.alert = { time, type ->
            val content: String
            var isUrgent = false
            var showCustomerService = false
            val res = ContextUtil.getResources() ?: resources
            var alertMethod = if (AidexxApp.instance.isForeground()) {
                METHOD_DIALOG
            } else {
                METHOD_NOTIFICATION
            }
            when (type) {
                MESSAGE_TYPE_REPLACE_SENSOR -> {
                    content =
                        "$time ${res.getString(R.string.need_replace_sensor_and_getCustomer_service)}"
                    showCustomerService = true
                }
                MESSAGE_TYPE_GLUCOSE_HIGH -> {
                    content = "$time ${res.getString(R.string.hyper_item)}"
                }
                MESSAGE_TYPE_GLUCOSE_LOW -> {
                    content = "$time ${res.getString(R.string.hypo_item)}"
                }
                MESSAGE_TYPE_GLUCOSE_LOW_URGENT -> {
                    content = "$time ${res.getString(R.string.urgent_low_title)}"
                    isUrgent = true
                }
                MESSAGE_TYPE_GLUCOSE_DOWN -> {
                    content = "$time ${res.getString(R.string.Falling_Fast)}"
                }
                MESSAGE_TYPE_GLUCOSE_UP -> {
                    content = "$time ${res.getString(R.string.Rising_Fast)}"
                }
                MESSAGE_TYPE_SIGNAL_LOST -> {
                    content = "$time ${res.getString(R.string.Signal_Loss)}"
                    alertMethod = METHOD_NOTIFICATION
                }
                MESSAGE_TYPE_NEW_SENSOR -> {
                    content = "$time ${res.getString(R.string.New_Sensor)}"
                    alertMethod = METHOD_NOTIFICATION
                }
                MESSAGE_TYPE_DEVICE_TIME_ERROR -> {
                    content = "$time ${res.getString(R.string.message_device_error)}"
                }
                else -> {
                    content = ""
                }
            }
            process(type, isUrgent)
            when (alertMethod) {
                METHOD_DIALOG -> {
                    EventBusManager.send(
                        EventBusKey.EVENT_SHOW_ALERT, AlertInfo(content, type, showCustomerService)
                    )
                }
                METHOD_NOTIFICATION -> {
                    notificationAlert(content, type)
                }
            }
        }
    }

    private fun notificationAlert(content: String, type: Int) {
        val notification = NotificationCompat.Builder(this, alertChannelId)
            .setContentTitle(getString(R.string.app_name)).setContentText(content)
            .setVibrate((longArrayOf(0, 180, 80, 120)))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.mipmap.ic_launcher_weitai2).setContentIntent(pendingIntent)
            .setAutoCancel(true).build()
        notificationManager?.notify(type, notification)
    }

    private fun process(type: Int, isUrgent: Boolean) {
        if (type == MESSAGE_TYPE_SIGNAL_LOST) {
            val signalLossAlertMethod = MmkvManager.signalLossAlertMethod()
            AlertUtil.alert(this, signalLossAlertMethod, isUrgent)
            return
        }
        if (type != MESSAGE_TYPE_REPLACE_SENSOR && type != MESSAGE_TYPE_NEW_SENSOR) {
            if (isUrgent) {
                val urgentAlertMethod = MmkvManager.getUrgentAlertMethod()
                AlertUtil.alert(this, urgentAlertMethod, true)
            } else {
                val alertMethod = MmkvManager.getAlertMethod()
                AlertUtil.alert(this, alertMethod, false)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceMainScope.cancel()
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    override val lifecycle: Lifecycle
        get() = mLifecycleRegistry
}