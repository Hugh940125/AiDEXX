package com.microtech.aidexx.common.net.repository

import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.common.net.entity.ReqChangePWD
import com.microtech.aidexx.common.net.entity.ReqPhoneCodeLogin
import com.microtech.aidexx.common.net.entity.ReqPhoneVerCode
import com.microtech.aidexx.common.net.entity.ReqPwdLogin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AccountRepository {

    private val dispatcher = Dispatchers.IO

    suspend fun sendRegisterPhoneVerificationCode(phone: String) = withContext(dispatcher) {
        ApiService.instance.sendRegisterPhoneVerificationCode(ReqPhoneVerCode(phone))
    }

    suspend fun loginOrRegisterByVerificationCodeWithPhone(phone: String, code: String) = withContext(
        dispatcher
    ) {
        ApiService.instance.loginOrRegisterByVerificationCodeWithPhone(ReqPhoneCodeLogin(phone,code))
    }

    suspend fun loginByPassword(phone: String, code: String) = withContext(dispatcher) {
        ApiService.instance.loginByPassword(ReqPwdLogin(phone,code))
    }

    suspend fun getUserInfo() = withContext(dispatcher) {
        ApiService.instance.getUserInfo()
    }

    suspend fun sendResetPasswordPhoneVerificationCode(phoneNumber: String) = withContext(dispatcher) {
        val body = ReqPhoneVerCode(phoneNumber)
        ApiService.instance.sendResetPasswordPhoneVerificationCode(body)
    }

    suspend fun resetPasswordByVerificationCode(phoneNumber: String, pwdEncrypted: String, verifyCode: String) = withContext(
        dispatcher
    ) {
        val body = ReqChangePWD(phoneNumber, pwdEncrypted, verifyCode)
        ApiService.instance.resetPasswordByVerificationCode(body)
    }

    suspend fun getFollowers() = withContext(dispatcher) {
        ApiService.instance.getFollowers()
    }
}