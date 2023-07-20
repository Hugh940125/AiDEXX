package com.microtech.aidexx.common.net


import com.google.gson.*
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.BuildConfig
import com.microtech.aidexx.ble.device.entity.CloudDeviceInfo
import com.microtech.aidexx.ble.device.entity.DeviceRegisterInfo
import com.microtech.aidexx.common.net.convert.GsonConverterFactory
import com.microtech.aidexx.common.net.cookie.CookieStore
import com.microtech.aidexx.common.net.entity.*
import com.microtech.aidexx.common.net.interceptors.*
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.entity.BaseEventEntity
import com.microtech.aidexx.db.entity.BloodGlucoseEntity
import com.microtech.aidexx.db.entity.CalibrateEntity
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import com.microtech.aidexx.db.entity.SettingsEntity
import com.microtech.aidexx.db.entity.UserEntity
import com.microtech.aidexx.db.entity.event.DietEntity
import com.microtech.aidexx.db.entity.event.ExerciseEntity
import com.microtech.aidexx.db.entity.event.InsulinEntity
import com.microtech.aidexx.db.entity.event.MedicationEntity
import com.microtech.aidexx.db.entity.event.OthersEntity
import com.microtech.aidexx.db.entity.event.preset.DietUsrPresetEntity
import com.microtech.aidexx.db.entity.event.preset.InsulinUsrPresetEntity
import com.microtech.aidexx.db.entity.event.preset.MedicineUsrPresetEntity
import com.microtech.aidexx.db.entity.event.preset.SportSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.SportUsrPresetEntity
import com.microtech.aidexx.ui.account.entity.UserPreferenceEntity
import com.microtech.aidexx.ui.setting.share.ShareUserInfo
import com.microtech.aidexx.utils.Throttle
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import java.io.File
import java.lang.reflect.Type
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSession

const val middleUrl = "/backend/aidex-x"

// region 账号
const val USER_URL = "$middleUrl/user"
const val sendRegisterPhoneVerificationCode =
    "$USER_URL/sendRegisterPhoneVerificationCode" // 也可以使用sendLoginPhoneVerificationCode
const val sendLoginPhoneVerificationCode =
    "$USER_URL/sendLoginPhoneVerificationCode" // 也可以使用sendLoginPhoneVerificationCode
const val loginOrRegisterByVerificationCodeWithPhone = "$USER_URL/loginOrRegisterByVerificationCodeWithPhone"
const val loginByPassword = "$USER_URL/loginByPassword"
const val getUserInfo = "$USER_URL/getUserInfo"
const val sendResetPasswordPhoneVerificationCode = "$USER_URL/sendResetPasswordPhoneVerificationCode"
const val resetPasswordByVerificationCode = "$USER_URL/passCheckToken/resetPasswordByVerificationCode"
const val setPassword = "$USER_URL/setPassword"
const val logout = "$USER_URL/logout"
const val getuiLogin = "$USER_URL/getuiLogin"
const val updateUserInformation = "$USER_URL/updateUserInformation"
const val userUploadAvatar = "$USER_URL/userUploadAvatar"

//gp
const val sendRegisterEmailVerificationCode = "$USER_URL/sendRegisterEmailVerificationCode"
const val registerByVerificationCodeWithEmail = "$USER_URL/registerByVerificationCodeWithEmail"
const val sendUpdatePasswordEmailVerificationCode = "$USER_URL/sendUpdatePasswordEmailVerificationCode"
// endregion

//region 版本升级
const val getAppVersionList = "$middleUrl/appVersionControl/v2/passCheckToken/getAppVersionList" //APP版本升级检查
//endregion

//region 数据事件相关
const val CGM_URL = "$middleUrl/cgmRecord"
const val getCgmRecordsByPageInfo = "$CGM_URL/getCgmRecordsByPageInfo"

const val BG_URL = "$middleUrl/bloodGlucoseRecord"
const val getBloodGlucoseRecordsByPageInfo = "$BG_URL/getBloodGlucoseRecordsByPageInfo"
const val deleteFingerBloodGlucose = "$BG_URL/deleteFingerBloodGlucose"

