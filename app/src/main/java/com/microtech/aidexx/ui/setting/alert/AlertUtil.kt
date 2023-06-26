package com.microtech.aidexx.ui.setting.alert

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.R
import com.microtech.aidexx.common.minutesToMillis
import com.microtech.aidexx.ui.setting.SettingsManager.getSettings
import com.microtech.aidexx.ui.setting.SettingsManager.saveSetting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val COMMON_NOTICE = 0
const val URGENT_NOTICE = 1

const val MESSAGE_TYPE_GLUCOSE_HIGH = 1 //  高血糖报警
const val MESSAGE_TYPE_GLUCOSE_LOW = 2 //  低血糖报警
const val MESSAGE_TYPE_GLUCOSE_LOW_URGENT = 3 //  低血糖紧急提醒
const val MESSAGE_TYPE_GLUCOSE_DOWN = 4 //  血糖数据快速下降
const val MESSAGE_TYPE_GLUCOSE_UP = 5  //  血糖数据快速上升
const val MESSAGE_TYPE_SIGNAL_LOST = 6  // 信号丢失
const val MESSAGE_TYPE_REPLACE_SENSOR = 7  // 需要更换传感器
const val MESSAGE_TYPE_NEW_SENSOR = 8  // 新传感器
const val MESSAGE_TYPE_DEVICE_TIME_ERROR = 9  //设备时间不对

const val METHOD_DIALOG = 0
const val METHOD_NOTIFICATION = 1

object AlertUtil {
    private lateinit var soundMap: HashMap<Int, Int>
    private lateinit var mSoundPool: SoundPool
    private var playingSound: Int = -1
    private var vibrator: Vibrator? = null
    var alertFrequency: Long = 30.minutesToMillis()
    var urgentFrequency: Long = 5.minutesToMillis()
    var hyperSwitchEnable: Boolean = true
    var hypoSwitchEnable: Boolean = true
    var urgentLowSwitchEnable: Boolean = true

    fun init(context: Context) {
        soundMap = hashMapOf()
        val audioAttributes =
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        mSoundPool = SoundPool.Builder().setMaxStreams(1)
            .setAudioAttributes(audioAttributes).build()
        soundMap[COMMON_NOTICE] = mSoundPool.load(context, R.raw.common_notice, 1)
        soundMap[URGENT_NOTICE] = mSoundPool.load(context, R.raw.urgent_notice, 1)
    }

    private fun playSound(sound: Int) {
        soundMap[sound]?.let { playingSound = mSoundPool.play(it, 1F, 1F, 0, 0, 1F) }
    }

    fun stop() {
        if (playingSound != -1) {
            mSoundPool.stop(playingSound)
        }
        vibrator?.cancel()
    }

    private fun vibrate(context: Context, isUrgent: Boolean) {
        val vibrationPattern = if (isUrgent) {
            longArrayOf(
                0, 500, 500, 500, 500, 500, 500,
                500, 500, 500, 500, 500, 500, 500, 500, 500, 500
            )
        } else {
            longArrayOf(0, 500, 500, 500, 500)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator = vibratorManager.defaultVibrator
            vibrator?.vibrate(
                VibrationEffect.createWaveform(
                    vibrationPattern,
                    -1
                )
            )
        } else {
            vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator?.vibrate(vibrationPattern, -1)
        }
    }

    fun alert(context: Context, index: Int, isUrgent: Boolean) {
        when (index) {
            0 -> playSound(if (isUrgent) URGENT_NOTICE else COMMON_NOTICE)
            1 -> vibrate(context, isUrgent)
            2 -> {
                AlertUtil.apply {
                    this.playSound(if (isUrgent) URGENT_NOTICE else COMMON_NOTICE)
                    this.vibrate(context, isUrgent)
                }
            }
        }
    }

    fun setAlertMethod(index: Int) = AidexxApp.mainScope.launch(Dispatchers.IO) {
        val alertSettings = getSettings()
        alertSettings.alertType = index
        saveSetting(alertSettings)
    }

    fun setAlertFrequency(index: Int) = AidexxApp.mainScope.launch(Dispatchers.IO) {
        val alertSettings = getSettings()
        alertSettings.alertRate = index
        alertFrequency = index.minutesToMillis()
        saveSetting(alertSettings)
    }

    fun setHypoEnable(enable: Boolean) = AidexxApp.mainScope.launch(Dispatchers.IO) {
        val alertSettings = getSettings()
        alertSettings.lowAlertSwitch = if (enable) 0 else 1
        hypoSwitchEnable = enable
        saveSetting(alertSettings)
    }

    fun setHypoThreshold(value: Float) = AidexxApp.mainScope.launch(Dispatchers.IO) {
        val alertSettings = getSettings()
        alertSettings.lowLimitMg = value
        saveSetting(alertSettings)
    }

    fun setHyperEnable(value: Boolean) = AidexxApp.mainScope.launch(Dispatchers.IO) {
        val alertSettings = getSettings()
        alertSettings.highAlertSwitch = if (value) 0 else 1
        hyperSwitchEnable = value
        saveSetting(alertSettings)
    }

    fun setHyperThreshold(value: Float) = AidexxApp.mainScope.launch(Dispatchers.IO) {
        val alertSettings = getSettings()
        alertSettings.highLimitMg = value
        saveSetting(alertSettings)
    }

    fun setFastUpEnable(enable: Boolean) = AidexxApp.mainScope.launch(Dispatchers.IO) {
        val alertSettings = getSettings()
        alertSettings.fastUpSwitch = enable
        saveSetting(alertSettings)
    }

    fun setFastDownEnable(enable: Boolean) = AidexxApp.mainScope.launch(Dispatchers.IO) {
        val alertSettings = getSettings()
        alertSettings.isFastDownEnable = if (enable) 0 else 1
        saveSetting(alertSettings)
    }

    fun setUrgentEnable(enable: Boolean) = AidexxApp.mainScope.launch(Dispatchers.IO) {
        val alertSettings = getSettings()
        alertSettings.fastDownSwitch = if (enable) 0 else 1
        urgentLowSwitchEnable = enable
        saveSetting(alertSettings)
    }

    fun setUrgentAlertMethod(index: Int) = AidexxApp.mainScope.launch(Dispatchers.IO) {
        val alertSettings = getSettings()
        alertSettings.urgentAlertType = index
        saveSetting(alertSettings)
    }

    fun setUrgentFrequency(minutes: Int) = AidexxApp.mainScope.launch(Dispatchers.IO) {
        val alertSettings = getSettings()
        alertSettings.urgentAlertRate = minutes
        urgentFrequency = minutes.minutesToMillis()
        saveSetting(alertSettings)
    }

    fun setSignalLossEnable(enable: Boolean) = AidexxApp.mainScope.launch(Dispatchers.IO) {
        val alertSettings = getSettings()
        alertSettings.signalMissingSwitch = if (enable) 0 else 1
        saveSetting(alertSettings)
    }

    fun setSignalLossMethod(index: Int) = AidexxApp.mainScope.launch(Dispatchers.IO) {
        val alertSettings = getSettings()
        alertSettings.signalMissingAlertType = index
        saveSetting(alertSettings)
    }

    fun setSignalLossFrequency(index: Int) = AidexxApp.mainScope.launch(Dispatchers.IO) {
        val alertSettings = getSettings()
        alertSettings.signalMissingAlertRate = index
        saveSetting(alertSettings)
    }

}