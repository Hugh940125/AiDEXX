package com.microtech.aidexx.common.net.convert

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import okhttp3.ResponseBody
import retrofit2.Converter
import java.io.IOException


internal class GsonResponseBodyConverter<T>(
    private val gson: Gson,
    private val adapter: TypeAdapter<out T>,
    private val checkBizCodeIsSuccess: ((bodyStr: String) -> Throwable?)? = null,
    private val afterConvert: ((result: T?) -> Unit)? = null
) :
    Converter<ResponseBody, T> {
    @Throws(IOException::class)
    override fun convert(value: ResponseBody): T {

        return value.use {
            val bodyStr = value.string()

            checkBizCodeIsSuccess?.invoke(bodyStr)?.let {
                throw it
            }

            val result: T = adapter.fromJson(bodyStr)

            afterConvert?.invoke(result)

            result
        }
    }
}
