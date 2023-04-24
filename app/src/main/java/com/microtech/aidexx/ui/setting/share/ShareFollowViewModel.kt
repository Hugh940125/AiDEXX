package com.microtech.aidexx.ui.setting.share

import androidx.lifecycle.ViewModel
import com.microtech.aidexx.db.entity.ShareUserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ShareFollowViewModel: ViewModel() {


    suspend fun loadData(isShare: Boolean) = flow {

        delay(600)

        val items = mutableListOf<ShareUserEntity>()
        for (i in 1..5) {
            items.add(ShareUserEntity().also {
                it.id = "0000$i"
                it.userAlias = "$i"
            })
        }

        emit(items)
    }.flowOn(Dispatchers.IO)


    suspend fun shareMyselfToOther() = flow {
        emit("")
    }.flowOn(Dispatchers.IO)


    suspend fun getQrCodeToShareMySelf() = flow {
        emit("")
    }.flowOn(Dispatchers.IO)


    suspend fun modifyShareUser() = flow {
        emit("")
    }.flowOn(Dispatchers.IO)

    suspend fun modifyFollowUser() = flow {
        emit("")
    }.flowOn(Dispatchers.IO)

    suspend fun cancelShare() = flow {
        emit(false)
    }.flowOn(Dispatchers.IO)

}