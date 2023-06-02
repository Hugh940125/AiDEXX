package com.microtech.aidexx.common.net.repository

import com.microtech.aidexx.BuildConfig
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.common.net.entity.BaseResponse
import com.microtech.aidexx.common.net.entity.CGM_RECENT_COUNT
import com.microtech.aidexx.common.net.entity.PAGE_SIZE
import com.microtech.aidexx.common.net.entity.ReqGetEventByPage
import com.microtech.aidexx.common.net.entity.ReqSaveOrUpdateEventRecords
import com.microtech.aidexx.common.net.entity.ReqSysPresetExercisePageInfo
import com.microtech.aidexx.common.net.entity.ReqSysPresetFoodPageInfo
import com.microtech.aidexx.common.net.entity.ReqSysPresetInsulinPageInfo
import com.microtech.aidexx.common.net.entity.ReqSysPresetMedicationPageInfo
import com.microtech.aidexx.common.net.entity.toQueryMap
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.data.DataSyncController
import com.microtech.aidexx.data.DataSyncController.Companion.DATA_EMPTY_MIN_ID
import com.microtech.aidexx.data.DataSyncController.Companion.insertToDb
import com.microtech.aidexx.db.entity.BaseEventEntity
import com.microtech.aidexx.db.entity.BloodGlucoseEntity
import com.microtech.aidexx.db.entity.CalibrateEntity
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import com.microtech.aidexx.db.entity.event.DietEntity
import com.microtech.aidexx.db.entity.event.ExerciseEntity
import com.microtech.aidexx.db.entity.event.InsulinEntity
import com.microtech.aidexx.db.entity.event.MedicationEntity
import com.microtech.aidexx.db.entity.event.OthersEntity
import com.microtech.aidexx.db.entity.event.preset.BasePresetEntity
import com.microtech.aidexx.db.entity.event.preset.BaseSysPreset
import com.microtech.aidexx.db.entity.event.preset.DietSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.DietUsrPresetEntity
import com.microtech.aidexx.db.entity.event.preset.InsulinSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.InsulinUsrPresetEntity
import com.microtech.aidexx.db.entity.event.preset.MedicineSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.MedicineUsrPresetEntity
import com.microtech.aidexx.db.entity.event.preset.SportSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.SportUsrPresetEntity
import com.microtech.aidexx.db.repository.EventDbRepository
import com.microtech.aidexx.ui.main.event.viewmodels.EventType
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.mmkv.MmkvManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlin.reflect.KClass

object EventRepository {

    private val dispatcher = Dispatchers.IO

    suspend fun saveOrUpdateRecords(
        data: List<Any>
    ): ApiResult<BaseResponse<out List<Any>>> = withContext(dispatcher) {

        val req = ReqSaveOrUpdateEventRecords(data)
        val api = ApiService.instance

        when(data.first().javaClass) {
            DietEntity::class.java -> api.saveOrUpdateFoodRecord(req as ReqSaveOrUpdateEventRecords<DietEntity>)
            ExerciseEntity::class.java -> api.saveOrUpdateExerciseRecord(req as ReqSaveOrUpdateEventRecords<ExerciseEntity>)
            MedicationEntity::class.java -> api.saveOrUpdateMedicationRecord(req as ReqSaveOrUpdateEventRecords<MedicationEntity>)
            InsulinEntity::class.java -> api.saveOrUpdateInsulinRecord(req as ReqSaveOrUpdateEventRecords<InsulinEntity>)
            OthersEntity::class.java -> api.saveOrUpdateOtherRecord(req as ReqSaveOrUpdateEventRecords<OthersEntity>)

            DietUsrPresetEntity::class.java -> api.saveOrUpdateUserFoodPreset(req as ReqSaveOrUpdateEventRecords<DietUsrPresetEntity>)
            SportUsrPresetEntity::class.java -> api.saveOrUpdateExerciseUserPreset(req as ReqSaveOrUpdateEventRecords<SportUsrPresetEntity>)
            MedicineUsrPresetEntity::class.java -> api.saveOrUpdateMedicationUsrPreset(req as ReqSaveOrUpdateEventRecords<MedicineUsrPresetEntity>)
            InsulinUsrPresetEntity::class.java -> api.saveOrUpdateUserInsulinPreset(req as ReqSaveOrUpdateEventRecords<InsulinUsrPresetEntity>)

            else -> {
                if (BuildConfig.DEBUG) TODO("添加对应类型的上传请求接口")
                else {
                    LogUtil.xLogE("不支持当前类型下载数据 clazz=${data.first().javaClass.simpleName}")
                    ApiResult.Failure(ApiResult.ERR_CODE_SYSTEM, "not support")
                }
            }
        }
    }

