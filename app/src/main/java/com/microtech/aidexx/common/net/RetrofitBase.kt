package com.microtech.aidexx.common.net

import com.microtech.aidexx.utils.LogUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

internal const val TAG_API_SERVICE = "RETROFIT_BASE"

sealed class ApiResult<out R> {

    data class Success<out R>(val result: R) : ApiResult<R>() {
        override fun toString(): String {
            return "ApiResult.Success(result=${result.toString()})"
        }
    }

    data class Failure(val code: String, val msg: String) : ApiResult<Nothing>() {

        override fun toString(): String {
            return "ApiResult.Failure(code=$code, msg=$msg)"
        }
    }

    companion object {
        const val ERR_CODE_DATA_NULL = "0"
        const val ERR_CODE_HTTP_CODE = "1"
        const val ERR_CODE_REQUEST_FAILURE = "2"
        const val ERR_CODE_SYSTEM = "3"
    }
}

internal class ApiCallAdapterFactory : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *> {
        check(getRawType(returnType) == Call::class.java) { "$returnType must be retrofit2.Call." }
        check(returnType is ParameterizedType) { "$returnType must be parameterized. Raw types are not supported" }

        val apiResultType = getParameterUpperBound(0, returnType)
        check(getRawType(apiResultType) == ApiResult::class.java) { "$apiResultType must be ApiResult." }
        check(apiResultType is ParameterizedType) { "$apiResultType must be parameterized. Raw types are not supported" }

        val dataType = getParameterUpperBound(0, apiResultType)
        return ApiCallAdapter<Any>(dataType)

    }

}

internal class ApiCallAdapter<T>(private val type: Type) : CallAdapter<T, Call<ApiResult<T>>> {
    override fun responseType(): Type = type

    override fun adapt(call: Call<T>): Call<ApiResult<T>> {
        return ApiCall(call)
    }
}

internal class ApiCall<T>(private val delegate: Call<T>) : Call<ApiResult<T>> {
    override fun enqueue(callback: Callback<ApiResult<T>>) {
        delegate.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    val apiResult = if (response.body() == null) {
                        ApiResult.Failure(ApiResult.ERR_CODE_DATA_NULL, "")
                    } else {
                        ApiResult.Success(response.body()!!)
                    }
                    callback.onResponse(this@ApiCall, Response.success(apiResult))
                } else {
                    val apiResult =
                        ApiResult.Failure(ApiResult.ERR_CODE_HTTP_CODE, "${response.code()}")
                    callback.onResponse(this@ApiCall, Response.success(apiResult))
                }

            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                LogUtil.xLogE("ApiCall on Failure t=$t t.message=${t.localizedMessage} request=${request()} }", TAG_API_SERVICE)

                when (t) {
                    is BizException -> callback.onResponse(
                        this@ApiCall,
                        Response.success(ApiResult.Failure(t.code.toString(), t.message ?: ""))
                    )
                    else -> callback.onResponse(
                        this@ApiCall,
                        Response.success(
                            ApiResult.Failure(
                                ApiResult.ERR_CODE_REQUEST_FAILURE,
                                t.localizedMessage ?: ""
                            )
                        )
                    )
                }
            }

        })
    }

    override fun clone(): Call<ApiResult<T>> = ApiCall(delegate.clone())

    override fun execute(): Response<ApiResult<T>> {
        throw UnsupportedOperationException("Synchronous call is not supported")
    }

    override fun isExecuted(): Boolean = delegate.isExecuted

    override fun cancel() = delegate.cancel()

    override fun isCanceled(): Boolean = delegate.isCanceled

    override fun request(): Request = delegate.request()

    override fun timeout(): Timeout = delegate.timeout()

}

fun buildRetrofit(
    baseUrl: String,
    convertFactory: Converter.Factory,
    client: OkHttpClient? = null,
    apiCallAdapterFactory: CallAdapter.Factory = ApiCallAdapterFactory()
): Retrofit = Retrofit.Builder()
    .addCallAdapterFactory(apiCallAdapterFactory)
    .also { rb ->
        client?.let { rb.client(it) }
    }
    .addConverterFactory(convertFactory)
    .baseUrl(baseUrl)
    .build()
