package com.microtech.aidexx.ui.setting.profile

import androidx.lifecycle.ViewModel
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.repository.AccountRepository
import com.microtech.aidexx.common.user.UserInfoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ProfileViewModel: ViewModel() {

    suspend fun modifyProfileInfo(
        name: String? = null,
        fullName: String? = null,
        height: Int? = null,
        bodyWeight: Int? = null,
        gender: Int? = null,
        birthDate: String? = null,
    ) = flow {
        when (val ret = AccountRepository.updateUserInformation(
            name = name,
            fullName = fullName,
            height = height,
            bodyWeight = bodyWeight,
            gender = gender,
            birthDate = birthDate,
        )) {
            is ApiResult.Success -> {
                UserInfoManager.instance().updateProfile(name, fullName, height, bodyWeight, gender, birthDate)
                emit(0 to "0")
            }
            is ApiResult.Failure -> emit(1 to ret.code)
        }
    }.flowOn(Dispatchers.IO)

}