const val CAL_URL = "$middleUrl/cgmCalibration"
const val getCalibrationList = "$CAL_URL/getCalibrationList"

//endregion

//region 事件
const val EVENT_URL = "$middleUrl/event"
const val getSysPresetVersion = "$EVENT_URL/getSysPresetVersion"

const val EVENT_EXERCISE_USR_URL = "$EVENT_URL/exerciseUsrPreset"
const val EVENT_EXERCISE_SYS_URL = "$EVENT_URL/exerciseSysPreset"
const val getExerciseUserPresetList = "$EVENT_EXERCISE_USR_URL/getExerciseUserPresetList"
const val saveOrUpdateExerciseUserPreset = "$EVENT_EXERCISE_USR_URL/saveOrUpdateExerciseUserPreset"
const val deleteExerciseUserPresetRecord = "$EVENT_EXERCISE_USR_URL/deleteExerciseUserPresetRecord"
const val getExerciseSysPresetList = "$EVENT_EXERCISE_SYS_URL/getExerciseSysPresetList"

const val FOOD_PRESET_URL = "$EVENT_URL/foodUsrPreset"
const val findFoodUserPresetList = "$FOOD_PRESET_URL/findFoodUserPresetList"
const val saveOrUpdateUserFoodPreset = "$FOOD_PRESET_URL/saveOrUpdateUserFoodPreset"

const val INSULIN_PRESET_URL = "$EVENT_URL/insulinUsrPreset"
const val findInsulinUserPresetList = "$INSULIN_PRESET_URL/findInsulinUserPresetList"
const val saveOrUpdateUserInsulinPreset = "$INSULIN_PRESET_URL/saveOrUpdateUserInsulinPreset"

const val MEDICATION_PRESET_URL = "$EVENT_URL/medicationUsrPreset"
const val findMedicationUsrPresetList = "$MEDICATION_PRESET_URL/findMedicationUsrPresetList"
const val saveOrUpdateMedicationUsrPreset = "$MEDICATION_PRESET_URL/saveOrUpdateMedicationUsrPreset"

const val FOOD_URL = "$EVENT_URL/foodRecord"
const val saveOrUpdateFoodRecord = "$FOOD_URL/saveOrUpdateFoodRecord"
const val findFoodRecordList = "$FOOD_URL/findFoodRecordList"
const val deleteByIdsFood = "$FOOD_URL/deleteByIds"

const val INSULIN_URL = "$EVENT_URL/insulinRecord"
const val saveOrUpdateInsulinRecord = "$INSULIN_URL/saveOrUpdateInsulinRecord"
const val findInsulinRecordList = "$INSULIN_URL/findInsulinRecordList"
const val deleteByIdsInsulin = "$INSULIN_URL/deleteByIds"

const val EXERCISE_URL = "$EVENT_URL/exerciseRecord"
const val saveOrUpdateExerciseRecord = "$EXERCISE_URL/saveOrUpdateExerciseRecord"
const val findExerciseRecordList = "$EXERCISE_URL/findExerciseRecordList"
const val deleteByIdsExercise = "$EXERCISE_URL/deleteByIds"

const val MEDICATION_URL = "$EVENT_URL/medicationRecord"
const val saveOrUpdateMedicationRecord = "$MEDICATION_URL/saveOrUpdateMedicationRecord"
const val findMedicationRecordList = "$MEDICATION_URL/findMedicationRecordList"
const val deleteByIdsMedication = "$MEDICATION_URL/deleteByIds"

const val OTHERS_URL = "$EVENT_URL/otherRecord"
const val saveOrUpdateOtherRecord = "$OTHERS_URL/saveOrUpdateOtherRecord"
const val findOtherRecordList = "$OTHERS_URL/findOtherRecordList"
const val deleteByIdsOthers = "$OTHERS_URL/deleteByIds"


//endregion

