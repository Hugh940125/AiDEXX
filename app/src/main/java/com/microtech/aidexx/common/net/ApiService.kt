package com.microtech.aidexx.common.net


import com.google.gson.*
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.BuildConfig
import com.microtech.aidexx.common.net.convert.GsonConverterFactory
import com.microtech.aidexx.common.net.cookie.CookieStore
import com.microtech.aidexx.common.net.entity.*
import com.microtech.aidexx.common.net.interceptors.*
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import com.microtech.aidexx.db.entity.ShareUserEntity
import com.microtech.aidexx.db.entity.TransmitterEntity
import com.microtech.aidexx.ui.account.entity.UserPreferenceEntity
import com.microtech.aidexx.utils.Throttle
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import java.io.File
import java.lang.reflect.Type
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

const val middleUrl = "/backend/aidex-v2"

// region 账号
const val USER_URL = "$middleUrl/user"
const val sendRegisterPhoneVerificationCode = "$USER_URL/sendRegisterPhoneVerificationCode" // 也可以使用sendLoginPhoneVerificationCode
const val sendLoginPhoneVerificationCode = "$USER_URL/sendLoginPhoneVerificationCode" // 也可以使用sendLoginPhoneVerificationCode
const val loginOrRegisterByVerificationCodeWithPhone = "$USER_URL/loginOrRegisterByVerificationCodeWithPhone"
const val loginByPassword = "$USER_URL/loginByPassword"
const val getUserInfo = "$USER_URL/getUserInfo"
const val sendResetPasswordPhoneVerificationCode = "$USER_URL/sendResetPasswordPhoneVerificationCode"
const val resetPasswordByVerificationCode = "$USER_URL/passCheckToken/resetPasswordByVerificationCode"
const val setPassword = "$USER_URL/setPassword"
const val getFollowers = "http://192.168.222.26:5555/backend/aidex/follows"
// endregion

const val API_DEVICE_REGISTER = "$middleUrl/cgm-device/register" //注册设备
const val API_DEVICE_UNREGISTER = "$middleUrl/cgm-device/unregister" //注销设备
const val DEVICE = "$middleUrl/cgn-device" //获取设备
const val USER_PREFERENCE = "$middleUrl/user-preference" //
const val UPLOAD_CGM_RECORD = "$middleUrl/cgm-record" //上传CGM
const val DOWNLOAD_CGM_RECORD = "$middleUrl/cgm-record/list" //下载CGM
const val CGM_LIST_RECENT = "$middleUrl/cgm-record/list-recent"

const val vcsMiddleUrl = "backend/vcs"
const val CHECK_APP_UPDATE = "$vcsMiddleUrl/version/getAppConfig" //APP版本升级检查
const val LOG_UPLOAD = "$vcsMiddleUrl/log/uploadLog" //上传日志

interface ApiService {


    //region 账户相关
    @POST(sendRegisterPhoneVerificationCode)
    suspend fun sendRegisterPhoneVerificationCode(@Body body: ReqPhoneVerCode): ApiResult<BaseResponse<Nothing>>

    @POST(sendLoginPhoneVerificationCode)
    suspend fun sendLoginPhoneVerificationCode(@Body body: ReqPhoneVerCode): ApiResult<BaseResponse<Nothing>>

    @POST(loginOrRegisterByVerificationCodeWithPhone)
    suspend fun loginOrRegisterByVerificationCodeWithPhone(@Body body: ReqPhoneCodeLogin): ApiResult<BaseResponse<ResLogin>>

    @POST(loginByPassword)
    suspend fun loginByPassword(@Body body: ReqPwdLogin): ApiResult<BaseResponse<ResLogin>>

    @GET(getUserInfo)
    suspend fun getUserInfo(): ApiResult<BaseResponse<ResUserInfo>>

    @POST(sendResetPasswordPhoneVerificationCode)
    suspend fun sendResetPasswordPhoneVerificationCode(@Body body: ReqPhoneVerCode): ApiResult<BaseResponse<String>>

    @POST(resetPasswordByVerificationCode)
    suspend fun resetPasswordByVerificationCode(@Body body: ReqChangePWD): ApiResult<BaseResponse<String>>
    @GET(getFollowers)
    suspend fun getFollowers(): ApiResult<BaseResponse<BaseList<ShareUserEntity>>>

    //endregion




    @GET("$CGM_LIST_RECENT?{params}")
    suspend fun getRecentHistories(@Path("params") params: String)
            : Call<BaseResponse<BasePageList<RealCgmHistoryEntity>>>

    @POST(DOWNLOAD_CGM_RECORD)
    suspend fun getRemoteHistory(@Body json: String): Call<BaseResponse<BasePageList<RealCgmHistoryEntity>>>

    @POST(UPLOAD_CGM_RECORD)
    suspend fun postHistory(@Body json: String): Call<BaseResponse<BaseList<RealCgmHistoryEntity>>>

    @PUT(UPLOAD_CGM_RECORD)
    suspend fun putHistory(@Body json: String): Call<BaseResponse<BaseList<RealCgmHistoryEntity>>>

    @GET(USER_PREFERENCE)
    suspend fun getUserPreference(): ApiResult<BaseResponse<MutableList<UserPreferenceEntity>>>

    @GET(DEVICE)
    suspend fun getDevice(): ApiResult<BaseResponse<TransmitterEntity>>

    @POST(API_DEVICE_REGISTER)
    suspend fun deviceRegister(@Body entity: TransmitterEntity): ApiResult<BaseResponse<TransmitterEntity>>

    @POST(API_DEVICE_UNREGISTER)
    suspend fun deviceUnregister(@Body map: HashMap<String, String>): ApiResult<TransmitterEntity>

    @GET(BuildConfig.updateUrl + CHECK_APP_UPDATE)
    suspend fun checkAppUpdate(
        @Query("appId") appId: String,
        @Query("project") project: String = "aidex",
        @Query("os") os: String = "android"
    ): ApiResult<AppUpdateInfo>

    @Streaming
    @GET
    suspend fun downloadFile(@Url url: String): ApiResult<ResponseBody>

    @POST(BuildConfig.updateUrl + LOG_UPLOAD)
    suspend fun uploadLog(/*todo 参数*/): ApiResult<BaseResponse<String>>

    companion object {
        private val okClient by lazy { getOkHttpClient() }
        private val gson by lazy { Gson() }

        val instance: ApiService by lazy {
            buildRetrofit(
                BuildConfig.baseUrl,
                GsonConverterFactory.create(createGson(), ::checkBizCodeIsSuccess),
                client = okClient
            ).create(ApiService::class.java)
        }

        /**
         * 响应在转实体之前做拦截判断业务是否成功
         */
        private fun checkBizCodeIsSuccess(bodyStr: String): Throwable? {
            val baseResponse = gson.fromJson(bodyStr, BaseResponse::class.java)
            var ret: Throwable? = null

            baseResponse.run {
                if (code != RESULT_OK) {
                    if (code == 800) {
                        Throttle.instance().emit(5000, code) {
                            EventBusManager.send(EventBusKey.TOKEN_EXPIRED, true)
                        }
                    }
                    ret = BizException(code, message = msg?.ifBlank { null } ?: "$code")
                }
            }
            return ret
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
