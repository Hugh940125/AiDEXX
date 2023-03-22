package com.microtech.aidexx.common.net


import com.google.gson.*
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.BuildConfig
import com.microtech.aidexx.common.net.convert.GsonConverterFactory
import com.microtech.aidexx.common.net.cookie.CookieStore
import com.microtech.aidexx.common.net.entity.BaseList
import com.microtech.aidexx.common.net.entity.BasePageList
import com.microtech.aidexx.common.net.entity.BaseResponse
import com.microtech.aidexx.common.net.entity.LoginInfo
import com.microtech.aidexx.common.net.interceptors.EncryptInterceptor
import com.microtech.aidexx.common.net.interceptors.HeaderInterceptor
import com.microtech.aidexx.common.net.interceptors.LogInterceptor
import com.microtech.aidexx.common.net.interceptors.TokenInterceptor
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import com.microtech.aidexx.db.entity.TransmitterEntity
import com.microtech.aidexx.ui.account.entity.UserPreferenceEntity
import com.microtech.aidexx.utils.Throttle
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtechmd.cgms.data.api.interceptors.DecryptInterceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import java.io.File
import java.lang.reflect.Type
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

const val middleUrl = "backend/aidex/api"
const val API_DEVICE_REGISTER = "$middleUrl/cgm-device/register" //注册设备
const val API_DEVICE_UNREGISTER = "$middleUrl/cgm-device/unregister" //注销设备
const val LOGIN_VERIFICATION_CODE = "$middleUrl/login-verification-code" //验证码
const val LOGIN = "$middleUrl/login" //登录
const val DEVICE = "$middleUrl/cgn-device" //获取设备
const val USER_PREFERENCE = "$middleUrl/user-preference" //
const val UPLOAD_CGM_RECORD = "$middleUrl/cgm-record" //上传CGM
const val DOWNLOAD_CGM_RECORD = "$middleUrl/cgm-record/list" //下载CGM

interface ApiService {
    @POST(DOWNLOAD_CGM_RECORD)
    suspend fun getRemoteHistory(@Body json: String): Call<BaseResponse<BasePageList<RealCgmHistoryEntity>>>

    @POST(UPLOAD_CGM_RECORD)
    suspend fun postHistory(@Body json: String): Call<BaseResponse<BaseList<RealCgmHistoryEntity>>>

    @PUT(UPLOAD_CGM_RECORD)
    suspend fun putHistory(@Body json: String): Call<BaseResponse<BaseList<RealCgmHistoryEntity>>>

    @GET(USER_PREFERENCE)
    suspend fun getUserPreference(): ApiResult<BaseResponse<MutableList<UserPreferenceEntity>>>

    @POST(LOGIN)
    suspend fun login(@Body map: HashMap<String, String>): ApiResult<BaseResponse<LoginInfo>>

    @POST(LOGIN_VERIFICATION_CODE)
    suspend fun getVerCode(@Body map: HashMap<String, String>): ApiResult<BaseResponse.Info>

    @GET(DEVICE)
    suspend fun getDevice(): ApiResult<BaseResponse<TransmitterEntity>>

    @POST(API_DEVICE_REGISTER)
    suspend fun deviceRegister(@Body entity: TransmitterEntity): ApiResult<TransmitterEntity>

    @POST(API_DEVICE_UNREGISTER)
    suspend fun deviceUnregister(@Body map: HashMap<String, String>): ApiResult<TransmitterEntity>

    companion object {
        private val okClient by lazy { getOkHttpClient() }
        private val gson by lazy { Gson() }

        val instance: ApiService by lazy {
            buildRetrofit(
                BuildConfig.baseUrl,
                GsonConverterFactory.create(createGson(), checkBizCodeIsSuccess = {
                    val baseResponse = gson.fromJson(it, BaseResponse::class.java)
                    var ret: Throwable? = null
                    baseResponse.info.let { info ->
                        info.code.let { code ->
                            if (code != 100000) {
                                if (code in 800..806) {
                                    Throttle.instance().emit(5000, code) {
                                        EventBusManager.send(EventBusKey.TOKEN_EXPIRED, true)
                                    }
                                } else {
                                    ret = BizException(code, message = info.msg)
                                }
                            }
                        }
                    }
                    ret
                }),
                client = okClient
            ).create(ApiService::class.java)
        }

        private fun getOkHttpClient(): OkHttpClient {
            val file = File(AidexxApp.instance.externalCacheDir, "RxHttpCookie")
            val sslParams = HttpsUtil.getSslSocketFactory()
            val clientBuilder = OkHttpClient.Builder()
                .cookieJar(CookieStore(file))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .sslSocketFactory(sslParams!!.sSLSocketFactory, sslParams.trustManager) //添加信任证书
                .hostnameVerifier(HostnameVerifier { hostname: String?, session: SSLSession? -> true }) //忽略host验证
                //            .followRedirects(false)  //禁制OkHttp的重定向操作，我们自己处理重定向
                //            .addInterceptor(new RedirectInterceptor())
                .addInterceptor(HeaderInterceptor())
                .addInterceptor(TokenInterceptor())
                .addInterceptor(LogInterceptor())
            return if (BuildConfig.enableEncrypt) {
                clientBuilder
                    .addInterceptor(EncryptInterceptor())
                    .addInterceptor(DecryptInterceptor())
                    .build()
            } else {
                clientBuilder.build()
            }
        }

        /**
         * json解析时指定类型格式化
         */
        private fun createGson(): Gson {
            return GsonBuilder().registerTypeAdapter(
                Float.Companion::class.java,
                object : JsonSerializer<Float> {
                    override fun serialize(
                        value: Float?,
                        typeOfSrc: Type?,
                        context: JsonSerializationContext?
                    ): JsonElement {
                        return if (value != null) {
                            if (value.isNaN()) {
                                JsonPrimitive(0) // Convert NaN to zero
                            } else if (value.isInfinite() || value < 0.01) {
                                JsonPrimitive(0) // Leave small numbers and infinite alone
                            } else {
                                // Keep 2 decimal digits only
                                JsonPrimitive(
                                    DecimalFormat(".00").format(value)
                                )
                            }
                        } else {
                            JsonPrimitive(0) // Convert NaN to zero
                        }
                    }
                })
                .setLongSerializationPolicy(LongSerializationPolicy.STRING)
                .setDateFormat("yyyy-MM-dd HH:mm:ssZ")
                .create()
        }
    }
}