//region 分享关注
const val SHARE_FOLLOW_URL = "$middleUrl/userAuthorization"
const val saveOrUpdateUserAuthorization = "$SHARE_FOLLOW_URL/saveOrUpdateUserAuthorization"
const val deleteByIdsShareFollow = "$SHARE_FOLLOW_URL/deleteByIds"
const val findUserAuthorizationList = "$SHARE_FOLLOW_URL/findUserAuthorizationList"
const val findAuthorizationInfoById = "$SHARE_FOLLOW_URL/findAuthorizationInfoById"
const val updateAuthorizationInfo = "$SHARE_FOLLOW_URL/updateAuthorizationInfo"
//endregion

const val API_DOWNLOAD_SETTING = "$middleUrl/userSetting/getUserSetting" //下载设置
const val API_UPLOAD_SETTING = "$middleUrl/userSetting/updateUserSetting" //上传设置
const val API_DEVICE_REGISTER = "$middleUrl/cgmDevice/userDeviceRegister" //注册设备
const val API_DEVICE_UNREGISTER = "$middleUrl/cgmDevice/deviceUnRegister" //注销设备
const val DEVICE = "$middleUrl/cgmDevice/getUserDeviceInfo" //获取设备
const val USER_PREFERENCE = "$middleUrl/user-preference" //
const val UPLOAD_CAL_HISTORY = "$middleUrl/cgmCalibration/saveCalibration" //上传日志
const val UPLOAD_CGM_BRIEF = "$middleUrl/cgmRecord/saveCgmRecord" //上传CGM
const val UPLOAD_CGM_TREND = "$middleUrl/userTrend/saveOrUpdateUserTrend" //上传CGM趋势
const val UPDATE_CGM_RECORD = "$middleUrl/cgmRecord/updateCgmRecord" //更新CGM
const val DOWNLOAD_CGM_RECORD = "$middleUrl/cgm-record/list" //下载CGM
const val UPLOAD_BG_HISTORY = "$middleUrl/bloodGlucoseRecord/saveOrUpdateFingerBloodGlucose" //下载CGM
const val CGM_LIST_RECENT = "$middleUrl/cgm-record/list-recent"
const val vcsMiddleUrl = "backend/vcs"

interface ApiService {


    //region 账户相关
    @GET(logout)
    suspend fun logout(): ApiResult<BaseResponse<String?>>

    @POST(getuiLogin)
    suspend fun getuiLogin(@Body body: ReqGetuiLogin): ApiResult<BaseResponse<String?>>

    @POST(sendRegisterPhoneVerificationCode)
    suspend fun sendRegisterPhoneVerificationCode(@Body body: ReqPhoneVerCode): ApiResult<BaseResponse<Nothing>>

    @POST(sendLoginPhoneVerificationCode)
    suspend fun sendLoginPhoneVerificationCode(@Body body: ReqPhoneVerCode): ApiResult<BaseResponse<Nothing>>

    @POST(loginOrRegisterByVerificationCodeWithPhone)
    suspend fun loginOrRegisterByVerificationCodeWithPhone(@Body body: ReqPhoneCodeLogin): ApiResult<BaseResponse<ResLogin>>

    @POST(loginByPassword)
    suspend fun loginByPassword(@Body body: ReqPwdLogin): ApiResult<BaseResponse<ResLogin>>

    @GET(getUserInfo)
    suspend fun getUserInfo(): ApiResult<BaseResponse<UserEntity>>

    @POST(sendResetPasswordPhoneVerificationCode)
    suspend fun sendResetPasswordPhoneVerificationCode(@Body body: ReqPhoneVerCode): ApiResult<BaseResponse<String>>

    @POST(resetPasswordByVerificationCode)
    suspend fun resetPasswordByVerificationCode(@Body body: ReqChangePWD): ApiResult<BaseResponse<String>>

    @POST(updateUserInformation)
    suspend fun updateUserInformation(@Body map: HashMap<String, Any?>): ApiResult<BaseResponse<String>>
    @Multipart
    @POST(userUploadAvatar)
    suspend fun userUploadAvatar(@Part part: MultipartBody.Part): ApiResult<BaseResponse<String>>

    //gp-start
    @GET(sendRegisterEmailVerificationCode)
    suspend fun sendRegisterEmailVerificationCode(@Query("email") email: String): ApiResult<BaseResponse<String>>

