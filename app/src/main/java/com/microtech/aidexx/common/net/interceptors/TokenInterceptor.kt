package com.microtech.aidexx.common.net.interceptors


import android.util.Log
import com.microtech.aidexx.utils.mmkv.MmkvManager
import com.tencent.mmkv.MMKV
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


class TokenInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val originalResponse = chain.proceed(request)

        if (!originalResponse.header("x-token").isNullOrEmpty()) {
            val token = originalResponse.header("x-token")
            token?.let { MmkvManager.saveToken(it) }
        }
        return originalResponse
    }
}
