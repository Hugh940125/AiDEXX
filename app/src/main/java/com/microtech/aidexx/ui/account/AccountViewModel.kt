package com.microtech.aidexx.ui.account

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import com.microtech.aidexx.base.BaseViewModel

/**
 *@date 2023/2/20
 *@author Hugh
 *@desc
 */
class AccountViewModel : BaseViewModel() {

    val timeLeft by lazy {
        MutableLiveData<Pair<Boolean, Int>>()
    }

    private val countDownTimer = object : CountDownTimer(60 * 1000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            timeLeft.value = Pair(true, (millisUntilFinished / 1000).toInt())
        }

        override fun onFinish() {
            timeLeft.value = Pair(false, 0)
        }
    }

    fun startCountDown() {
        countDownTimer.start()
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer.cancel()
    }
}