    @POST(registerByVerificationCodeWithEmail)
    suspend fun registerByVerificationCodeWithEmail(@Body body: ReqEmailRegister): ApiResult<BaseResponse<ResLogin>>

    @GET(sendUpdatePasswordEmailVerificationCode)
    suspend fun sendUpdatePasswordEmailVerificationCode(@Query("email") email: String): ApiResult<BaseResponse<String>>
    //gp-end
    //endregion

    //region 版本升级
    @GET(getAppVersionList)
    suspend fun checkAppUpdate(
        @Query("appId") appId: String,
        @Query("project") project: String = "aidex-x",
        @Query("os") os: String = "android",
        @Query("appVersion") appVersion: String = BuildConfig.VERSION_NAME,
        @Query("resourceVersion") resourceVersion: String = "",
    ): ApiResult<BaseResponse<UpgradeInfo>>
    //endregion

    //region cgm bg cal数据
    @GET(getCgmRecordsByPageInfo)
    suspend fun getCgmRecordsByPageInfo(@QueryMap queryMap: Map<String, String>): ApiResult<BaseResponse<List<RealCgmHistoryEntity>>>

    @GET(getBloodGlucoseRecordsByPageInfo)
    suspend fun getBloodGlucoseRecordsByPageInfo(@QueryMap queryMap: Map<String, String>): ApiResult<BaseResponse<List<BloodGlucoseEntity>>>

    @POST(deleteFingerBloodGlucose)
    suspend fun deleteFingerBloodGlucose(@Body req: ReqDeleteEventIds): ApiResult<BaseResponse<String?>>

    @GET(getCalibrationList)
    suspend fun getCalibrationList(@QueryMap queryMap: Map<String, String>): ApiResult<BaseResponse<List<CalibrateEntity>>>

    //endregion

    //region 事件
    @GET(getSysPresetVersion)
    suspend fun getPresetVersion(@Query("eventType") eventType: Int?): ApiResult<BaseResponse<List<ResEventPresetVersion>>>
    @GET(getExerciseSysPresetList)
    suspend fun getExerciseSysPresetList(@QueryMap queryMap: Map<String, String>): ApiResult<BaseResponse<List<SportSysPresetEntity>>>
    @GET(getExerciseUserPresetList)
    suspend fun getExerciseUserPresetList(@QueryMap queryMap: Map<String, String>): ApiResult<BaseResponse<List<SportUsrPresetEntity>>>
    @POST(saveOrUpdateExerciseUserPreset)
    suspend fun saveOrUpdateExerciseUserPreset(@Body data: ReqSaveOrUpdateEventRecords<SportUsrPresetEntity>): ApiResult<BaseResponse<List<SportUsrPresetEntity>>>


    @GET(findFoodUserPresetList)
    suspend fun getFoodUserPresetList(@QueryMap queryMap: Map<String, String>): ApiResult<BaseResponse<List<DietUsrPresetEntity>>>
    @POST(saveOrUpdateUserFoodPreset)
    suspend fun saveOrUpdateUserFoodPreset(@Body data: ReqSaveOrUpdateEventRecords<DietUsrPresetEntity>): ApiResult<BaseResponse<List<DietUsrPresetEntity>>>


    @GET(findInsulinUserPresetList)
    suspend fun getInsulinUserPresetList(@QueryMap queryMap: Map<String, String>): ApiResult<BaseResponse<List<InsulinUsrPresetEntity>>>
    @POST(saveOrUpdateUserInsulinPreset)
    suspend fun saveOrUpdateUserInsulinPreset(@Body data: ReqSaveOrUpdateEventRecords<InsulinUsrPresetEntity>): ApiResult<BaseResponse<List<InsulinUsrPresetEntity>>>


    @GET(findMedicationUsrPresetList)
    suspend fun getMedicineUserPresetList(@QueryMap queryMap: Map<String, String>): ApiResult<BaseResponse<List<MedicineUsrPresetEntity>>>
    @POST(saveOrUpdateMedicationUsrPreset)
    suspend fun saveOrUpdateMedicationUsrPreset(@Body data: ReqSaveOrUpdateEventRecords<MedicineUsrPresetEntity>): ApiResult<BaseResponse<List<MedicineUsrPresetEntity>>>


