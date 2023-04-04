package com.microtech.aidexx.common.net

import com.microtech.aidexx.common.net.entity.ReqChangePWD
import com.microtech.aidexx.common.net.entity.ReqChangePWDVerifyCode
import com.microtech.aidexx.utils.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.FileOutputStream

object ApiRepository {

    private val dispatcher = Dispatchers.IO

    sealed class NetResult<out R> {
        data class Loading(var value:Int): NetResult<Nothing>()
        data class Success<out R>(val result: R): NetResult<R>()
        data class Failure(val code:String, val msg:String): NetResult<Nothing>()
    }

    suspend fun checkAppUpdate() = withContext(dispatcher) {
        val appId = "cn" // 国际版再改
        ApiService.instance.checkAppUpdate(appId)
    }


    /**
     * 文件下载
     */
    fun downloadFile(downloadUrl: String, downloadPath: String, fileName:String) = flow<NetResult<String>> {

        emit(NetResult.Loading(0))
        when (val apiResult = ApiService.instance.downloadFile(downloadUrl)){
            is ApiResult.Success -> {

                var bufferedInputStream: BufferedInputStream? = null
                var outPutStream: FileOutputStream? = null

                try {

                    val responseBody = apiResult.result
                    val length = responseBody.contentLength()

                    val targetFile = java.io.File(downloadPath, fileName)
                    if(targetFile.exists()){
                        targetFile.delete()
                    }

                    outPutStream = FileOutputStream(targetFile)

                    var currentLength = 0
                    val bufferSize = 1024 * 8
                    val buffer = ByteArray(bufferSize)
                    bufferedInputStream = BufferedInputStream(responseBody.byteStream(),bufferSize)
                    var readLength: Int
                    while ( responseBody.byteStream().read(buffer,0,bufferSize).also { readLength = it } != -1){
                        outPutStream.write(buffer,0,readLength)
                        currentLength += readLength
                        emit(NetResult.Loading(((currentLength / length.toFloat()) * 100).toInt()))
                    }

                    emit(NetResult.Success(targetFile.absolutePath))

                }catch (e:Exception){
                    LogUtil.xLogE(e.message?:e.toString(), "ApiRepository")
                    emit(NetResult.Failure("0", "fail"))
                }finally {
                    bufferedInputStream?.close()
                    outPutStream?.close()
                }

            }
            is ApiResult.Failure -> {
                emit(NetResult.Failure(apiResult.code, apiResult.msg))
            }
        }

    }.flowOn(Dispatchers.IO)

    suspend fun getChangePWDVerifyCode(phoneNumber: String) = withContext(dispatcher) {
        val body = ReqChangePWDVerifyCode(phoneNumber)
        ApiService.instance.getChangePWDVerCode(body)
    }

    suspend fun changePWD(phoneNumber: String, pwdEncrypted: String, verifyCode: String) = withContext(dispatcher) {
        val body = ReqChangePWD(phoneNumber, pwdEncrypted, verifyCode)
        ApiService.instance.changePWD(body)
    }

}