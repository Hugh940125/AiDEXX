package com.microtech.aidexx.common.net.interceptors

import com.microtech.aidexx.BuildConfig
import com.microtech.aidexx.utils.mmkv.MmkvManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 根据不同请求添加不同参数，子线程执行，每次发送请求前都会被回调
 * todo: 如果希望部分请求不回调这里
 */
class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        val original = chain.request()
        var newRequest = original.newBuilder()
        val method = original.method
        if (method.equals("get", ignoreCase = true)) { //Get请求
            val newUrl = original.url.newBuilder()
                .addQueryParameter("pageSize", "200")
                .addQueryParameter("sortOrder", "DESC")
                .build()
            newRequest = newRequest.url(newUrl)
        }
        if (BuildConfig.isPre) {
            newRequest.header("version", "test")
        }
        val token = MmkvManager.getToken()
        val select = 0
        if (token.isNotEmpty()) {
            newRequest.header("x-token", token) //添加公共请求头
        }
        newRequest.header("version", "watch-code")
        newRequest.header("encryption", if (BuildConfig.enableEncrypt) "enabled" else "disable")
        newRequest.header("app-info", "${BuildConfig.APPLICATION_ID},${BuildConfig.VERSION_NAME}")
        when (select) {
            0 -> newRequest.header("Accept-Language", "en-US")
            1 -> newRequest.header("Accept-Language", "zh-cn")
            else -> newRequest.header("Accept-Language", "zh-cn")
        }
        return chain.proceed(newRequest.build())
    }
}