    suspend fun getEventRecordsByPageInfo(
        userId: String,
        pageSize: Int = PAGE_SIZE,
        curMinId: Long?,
        eventClazz: Class<out BaseEventEntity>
    ): ApiResult<BaseResponse<out List<BaseEventEntity>>> {

        return when (eventClazz) {
            RealCgmHistoryEntity::class.java -> getCgmRecordsByPageInfo(userId = userId, pageSize = pageSize, endAutoIncrementColumn = curMinId)
            CalibrateEntity::class.java -> getCalRecordsByPageInfo(userId = userId, pageSize = pageSize, downAutoIncrementColumn = curMinId)
            BloodGlucoseEntity::class.java -> getBgRecordsByPageInfo(userId = userId, pageSize = pageSize, downAutoIncrementColumn = curMinId)
            DietEntity::class.java -> getDietRecordsByPageInfo(userId = userId, pageSize = pageSize, downAutoIncrementColumn = curMinId)
            ExerciseEntity::class.java -> getExerciseRecordsByPageInfo(userId = userId, pageSize = pageSize, downAutoIncrementColumn = curMinId)
            MedicationEntity::class.java -> getMedicineRecordsByPageInfo(userId = userId, pageSize = pageSize, downAutoIncrementColumn = curMinId)
            InsulinEntity::class.java -> getInsulinRecordsByPageInfo(userId = userId, pageSize = pageSize, downAutoIncrementColumn = curMinId)
            OthersEntity::class.java -> getOthersRecordsByPageInfo(userId = userId, pageSize = pageSize, downAutoIncrementColumn = curMinId)
            else -> {
                if (BuildConfig.DEBUG) TODO("添加对应类型的分页请求接口")
                else {
                    LogUtil.xLogE("不支持当前类型下载数据 clazz=${eventClazz.simpleName}")
                    ApiResult.Failure(ApiResult.ERR_CODE_SYSTEM, "not support")
                }
            }
        }
    }

    suspend inline fun <reified T: BaseEventEntity> getRecentData(
        userId: String,
        count: Int = CGM_RECENT_COUNT
    ) = withContext(Dispatchers.IO)  {

        val clazz = T::class.java
        val dataSyncFlagKey = DataSyncController.getDataSyncFlagKey(userId, clazz)

        (0 until count).chunked(PAGE_SIZE).all { list ->

            val curMinId = MmkvManager.getEventDataMinId<Long>(dataSyncFlagKey)?.let { it - 1 }

            if (curMinId != null && curMinId <= 0L) {
                LogUtil.d("最小id为0 代表数据下载完了")
                return@withContext true
            }

            when (val apiResult = getEventRecordsByPageInfo(userId, list.size, curMinId, clazz)) {
                is ApiResult.Success -> {

                    apiResult.result.data?.let {
                        if (it.isEmpty()) {
                            if (list[0] == 0) {
                                MmkvManager.setEventDataMinId(dataSyncFlagKey, DATA_EMPTY_MIN_ID)
                            }
                            return@withContext true
                        } else {
                            insertToDb(it, clazz)
                            MmkvManager.setEventDataMinId(dataSyncFlagKey, it.last().autoIncrementColumn)
                        }
                    } ?:let {
                        // 和服务端确认 成功不会给null 空的只会是空集合
                        // 如果是null 就是不确定是否有数据 不记录最小id 让下载任务去下载
                        return@withContext true
                    }
                    true
                }

                is ApiResult.Failure -> {
                    return@all false
                }
            }
        }
    }

