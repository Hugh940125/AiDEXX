package com.microtech.aidexx.ui.account

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.device.entity.CloudDeviceInfo
import com.microtech.aidexx.common.LOGIN_TYPE_EMAIL_VER_CODE
import com.microtech.aidexx.common.LOGIN_TYPE_PWD
import com.microtech.aidexx.common.LOGIN_TYPE_VER_CODE
import com.microtech.aidexx.common.LoginType
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.common.net.entity.BaseResponse
import com.microtech.aidexx.common.net.repository.AccountRepository
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.repository.AccountDbRepository
import com.microtech.aidexx.ui.account.entity.UserPreferenceEntity
import com.microtech.aidexx.utils.EncryptUtils
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.mmkv.MmkvManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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

    /**
     * @param type
     */
    fun login(name: String, password: String = "", verCode: String = "", @LoginType type: Int) = flow {

        val apiResult = when (type) {
            LOGIN_TYPE_VER_CODE -> AccountRepository.loginOrRegisterByVerificationCodeWithPhone(name, verCode)
            LOGIN_TYPE_PWD -> AccountRepository.loginByPassword(name, password)
            LOGIN_TYPE_EMAIL_VER_CODE -> AccountRepository.registerByVerificationCodeWithEmail(name, verCode, password)
            else -> null
        }

        when (apiResult) {
            is ApiResult.Success -> {
                apiResult.result.data?.token?.let {
                    MmkvManager.saveToken(it)
                } ?: let {
                    emit(-1 to "token为空 登录失败")
                    return@flow
                }
                // 拉取用户信息
                val userId = getUserInfo()
                if (userId.isNotEmpty()) {
                    emit(1 to "开始下载数据")
                    if (downloadData()) {
                        emit(2 to "登录成功")
                    } else {
                        // 清除token 用户信息
                        MmkvManager.saveToken("")
                        AccountDbRepository.removeUserByUId(userId)
                        emit(-1 to "数据下载失败")
                    }
                } else {
                    emit(-1 to "登录成功，用户信息拉取失败")
                }
            }
            is ApiResult.Failure -> {
                emit(-2 to apiResult.msg)
            }
            else -> emit(-1 to "暂不支持该方式登录")
        }
    }.flowOn(Dispatchers.IO)


    private suspend fun getUserInfo(): String {
        // 登录成功去拉用户详细信息
        return when (val userInfoApiResult = AccountRepository.getUserInfo()) {
            is ApiResult.Success -> {
                if (userInfoApiResult.result.data?.userId.isNullOrEmpty()) {
                    LogUtil.xLogE("拉用户信息接口返回的userid为空", TAG)
                    ""
                } else {
                    UserInfoManager.instance().onUserLogin(userInfoApiResult.result.data!!)
                    userInfoApiResult.result.data!!.userId!!
                }
            }
            else -> ""
        }
    }

    private suspend fun downloadData(): Boolean {
        delay(4000)
        return true
    }

    suspend fun sendRegisterPhoneVerificationCode(phone: String): Boolean =
        when (AccountRepository.sendRegisterPhoneVerificationCode(phone)) {
            is ApiResult.Success -> true
            is ApiResult.Failure -> false
        }

    fun getChangePWDVerifyCode(phoneNumber: String) = flow {
        when (AccountRepository.sendResetPasswordPhoneVerificationCode(phoneNumber)) {
            is ApiResult.Success -> emit(true)
            else -> emit(false)
        }
    }.flowOn(Dispatchers.IO)

    fun changePWD(phoneNumber: String, pwd: String, verifyCode: String) = flow {
        val pwdEncrypted = EncryptUtils.md5(pwd)
        when (val ret = AccountRepository.resetPasswordByVerificationCode(phoneNumber, pwdEncrypted, verifyCode)) {
            is ApiResult.Success -> emit(true to "")
            is ApiResult.Failure -> {
                emit(false to ret.msg)
            }
        }
    }.flowOn(Dispatchers.IO)

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

    fun startCountDown() {
        countDownTimer.start()
    }

    suspend fun sendRegisterEmailVerificationCode(phone: String): Boolean =
        when (AccountRepository.sendRegisterEmailVerificationCode(phone)) {
            is ApiResult.Success -> {
                countDownTimer.start()
                true
            }
            is ApiResult.Failure -> false
        }

    override fun onCleared() {
        super.onCleared()
        countDownTimer.cancel()
    }

    companion object {
        private const val TAG = "AccountViewModel"
    }
}