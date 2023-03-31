package com.microtech.aidexx.ui.setting.alert

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.microtech.aidexx.R

const val COMMON_NOTICE = 0
const val URGENT_NOTICE = 1

object AlertUtil {
    private lateinit var soundMap: HashMap<Int, Int>
    private lateinit var mSoundPool: SoundPool
    private var playingSound: Int = -1

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

    fun playSound(sound: Int) {
        soundMap[sound]?.let { playingSound = mSoundPool.play(it, 1F, 1F, 0, 0, 1F) }
    }

    fun stopPlay() {
        if (playingSound != -1) {
            mSoundPool.stop(playingSound)
        }
    }

    fun vibrate(context: Context, isUrgent: Boolean) {
        val vibrationPattern = if (isUrgent) {
            longArrayOf(
                0, 500, 500, 500, 500, 500, 500,
                500, 500, 500, 500, 500, 500, 500, 500, 500, 500
            )
        } else {
            longArrayOf(0, 500, 500, 500, 500)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, -1))
        } else {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(vibrationPattern, -1)
        }
    }
}