package com.microtech.aidexx.common.net.interceptors

import com.microtech.aidexx.common.net.RSAUtil
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody

class DecryptInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        return if (response.body != null && response.body!!.contentType() != null) {
            val mediaType: MediaType? = response.body!!.contentType()
            var content = response.body!!.string()
            content = RSAUtil.decryptByPrivateKey(content, RSAUtil.getPrivateKey(RSAUtil.PRIVATE_KEY))
            val responseBody: ResponseBody = content.toResponseBody(mediaType)
            response.newBuilder().body(responseBody).build()
        } else {
            response
        }
    }
}