    //region CGM
    /**
     *   @param pageNum: Int = 1,//	是 1 分页参数 页数(Integer)
     *   @param pageSize: Int = [PAGE_SIZE],//	是 100 分页参数 条数(Integer)
     *   @param userId: String = [UserInfoManager.instance().userId()],//	是 String (String)
     *   @param startAutoIncrementColumn: Long?,//	否 0 自增列(Long).序号 有值的话是返回 大于等于 startAutoIncrementColumn 的数据
     *   @param endAutoIncrementColumn: Long?,// 有值的话是返回 小于等于 endAutoIncrementColumn 的数据
     *   @param orderStrategy: String? //	否 ASC 枚举值.排序规则 默认DESC
     */
    private suspend fun getCgmRecordsByPageInfo(
        pageNum: Int = 1,
        pageSize: Int = PAGE_SIZE,
        userId: String = UserInfoManager.instance().userId(),
        startAutoIncrementColumn: Long? = null,
        endAutoIncrementColumn: Long? = null,
        orderStrategy: String? = null
    ) = withContext(dispatcher) {


        val req = ReqGetEventByPage(
            startAutoIncrementColumn,
            endAutoIncrementColumn,
            orderStrategy
        ).also {
            it.pageNum = pageNum
            it.pageSize = pageSize
            it.userId = userId
        }

        ApiService.instance.getCgmRecordsByPageInfo(req.toQueryMap())
    }

    //endregion

    //region BG
    /**
     *date: yyyy-MM-dd HH:mm:ssZ 格式
     */
    private suspend fun getBgRecordsByPageInfo(
        pageNum: Int = 1,
        pageSize: Int = PAGE_SIZE,
        userId: String = UserInfoManager.instance().userId(),
        downAutoIncrementColumn: Long?
    ) = withContext(dispatcher) {

        val req = ReqGetEventByPage(
            null,
            downAutoIncrementColumn,
            null
        ).also {
            it.pageNum = pageNum
            it.pageSize = pageSize
            it.userId = userId
        }

        ApiService.instance.getBloodGlucoseRecordsByPageInfo(req.toQueryMap())
    }

    //endregion

    //region CAL

    private suspend fun getCalRecordsByPageInfo(
        pageNum: Int = 1,
        pageSize: Int = PAGE_SIZE,
        userId: String = UserInfoManager.instance().userId(),
        downAutoIncrementColumn: Long?
    ) = withContext(dispatcher) {

        val req = ReqGetEventByPage(
            null,
            downAutoIncrementColumn,
            null
        ).also {
            it.pageNum = pageNum
            it.pageSize = pageSize
            it.userId = userId
        }

        ApiService.instance.getCalibrationList(req.toQueryMap())
    }

    //endregion


