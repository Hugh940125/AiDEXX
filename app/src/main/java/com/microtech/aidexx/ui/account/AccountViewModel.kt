package com.microtech.aidexx.ui.account

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.common.net.entity.BaseResponse
import com.microtech.aidexx.common.net.entity.LoginInfo
import com.microtech.aidexx.db.entity.TransmitterEntity
import com.microtech.aidexx.ui.account.entity.UserPreferenceEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    fun login(
        map: HashMap<String, String>,
        success: ((info: BaseResponse<LoginInfo>) -> Unit)? = null,
        failure: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                when (val apiResult = ApiService.instance.login(map)) {
                    is ApiResult.Success -> {
                        apiResult.result.let { result ->
                            withContext(Dispatchers.Main) {
                                success?.invoke(result)
                            }
                        }
                    }
                    is ApiResult.Failure -> {
                        withContext(Dispatchers.Main) {
                            failure?.invoke()
                        }
                    }
                }
            }
        }
    }

    fun getVerCode(
        map: HashMap<String, String>,
        success: ((info: BaseResponse.Info) -> Unit)? = null,
        failure: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                when (val apiResult = ApiService.instance.getVerCode(map)) {
                    is ApiResult.Success -> {
                        apiResult.result.let { result ->
                            withContext(Dispatchers.Main) {
                                success?.invoke(result)
                            }
                        }
                    }
                    is ApiResult.Failure -> {
                        withContext(Dispatchers.Main) {
                            failure?.invoke()
                        }
                    }
                }
            }
        }
    }

    fun getUserPreference(
        success: ((info: BaseResponse<MutableList<UserPreferenceEntity>>) -> Unit)? = null,
        failure: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                when (val apiResult = ApiService.instance.getUserPreference()) {
                    is ApiResult.Success -> {
                        apiResult.result.let { result ->
                            withContext(Dispatchers.Main) {
                                success?.invoke(result)
                            }
                        }
                    }
                    is ApiResult.Failure -> {
                        withContext(Dispatchers.Main) {
                            failure?.invoke()
                        }
                    }
                }
            }
        }
    }
    fun getDevice(
        success: ((info: BaseResponse<TransmitterEntity>) -> Unit)? = null,
        failure: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                when (val apiResult = ApiService.instance.getDevice()) {
                    is ApiResult.Success -> {
                        apiResult.result.let { result ->
                            withContext(Dispatchers.Main) {
                                success?.invoke(result)
                            }
                        }
                    }
                    is ApiResult.Failure -> {
                        withContext(Dispatchers.Main) {
                            failure?.invoke()
                        }
                    }
                }
            }
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