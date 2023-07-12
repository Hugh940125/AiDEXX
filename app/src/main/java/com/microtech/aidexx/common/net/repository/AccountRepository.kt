package com.microtech.aidexx.common.net.repository

import android.net.Uri
import androidx.core.net.toFile
import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.common.net.entity.ReqChangePWD
import com.microtech.aidexx.common.net.entity.ReqEmailRegister
import com.microtech.aidexx.common.net.entity.ReqGetuiLogin
import com.microtech.aidexx.common.net.entity.ReqPhoneCodeLogin
import com.microtech.aidexx.common.net.entity.ReqPhoneVerCode
import com.microtech.aidexx.common.net.entity.ReqPwdLogin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

object AccountRepository {

    private val dispatcher = Dispatchers.IO

    suspend fun sendRegisterEmailVerificationCode(email: String) = withContext(dispatcher) {
        ApiService.instance.sendRegisterEmailVerificationCode(email)
    }

    suspend fun registerByVerificationCodeWithEmail(email: String, code: String, password: String) = withContext(dispatcher) {
        ApiService.instance.registerByVerificationCodeWithEmail(ReqEmailRegister(email, password, code))
    }
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

    suspend fun logout() = withContext(dispatcher) {
        ApiService.instance.logout()
    }

    suspend fun getuiLogin(clientId: String) = withContext(dispatcher) {
        ApiService.instance.getuiLogin(ReqGetuiLogin(clientId))
    }

    suspend fun userUploadAvatar(fileUri: Uri) = withContext(dispatcher) {
        val file = fileUri.toFile()
        val fileBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, fileBody)
        ApiService.instance.userUploadAvatar(part)
    }

    suspend fun updateUserInformation(
        avatar: String? = null,
        name: String? = null,
        fullName: String? = null,
        surname: String? = null,
        middleName: String? = null,
        givenName: String? = null,
        gender: Int? = null,
        birthDate: String? = null,
        height: Int? = null,
        bodyWeight: Int? = null,
        diabetesType: Int? = null,
        diagnosisDate: String? = null,
        complications: String? = null,
    ) = withContext(dispatcher) {
        val map = HashMap<String, Any?>()
        avatar?.let { map["avatar"] = it }
        name?.let { map["name"] = it }
        fullName?.let { map["fullName"] = it }
        surname?.let { map["surname"] = it }
        middleName?.let { map["middleName"] = it }
        givenName?.let { map["givenName"] = it }
        gender?.let { map["gender"] = it }
        birthDate?.let { map["birthDate"] = it }
        height?.let { map["height"] = it }
        bodyWeight?.let { map["bodyWeight"] = it }
        diabetesType?.let { map["diabetesType"] = it }
        diagnosisDate?.let { map["diagnosisDate"] = it }
        complications?.let { map["complications"] = it }
        ApiService.instance.updateUserInformation(map)
    }
}