    @GET(findFoodRecordList)
    suspend fun getFoodRecordsByPageInfo(@QueryMap queryMap: Map<String, String>): ApiResult<BaseResponse<List<DietEntity>>>
    @POST(saveOrUpdateFoodRecord)
    suspend fun saveOrUpdateFoodRecord(@Body data: ReqSaveOrUpdateEventRecords<DietEntity>): ApiResult<BaseResponse<MutableList<DietEntity>>>
    @POST(deleteByIdsFood)
    suspend fun deleteByIdsFood(@Body data: ReqDeleteEventIds): ApiResult<BaseResponse<String?>>

    @GET(findInsulinRecordList)
    suspend fun getInsulinRecordsByPageInfo(@QueryMap queryMap: Map<String, String>): ApiResult<BaseResponse<List<InsulinEntity>>>
    @POST(saveOrUpdateInsulinRecord)
    suspend fun saveOrUpdateInsulinRecord(@Body data: ReqSaveOrUpdateEventRecords<InsulinEntity>): ApiResult<BaseResponse<MutableList<InsulinEntity>>>
    @POST(deleteByIdsInsulin)
    suspend fun deleteByIdsInsulin(@Body data: ReqDeleteEventIds): ApiResult<BaseResponse<String?>>


    @GET(findExerciseRecordList)
    suspend fun getExerciseRecordsByPageInfo(@QueryMap queryMap: Map<String, String>): ApiResult<BaseResponse<List<ExerciseEntity>>>
    @POST(saveOrUpdateExerciseRecord)
    suspend fun saveOrUpdateExerciseRecord(@Body data: ReqSaveOrUpdateEventRecords<ExerciseEntity>): ApiResult<BaseResponse<MutableList<ExerciseEntity>>>
    @POST(deleteByIdsExercise)
    suspend fun deleteByIdsExercise(@Body data: ReqDeleteEventIds): ApiResult<BaseResponse<String?>>


    @GET(findMedicationRecordList)
    suspend fun getMedicationRecordsByPageInfo(@QueryMap queryMap: Map<String, String>): ApiResult<BaseResponse<List<MedicationEntity>>>
    @POST(saveOrUpdateMedicationRecord)
    suspend fun saveOrUpdateMedicationRecord(@Body data: ReqSaveOrUpdateEventRecords<MedicationEntity>): ApiResult<BaseResponse<MutableList<MedicationEntity>>>
    @POST(deleteByIdsMedication)
    suspend fun deleteByIdsMedication(@Body data: ReqDeleteEventIds): ApiResult<BaseResponse<String?>>

    @GET(findOtherRecordList)
    suspend fun getOthersRecordsByPageInfo(@QueryMap queryMap: Map<String, String>): ApiResult<BaseResponse<List<OthersEntity>>>
    @POST(saveOrUpdateOtherRecord)
    suspend fun saveOrUpdateOtherRecord(@Body data: ReqSaveOrUpdateEventRecords<OthersEntity>): ApiResult<BaseResponse<MutableList<OthersEntity>>>
    @POST(deleteByIdsOthers)
    suspend fun deleteByIdsOthers(@Body data: ReqDeleteEventIds): ApiResult<BaseResponse<String?>>

    //endregion

    //region 分享关注
    @GET(findUserAuthorizationList)
    suspend fun findUserAuthorizationList(@QueryMap queryMap: Map<String, String>): ApiResult<BaseResponse<List<ShareUserInfo>>>
    @GET(findAuthorizationInfoById)
    suspend fun findAuthorizationInfoById(@QueryMap queryMap: Map<String, String>): ApiResult<BaseResponse<ShareUserInfo>>
    @POST(saveOrUpdateUserAuthorization)
    suspend fun saveOrUpdateUserAuthorization(@Body body: ReqShareUserInfo): ApiResult<BaseResponse<ShareUserInfo>>
    @POST(deleteByIdsShareFollow)
    suspend fun deleteByIdsShareFollow(@Body body: ReqDeleteEventIds): ApiResult<BaseResponse<String>>
    @POST(updateAuthorizationInfo)
    suspend fun updateAuthorizationInfo(@Body body: ReqModifyShareUserInfo): ApiResult<BaseResponse<String>>