    private suspend fun getDietRecordsByPageInfo(
        pageNum: Int = 1,
        pageSize: Int = PAGE_SIZE,
        userId: String = UserInfoManager.instance().userId(),
        startAutoIncrementColumn: Long? = null,
        downAutoIncrementColumn: Long? = null
    ) = withContext(dispatcher) {

        val req = ReqGetEventByPage(
            startAutoIncrementColumn,
            downAutoIncrementColumn,
            null
        ).also {
            it.pageNum = pageNum
            it.pageSize = pageSize
            it.userId = userId
        }

        ApiService.instance.getFoodRecordsByPageInfo(req.toQueryMap())
    }
    private suspend fun getExerciseRecordsByPageInfo(
        pageNum: Int = 1,
        pageSize: Int = PAGE_SIZE,
        userId: String = UserInfoManager.instance().userId(),
        startAutoIncrementColumn: Long? = null,
        downAutoIncrementColumn: Long? = null
    ) = withContext(dispatcher) {

        val req = ReqGetEventByPage(
            startAutoIncrementColumn,
            downAutoIncrementColumn,
            null
        ).also {
            it.pageNum = pageNum
            it.pageSize = pageSize
            it.userId = userId
        }

        ApiService.instance.getExerciseRecordsByPageInfo(req.toQueryMap())
    }
    private suspend fun getInsulinRecordsByPageInfo(
        pageNum: Int = 1,
        pageSize: Int = PAGE_SIZE,
        userId: String = UserInfoManager.instance().userId(),
        startAutoIncrementColumn: Long? = null,
        downAutoIncrementColumn: Long? = null
    ) = withContext(dispatcher) {

        val req = ReqGetEventByPage(
            startAutoIncrementColumn,
            downAutoIncrementColumn,
            null
        ).also {
            it.pageNum = pageNum
            it.pageSize = pageSize
            it.userId = userId
        }

        ApiService.instance.getInsulinRecordsByPageInfo(req.toQueryMap())
    }
    private suspend fun getMedicineRecordsByPageInfo(
        pageNum: Int = 1,
        pageSize: Int = PAGE_SIZE,
        userId: String = UserInfoManager.instance().userId(),
        startAutoIncrementColumn: Long? = null,
        downAutoIncrementColumn: Long? = null
    ) = withContext(dispatcher) {

        val req = ReqGetEventByPage(
            startAutoIncrementColumn,
            downAutoIncrementColumn,
            null
        ).also {
            it.pageNum = pageNum
            it.pageSize = pageSize
            it.userId = userId
        }

        ApiService.instance.getMedicationRecordsByPageInfo(req.toQueryMap())
    }
    private suspend fun getOthersRecordsByPageInfo(
        pageNum: Int = 1,
        pageSize: Int = PAGE_SIZE,
        userId: String = UserInfoManager.instance().userId(),
        startAutoIncrementColumn: Long? = null,
        downAutoIncrementColumn: Long? = null
    ) = withContext(dispatcher) {

        val req = ReqGetEventByPage(
            startAutoIncrementColumn,
            downAutoIncrementColumn,
            null
        ).also {
            it.pageNum = pageNum
            it.pageSize = pageSize
            it.userId = userId
        }

        ApiService.instance.getOthersRecordsByPageInfo(req.toQueryMap())
    }

    suspend fun fetchPresetVersion(@EventType type: Int?) = withContext(dispatcher) {
        ApiService.instance.getPresetVersion(type)
    }


    /**
     * @param isPullDesc 是否是从大日期往小日期方向同步也即降序拉数据  系统预设忽略该字段默认降序
     */
    inline fun <reified P: BasePresetEntity> syncEventPreset(
        userId: String = UserInfoManager.instance().userId(),
        isPullDesc: Boolean = true
    ) = callbackFlow {

        var startAutoIncrementColumn: Long? = null
        var endAutoIncrementColumn: Long? = null
        if (isPullDesc || P::class is BaseSysPreset) {
            endAutoIncrementColumn = EventDbRepository.findMinPresetId<P>()?.let { it - 1 }
        } else {
            startAutoIncrementColumn = EventDbRepository.findMaxPresetId<P>()?.let { it + 1 }
        }

        pollPresetData(
            userId = userId,
            pClazz = P::class,
            startAutoIncrementColumn = startAutoIncrementColumn,
            endAutoIncrementColumn = endAutoIncrementColumn
        ) { isDone, pageIndex ->
            trySend(isDone to pageIndex)
            if (isDone != false) close()
        }

        awaitClose()

    }.flowOn(Dispatchers.IO)

