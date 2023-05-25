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
import com.microtech.aidexx.common.equal
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.entity.AlertSettingsEntity
import com.microtech.aidexx.db.entity.AlertSettingsEntity_
import io.objectbox.kotlin.awaitCallInTx
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
    private var alertSettingEntity: AlertSettingsEntity? = null
    private lateinit var soundMap: HashMap<Int, Int>
    private lateinit var mSoundPool: SoundPool
    private var playingSound: Int = -1
    private var vibrator: Vibrator? = null
    var alertFrequency:Long = calculateFrequency(2)
    var urgentFrequency: Long = calculateFrequency(0)


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

    fun calculateFrequency(index: Int): Long {
        //"5分钟", "15分钟", "30分钟", "45分钟", "60分钟"
        return when (index) {
            0 -> 5 * 60 * 1000
            1 -> 15 * 60 * 1000
            2 -> 30 * 60 * 1000
            3 -> 45 * 60 * 1000
            4 -> 60 * 60 * 1000
            else -> 30 * 60 * 1000
        }
    }

    suspend fun loadSettingsFromDb(): AlertSettingsEntity? {
        return ObjectBox.store.awaitCallInTx {
            val findFirst = ObjectBox.AlertSettingsBox!!.query()
                .equal(
                    AlertSettingsEntity_.authorizationId,
                    UserInfoManager.instance().userId()
                )
                .orderDesc(AlertSettingsEntity_.idx)
                .build()
                .findFirst()
            findFirst?.let {
                alertFrequency = calculateFrequency(it.alertFrequency)
                urgentFrequency = calculateFrequency(it.urgentAlertFrequency)
            }
            findFirst
        }
    }

    suspend fun getAlertSettings(): AlertSettingsEntity {
        if (alertSettingEntity == null) {
            alertSettingEntity = loadSettingsFromDb() ?: AlertSettingsEntity(UserInfoManager.instance().userId())
        }
        return alertSettingEntity ?: AlertSettingsEntity(UserInfoManager.instance().userId())
    }

    private fun save(alertSettings: AlertSettingsEntity) {
        alertSettings.needSync = true
        ObjectBox.AlertSettingsBox!!.put(alertSettings)
    }

    fun setAlertMethod(index: Int) {
        AidexxApp.mainScope.launch(Dispatchers.IO) {
            val alertSettings = getAlertSettings()
            alertSettings.alertMethod = index
            save(alertSettings)
        }
    }

    fun setAlertFrequency(index: Int) {
        AidexxApp.mainScope.launch(Dispatchers.IO) {
            val alertSettings = getAlertSettings()
            alertSettings.alertFrequency = index
            alertFrequency = calculateFrequency(index)
            save(alertSettings)
        }
    }

    fun setHypoEnable(enable: Boolean) {
        AidexxApp.mainScope.launch(Dispatchers.IO) {
            val alertSettings = getAlertSettings()
            alertSettings.isHypoEnable = enable
            save(alertSettings)
        }
    }

    fun setHypoThreshold(value: Float) {
        AidexxApp.mainScope.launch(Dispatchers.IO) {
            val alertSettings = getAlertSettings()
            alertSettings.hypoThreshold = value
            save(alertSettings)
        }
    }

    fun setHyperEnable(value: Boolean) {
        AidexxApp.mainScope.launch(Dispatchers.IO) {
            val alertSettings = getAlertSettings()
            alertSettings.isHyperEnable = value
            save(alertSettings)
        }
    }

    fun setHyperThreshold(value: Float) {
        AidexxApp.mainScope.launch(Dispatchers.IO) {
            val alertSettings = getAlertSettings()
            alertSettings.hyperThreshold = value
            save(alertSettings)
        }
    }

    fun setFastUpEnable(enable: Boolean) {
        AidexxApp.mainScope.launch(Dispatchers.IO) {
            val alertSettings = getAlertSettings()
            alertSettings.isFastUpEnable = enable
            save(alertSettings)
        }
    }

    fun setFastDownEnable(enable: Boolean) {
        AidexxApp.mainScope.launch(Dispatchers.IO) {
            val alertSettings = getAlertSettings()
            alertSettings.isFastDownEnable = enable
            save(alertSettings)
        }
    }

    fun setUrgentEnable(enable: Boolean) {
        AidexxApp.mainScope.launch(Dispatchers.IO) {
            val alertSettings = getAlertSettings()
            alertSettings.isUrgentLowEnable = enable
            save(alertSettings)
        }
    }

    fun setUrgentAlertMethod(index: Int) {
        AidexxApp.mainScope.launch(Dispatchers.IO) {
            val alertSettings = getAlertSettings()
            alertSettings.urgentAlertMethod = index
            save(alertSettings)
        }
    }

    fun setUrgentFrequency(index: Int) {
        AidexxApp.mainScope.launch(Dispatchers.IO) {
            val alertSettings = getAlertSettings()
            alertSettings.urgentAlertFrequency = index
            urgentFrequency = calculateFrequency(index)
            save(alertSettings)
        }
    }

    fun setSignalLossEnable(enable: Boolean) {
        AidexxApp.mainScope.launch(Dispatchers.IO) {
            val alertSettings = getAlertSettings()
            alertSettings.isSignalLossEnable = enable
            save(alertSettings)
        }
    }

    fun setSignalLossMethod(index: Int) {
        AidexxApp.mainScope.launch(Dispatchers.IO) {
            val alertSettings = getAlertSettings()
            alertSettings.signalLossMethod = index
            save(alertSettings)
        }
    }

    fun setSignalLossFrequency(index: Int) {
        AidexxApp.mainScope.launch(Dispatchers.IO) {
            val alertSettings = getAlertSettings()
            alertSettings.signalLossFrequency = index
            save(alertSettings)
        }
    }
}