    //endregion

    @GET("$CGM_LIST_RECENT?{params}")
    suspend fun getRecentHistories(@Path("params") params: String)
            : Call<BaseResponse<BasePageList<RealCgmHistoryEntity>>>

    @POST(UPLOAD_CGM_TREND)
    suspend fun postGlucoseTrend(@Body map: HashMap<String, Any?>): ApiResult<BaseResponse<TrendInfo>>

    @POST(UPLOAD_CGM_BRIEF)
    suspend fun postBriefHistory(@Body body: RequestBody): ApiResult<BaseResponse<List<RealCgmHistoryEntity>>>

    @POST(UPLOAD_CAL_HISTORY)
    suspend fun postCalHistory(@Body map: HashMap<String, MutableList<CalibrateEntity>>): ApiResult<BaseResponse<List<CalibrateEntity>>>

    @POST(UPLOAD_BG_HISTORY)
    suspend fun postBgHistory(@Body map: HashMap<String, MutableList<BloodGlucoseEntity>>): ApiResult<BaseResponse<List<BloodGlucoseEntity>>>

    @POST(UPDATE_CGM_RECORD)
    suspend fun updateHistory(@Body map: HashMap<String, MutableList<RealCgmHistoryEntity>>): ApiResult<BaseResponse<List<RealCgmHistoryEntity>>>

    @GET(USER_PREFERENCE)
    suspend fun getUserPreference(): ApiResult<BaseResponse<MutableList<UserPreferenceEntity>>>

    @GET(DEVICE)
    suspend fun getDevice(): ApiResult<BaseResponse<CloudDeviceInfo>>

    @POST(API_DEVICE_REGISTER)
    suspend fun deviceRegister(@Body map: HashMap<String, Any?>): ApiResult<BaseResponse<DeviceRegisterInfo>>

    @POST(API_DEVICE_UNREGISTER)
    suspend fun deviceUnregister(@Body map: HashMap<String, String>): ApiResult<BaseResponse<Nothing>>

    @POST(API_UPLOAD_SETTING)
    suspend fun uploadSetting(@Body body: RequestBody): ApiResult<BaseResponse<Nothing>>

    @GET(API_DOWNLOAD_SETTING)
    suspend fun downloadSetting(@Query("userId") userId: String): ApiResult<BaseResponse<SettingsEntity>>

    @Streaming
    @GET
    suspend fun downloadFile(@Url url: String): ApiResult<ResponseBody>

    companion object {
        private val okClient by lazy { getOkHttpClient() }
        private val gson by lazy { Gson() }

        val instance: ApiService by lazy {
            buildRetrofit(
                BuildConfig.baseUrl,
                GsonConverterFactory.create(createGson(), ::checkBizCodeIsSuccess, ::afterGsonConvert),
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
                    if (code in 800..806 || code == 501) {

                        UserInfoManager.instance().onTokenExpired()

                        Throttle.instance().emit(5000, code) {
                            EventBusManager.send(EventBusKey.TOKEN_EXPIRED, true)
                        }
                    }
                    ret = BizException(code, message = msg.ifBlank { null } ?: "$code")
                }
            }
            return ret
        }

        private fun afterGsonConvert(result: Any?) {
            result?.let {
                if (it !is BaseResponse<*>) {
                    return
                }
                it.data?.let { data ->
                    when (data) {
                        is BaseEventEntity -> data.calTimestamp()
                        is List<*> -> {
                            data.forEach { item ->
                                if (item is BaseEventEntity) {
                                    item.calTimestamp()
                                } else {
                                    return@let
                                }
                            }
                        }
                    }
                }
            }
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
                .hostnameVerifier { _: String?, _: SSLSession? -> true } //忽略host验证
//                .followRedirects(false)  //禁制OkHttp的重定向操作，我们自己处理重定向
//                .addInterceptor(new RedirectInterceptor())
                .addInterceptor(HeaderInterceptor())
//                .addInterceptor(TokenInterceptor())
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