    suspend fun pollPresetData(
        pageNum: Int = 1,
        pageSize: Int = PAGE_SIZE,
        userId: String = UserInfoManager.instance().userId(),
        startAutoIncrementColumn: Long? = null,
        endAutoIncrementColumn: Long? = null,
        orderStrategy: String? = null,
        pClazz: KClass<out BasePresetEntity>,
        cb: ((isDone: Boolean?, pageIndex: Int)-> Unit)?
    ): Boolean = withContext(dispatcher) {

        when (val apiResult = getEventPresetByPageInfo(
            pageNum, pageSize, userId,
            startAutoIncrementColumn, endAutoIncrementColumn, orderStrategy,
            pClazz.java
        )) {
            is ApiResult.Success -> {
                apiResult.result.run {

                    data?.let { rspList ->
                        if(rspList.isNotEmpty()) {
                            EventDbRepository.insertPresetData(rspList)
                            cb?.invoke(pageSize < PAGE_SIZE, pageNum)
                            if (pageSize >= PAGE_SIZE) {

                                val start = startAutoIncrementColumn?.let {
                                    rspList.first().getServerPresetId()
                                }?.let { it + 1 }

                                val end = endAutoIncrementColumn?.let {
                                    rspList.last().getServerPresetId()
                                }?.let { it - 1 }

                                // 递归拉
                                pollPresetData( pageNum, pageSize, userId,
                                    start, end, orderStrategy,
                                    pClazz, cb
                                )
                            } else {
                                cb?.invoke(true, pageNum)
                            }
                        } else {
                            cb?.invoke(true, pageNum)
                        }
                    } ?:let {
                        cb?.invoke(null, pageNum)
                    }
                }
            }
            is ApiResult.Failure -> {
                cb?.invoke(null, pageNum)
            }
        }
        false
    }

    private suspend fun getEventPresetByPageInfo(
        pageNum: Int,
        pageSize: Int,
        userId: String,
        startAutoIncrementColumn: Long?,
        endAutoIncrementColumn: Long?,
        orderStrategy: String?,
        presetClazz: Class<out BasePresetEntity>
    ): ApiResult<BaseResponse<out List<BasePresetEntity>>> {
        val api = ApiService.instance

        val reqPageInfo = when (presetClazz) {
            SportUsrPresetEntity::class.java,
            DietUsrPresetEntity::class.java,
            InsulinUsrPresetEntity::class.java,
            MedicineUsrPresetEntity::class.java -> ReqGetEventByPage(startAutoIncrementColumn, endAutoIncrementColumn, orderStrategy)

            SportSysPresetEntity::class.java -> ReqSysPresetExercisePageInfo(endAutoIncrementColumn)
            DietSysPresetEntity::class.java -> ReqSysPresetFoodPageInfo(endAutoIncrementColumn)
            InsulinSysPresetEntity::class.java -> ReqSysPresetInsulinPageInfo(endAutoIncrementColumn)
            MedicineSysPresetEntity::class.java -> ReqSysPresetMedicationPageInfo(endAutoIncrementColumn)
            else -> {
                if (BuildConfig.DEBUG) TODO("添加对应类型的分页请求接口")
                else {
                    LogUtil.xLogE("不支持当前类型下载数据 clazz=${presetClazz.simpleName}")
                    null
                }
            }
        }
        reqPageInfo?.let {
            it.pageNum = pageNum
            it.pageSize = pageSize
            it.userId = userId
        } ?:let {
            return ApiResult.Failure(ApiResult.ERR_CODE_SYSTEM, "not support")
        }
        val reqMap = reqPageInfo.toQueryMap()

        return when (presetClazz) {

            SportUsrPresetEntity::class.java -> api.getExerciseUserPresetList(reqMap)
            DietUsrPresetEntity::class.java -> api.getFoodUserPresetList(reqMap)
            MedicineUsrPresetEntity::class.java -> api.getMedicineUserPresetList(reqMap)
            InsulinUsrPresetEntity::class.java -> api.getInsulinUserPresetList(reqMap)

            else -> {
                if (BuildConfig.DEBUG) TODO("添加对应类型的分页请求接口")
                else {
                    LogUtil.xLogE("不支持当前类型下载数据 clazz=${presetClazz.simpleName}")
                    ApiResult.Failure(ApiResult.ERR_CODE_SYSTEM, "not support")
                }
            }
        }

    }

    suspend fun getUnit() = withContext(dispatcher) {
        ApiService.instance.getUnit()
    }


}