package com.microtech.aidexx.common.net.repository

import com.microtech.aidexx.BuildConfig
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.common.net.entity.BaseResponse
import com.microtech.aidexx.common.net.entity.CGM_RECENT_COUNT
import com.microtech.aidexx.common.net.entity.PAGE_SIZE
import com.microtech.aidexx.common.net.entity.ReqDeleteEventIds
import com.microtech.aidexx.common.net.entity.ReqGetEventByPage
import com.microtech.aidexx.common.net.entity.ReqSaveOrUpdateEventRecords
import com.microtech.aidexx.common.net.entity.ReqSysPresetExercisePageInfo
import com.microtech.aidexx.common.net.entity.ReqSysPresetFoodPageInfo
import com.microtech.aidexx.common.net.entity.ReqSysPresetInsulinPageInfo
import com.microtech.aidexx.common.net.entity.ReqSysPresetMedicationPageInfo
import com.microtech.aidexx.common.net.entity.toQueryMap
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.data.DataSyncController
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
        startAutoIncrementColumn: Long? = null,
        endAutoIncrementColumn: Long?,
        eventClazz: Class<out BaseEventEntity>
    ): ApiResult<BaseResponse<out List<BaseEventEntity>>> {

        return when (eventClazz) {
            RealCgmHistoryEntity::class.java -> getCgmRecordsByPageInfo(userId = userId, pageSize = pageSize, startAutoIncrementColumn= startAutoIncrementColumn, endAutoIncrementColumn = endAutoIncrementColumn)
            CalibrateEntity::class.java -> getCalRecordsByPageInfo(userId = userId, pageSize = pageSize, startAutoIncrementColumn= startAutoIncrementColumn, endAutoIncrementColumn = endAutoIncrementColumn)
            BloodGlucoseEntity::class.java -> getBgRecordsByPageInfo(userId = userId, pageSize = pageSize, startAutoIncrementColumn= startAutoIncrementColumn, endAutoIncrementColumn = endAutoIncrementColumn)
            DietEntity::class.java -> getDietRecordsByPageInfo(userId = userId, pageSize = pageSize, startAutoIncrementColumn= startAutoIncrementColumn, endAutoIncrementColumn = endAutoIncrementColumn)
            ExerciseEntity::class.java -> getExerciseRecordsByPageInfo(userId = userId, pageSize = pageSize, startAutoIncrementColumn= startAutoIncrementColumn, endAutoIncrementColumn = endAutoIncrementColumn)
            MedicationEntity::class.java -> getMedicineRecordsByPageInfo(userId = userId, pageSize = pageSize, startAutoIncrementColumn= startAutoIncrementColumn, endAutoIncrementColumn = endAutoIncrementColumn)
            InsulinEntity::class.java -> getInsulinRecordsByPageInfo(userId = userId, pageSize = pageSize, startAutoIncrementColumn= startAutoIncrementColumn, endAutoIncrementColumn = endAutoIncrementColumn)
            OthersEntity::class.java -> getOthersRecordsByPageInfo(userId = userId, pageSize = pageSize, startAutoIncrementColumn= startAutoIncrementColumn, endAutoIncrementColumn = endAutoIncrementColumn)
            else -> {
                if (BuildConfig.DEBUG) TODO("添加对应类型的分页请求接口")
                else {
                    LogUtil.xLogE("不支持当前类型下载数据 clazz=${eventClazz.simpleName}")
                    ApiResult.Failure(ApiResult.ERR_CODE_SYSTEM, "not support")
                }
            }
        }
    }

    suspend inline fun <reified EVENT: BaseEventEntity> getRecentData(
        userId: String,
        count: Int = CGM_RECENT_COUNT,
        pageSize: Int = PAGE_SIZE
    ): Boolean {

        val clazz = EVENT::class.java

//        val dataSyncFlagKey = DataSyncController.getDataSyncFlagKey(userId, clazz)
        val loginStateKey = DataSyncController.getLoginStateKey(userId, clazz) // 标记登录时这个事件数据是否下载成功
        val loginMaxIdKey = DataSyncController.getLoginMaxIdKey(userId, clazz) // 标记登录时这个事件本地最大id
        val taskItemListKey = DataSyncController.getTaskItemListKey(userId, clazz) // 标记登录之后的同步任务

        var startAutoIncrementColumn: Long?
        var endAutoIncrementColumn: Long? = null

        val result = withContext(Dispatchers.IO)  {


            if (MmkvManager.isLastLoginEventDownloadSuccess(loginStateKey)) {
                MmkvManager.setLastLoginEventDownloadState(loginStateKey, false)
                startAutoIncrementColumn = EventDbRepository.findMaxEventId<EVENT>() ?: 0L
                startAutoIncrementColumn = if (startAutoIncrementColumn!! <= 0L) null else startAutoIncrementColumn

                // 保存登录同步前本地最大id
                MmkvManager.setEventDataId(loginMaxIdKey, startAutoIncrementColumn?:-1L)
            } else {
                // 上次登录失败 说明库里有脏数据 就从sp取上次登录时本地最大id
                startAutoIncrementColumn = MmkvManager.getEventDataId(loginMaxIdKey)
            }

            val breakAll: ()->Unit = {
                //  中途停止说明 start--end 区间数据不够 已经下载完了
                // end标记为0代表 登录成功后不 同步任务不需要拉这段数据
                endAutoIncrementColumn = 0L
            }

            (0 until count).chunked(pageSize).all { list ->

                if (endAutoIncrementColumn != null && endAutoIncrementColumn!! <= 0L) {
                    LogUtil.d("最小id为0 代表数据下载完了")
                    breakAll()
                    return@withContext true
                }

                when (val apiResult = getEventRecordsByPageInfo(
                    userId,
                    list.size,
                    startAutoIncrementColumn = startAutoIncrementColumn,
                    endAutoIncrementColumn = endAutoIncrementColumn,
                    clazz
                )
                ) {
                    is ApiResult.Success -> {

                        apiResult.result.data?.let {
                            if (it.isEmpty()) {
                                if (list[0] == 0) {
//                                    MmkvManager.setEventDataId(dataSyncFlagKey, DATA_EMPTY_MIN_ID)
                                }
                                breakAll()
                                //这页数据为空说明拉完了
                                return@withContext true
                            } else {
                                insertToDb(it, clazz)

                                val lastItemId = it.last().autoIncrementColumn
                                lastItemId?.let { itemId ->

                                    endAutoIncrementColumn = itemId - 1

                                    if (it.size < list.size) {
                                        //这页数据数量不够分页大小 说明拉完了
                                        breakAll()
                                        return@withContext true
                                    }

                                } ?:let {
                                    LogUtil.xLogE("getrecent fail ${clazz.simpleName} 登录拉数据出现空id情况")
                                    return@withContext true
                                }
                            }
                        } ?:let {
                            // 和服务端确认 成功不会给null 空的只会是空集合
                            // 如果是null 就是不确定是否有数据 不记录最小id 让下载任务去下载
                            LogUtil.xLogE("getrecent fail ${clazz.simpleName} apiResult.result.data = null")
                            return@withContext true
                        }

                        true
                    }

                    is ApiResult.Failure -> {
                        LogUtil.xLogE("getrecent fail ${clazz.simpleName} ${apiResult.code} ${apiResult.msg}")
                        false
                    }
                }
            }
        }

        if (result) {
            MmkvManager.setLastLoginEventDownloadState(loginStateKey, true)
            // 更新同步任务项
            if ( endAutoIncrementColumn != null && endAutoIncrementColumn != 0L) {
                var taskItemList = MmkvManager.getEventSyncTask(taskItemListKey)
                taskItemList = taskItemList ?: DataSyncController.SyncTaskItemList(list = mutableListOf())
                taskItemList.list.add(0, DataSyncController.SyncTaskItem(startAutoIncrementColumn, endAutoIncrementColumn))
                MmkvManager.setEventSyncTask(taskItemListKey, taskItemList)
                LogUtil.d("SyncTaskItemList $taskItemListKey=$taskItemList", "getRecentData")
            }
        }

        return result

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
        startAutoIncrementColumn: Long? = null,
        endAutoIncrementColumn: Long?
    ) = withContext(dispatcher) {

        val req = ReqGetEventByPage(
            startAutoIncrementColumn,
            endAutoIncrementColumn,
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
        startAutoIncrementColumn: Long? = null,
        endAutoIncrementColumn: Long?
    ) = withContext(dispatcher) {

        val req = ReqGetEventByPage(
            startAutoIncrementColumn,
            endAutoIncrementColumn,
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
        endAutoIncrementColumn: Long? = null
    ) = withContext(dispatcher) {

        val req = ReqGetEventByPage(
            startAutoIncrementColumn,
            endAutoIncrementColumn,
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
        endAutoIncrementColumn: Long? = null
    ) = withContext(dispatcher) {

        val req = ReqGetEventByPage(
            startAutoIncrementColumn,
            endAutoIncrementColumn,
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
        endAutoIncrementColumn: Long? = null
    ) = withContext(dispatcher) {

        val req = ReqGetEventByPage(
            startAutoIncrementColumn,
            endAutoIncrementColumn,
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
        endAutoIncrementColumn: Long? = null
    ) = withContext(dispatcher) {

        val req = ReqGetEventByPage(
            startAutoIncrementColumn,
            endAutoIncrementColumn,
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
        endAutoIncrementColumn: Long? = null
    ) = withContext(dispatcher) {

        val req = ReqGetEventByPage(
            startAutoIncrementColumn,
            endAutoIncrementColumn,
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

    suspend fun <T: BaseEventEntity> deleteEventByIds(ids: List<String>, clazz: Class<T>) = withContext(dispatcher) {
        val api = ApiService.instance
        val reqIds = ReqDeleteEventIds(ids)
        val apiResult = when (clazz) {
            BloodGlucoseEntity::class.java -> api.deleteFingerBloodGlucose(reqIds)
            DietEntity::class.java -> api.deleteByIdsFood(reqIds)
            ExerciseEntity::class.java -> api.deleteByIdsExercise(reqIds)
            InsulinEntity::class.java -> api.deleteByIdsInsulin(reqIds)
            MedicationEntity::class.java -> api.deleteByIdsMedication(reqIds)
            OthersEntity::class.java -> api.deleteByIdsOthers(reqIds)
            else -> null
        }

        apiResult?.let {
            it is ApiResult.Success
        } ?: true
    }



}