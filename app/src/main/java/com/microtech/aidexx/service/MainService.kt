package com.microtech.aidexx.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.*
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.R
import com.microtech.aidexx.ble.AidexBleAdapter
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.ble.device.model.DeviceModel
import com.microtech.aidexx.data.CloudCgmHistorySync
import com.microtech.aidexx.data.CloudHistorySync
import com.microtech.aidexx.ui.setting.alert.*
import com.microtech.aidexx.utils.ContextUtil
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.eventbus.AlertInfo
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.utils.mmkv.MmkvManager
import kotlinx.coroutines.*
import java.util.*

/**
 *@date 2023/3/6
 *@author Hugh
 *@desc
 */

private const val FOREGROUND_ID = 10010
private const val LOAD_TRANSMITTER = 10011
private const val LOCK_TIME_INTERVAL = 5 * 60 * 1000L
private const val LOCK_ACTION = "com.aidex.keep-alive"

class MainService : Service(), LifecycleOwner {
    private var mainServiceTimer: Timer? = null
    private var mainServiceTask: TimerTask? = null
    private var timeChangeReceiver: TimeChangeReceiver? = null
    private var lockPendingIntent: PendingIntent? = null
    private lateinit var alarmManager: AlarmManager
    private lateinit var foregroundNotification: Notification
    private lateinit var serviceMainScope: CoroutineScope
    private val alertChannelId = "com.microtech.aidexx.alert"
    private val foregroundChannelId = "com.microtech.aidexx.foreground"
    private val mLifecycleRegistry = LifecycleRegistry(this)
    private var smallIcon: IconCompat? = null
        get() {
            if (field == null) {
                field =
                    IconCompat.createFromIcon(Icon.createWithResource(this, R.mipmap.ic_launcher))
            }
            return field
        }
    private var notificationPendingIntent: PendingIntent? = null
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
                    getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            }
            return field
        }
    private var serviceHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            scheduleTask()
            lifecycleScope.launch {
                TransmitterManager.instance().loadTransmitter()
            }
            TransmitterManager.setOnTransmitterChangeListener {
                it?.let {
                    if (it.isPaired()) {
                        observeAlert(it)
                        AidexBleAdapter.getInstance().stopBtScan(true)
                        registerTimeChangeReceiver()
                    }
                }
            }
        }
    }
    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (LOCK_ACTION == action) {
                val powerManager = getSystemService(POWER_SERVICE) as PowerManager
                val wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK, "wake_lock:receiver"
                )
                wakeLock?.acquire(LOCK_TIME_INTERVAL)
                LogUtil.eAiDEX("Acquire wake lock")
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + LOCK_TIME_INTERVAL,
                    lockPendingIntent
                )
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        getWakeLock()
        serviceMainScope = MainScope()
        serviceHandler.removeMessages(LOAD_TRANSMITTER)
        serviceHandler.sendEmptyMessageDelayed(LOAD_TRANSMITTER, 500)
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) initNotificationChannel()
        EventBusManager.onReceive<Boolean>(EventBusKey.UPDATE_NOTIFICATION, this) {
            if (AidexxApp.instance.isDisplayOn()) {
                updateNotification(it)
            }
        }
    }

    fun scheduleTask() {
        var count = 0
        mainServiceTask = object : TimerTask() {
            override fun run() {
                count++
                if (count % 3 == 0) {
                    CloudHistorySync.downloadAllData()
                    val model = TransmitterManager.instance().getDefault()
                    if (model != null) {
                        if (model.isGettingTransmitterData) {
                            model.isGettingTransmitterData = false
                            return
                        }
                    }
                    serviceMainScope.launch {
                        CloudCgmHistorySync.upload()
                    }
                }
                if (count % 4 == 0) {

                }
                if (count == 9) {
                    count = 0
                }
            }
        }
        mainServiceTimer = Timer()
        mainServiceTimer?.schedule(mainServiceTask, 10 * 1000, 10 * 1000)
    }

    private fun registerTimeChangeReceiver() {
        timeChangeReceiver = TimeChangeReceiver()
        val mFilter = IntentFilter()
        mFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
        mFilter.addAction(Intent.ACTION_TIME_CHANGED)
        registerReceiver(timeChangeReceiver, mFilter)
    }

    private fun getWakeLock() {
        val intentFilter = IntentFilter(LOCK_ACTION)
        registerReceiver(receiver, intentFilter)
        val mIntent = Intent()
        mIntent.action = LOCK_ACTION
        lockPendingIntent = PendingIntent.getBroadcast(
            this@MainService,
            0,
            mIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 1000,
            lockPendingIntent
        )
    }

    private fun startForeground() {
        val remoteViews = ForegroundServiceNotification(
            this, notificationPendingIntent!!, packageName
        )
        buildNotification(remoteViews)
        startForeground(FOREGROUND_ID, foregroundNotification)
    }

    private fun buildNotification(view: RemoteViews) {
        smallIcon?.let {
            foregroundNotification =
                NotificationCompat.Builder(this, foregroundChannelId).setContent(view)
                    .setOnlyAlertOnce(true).setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSmallIcon(it).build()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initNotificationChannel() {
        val foregroundChannel = NotificationChannel(
            foregroundChannelId,
            getString(R.string.title_notification_foreground),
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager?.createNotificationChannel(foregroundChannel)
        val alertChannel = NotificationChannel(
            alertChannelId,
            getString(R.string.title_notification_alarm),
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager?.createNotificationChannel(alertChannel)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground()
        serviceHandler.removeMessages(LOAD_TRANSMITTER)
        serviceHandler.sendEmptyMessageDelayed(LOAD_TRANSMITTER, 500)
        return START_STICKY
    }

    private fun updateNotification(normal: Boolean) {
        val model = TransmitterManager.instance().getDefault()
        model?.let {
            val remoteViews = ForegroundServiceNotification(
                this, notificationPendingIntent!!, packageName
            )
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

    private fun observeAlert(model: DeviceModel) {
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
            .setSmallIcon(R.mipmap.ic_launcher_weitai2).setContentIntent(notificationPendingIntent)
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
        mainServiceTask?.cancel()
        mainServiceTimer?.cancel()
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        timeChangeReceiver?.let {
            unregisterReceiver(timeChangeReceiver)
        }
    }

    override val lifecycle: Lifecycle
        get() = mLifecycleRegistry
}