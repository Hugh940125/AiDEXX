package com.microtech.aidexx.common.net.convert

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import okhttp3.ResponseBody
import retrofit2.Converter
import java.io.IOException


internal class GsonResponseBodyConverter<T>(
    private val gson: Gson,
    private val adapter: TypeAdapter<out T>,
    private val checkBizCodeIsSuccess: ((bodyStr: String) -> Throwable?)? = null
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

//            val jsonReader = gson.newJsonReader(value.charStream())
//            val result1: T = adapter.read(jsonReader)
//            if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
//                throw JsonIOException("JSON document was not fully consumed.")
//            }

            result
        }
    }
}
