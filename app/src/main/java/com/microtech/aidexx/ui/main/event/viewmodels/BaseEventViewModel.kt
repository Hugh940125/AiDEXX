package com.microtech.aidexx.ui.main.event.viewmodels

import android.annotation.SuppressLint
import androidx.annotation.IntDef
import androidx.lifecycle.viewModelScope
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.date2ymdhm
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.repository.EventRepository
import com.microtech.aidexx.db.entity.BaseEventEntity
import com.microtech.aidexx.db.entity.event.BaseEventDetail
import com.microtech.aidexx.db.entity.event.DietEntity
import com.microtech.aidexx.db.entity.event.ExerciseEntity
import com.microtech.aidexx.db.entity.event.InsulinEntity
import com.microtech.aidexx.db.entity.event.MedicationEntity
import com.microtech.aidexx.db.entity.event.preset.BasePresetEntity
import com.microtech.aidexx.db.entity.event.preset.DietSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.DietUsrPresetEntity
import com.microtech.aidexx.db.entity.event.preset.InsulinSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.InsulinUsrPresetEntity
import com.microtech.aidexx.db.entity.event.preset.MedicineSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.MedicineUsrPresetEntity
import com.microtech.aidexx.db.entity.event.preset.SportSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.SportUsrPresetEntity
import com.microtech.aidexx.db.repository.EventDbRepository
import com.microtech.aidexx.ui.main.MainActivity
import com.microtech.aidexx.ui.main.event.EventParameterManager
import com.microtech.aidexx.ui.main.event.EventSlotType
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.PinyinUtils
import com.microtech.aidexx.utils.eventbus.DataChangedType
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.utils.eventbus.EventDataChangedInfo
import com.microtech.aidexx.utils.mmkv.MmkvManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.lang.reflect.ParameterizedType
import java.util.Date

// 1胰岛素，2饮食，3用药，4运动
const val EVENT_INSULIN = 1
const val EVENT_DIET = 2
const val EVENT_MEDICINE = 3
const val EVENT_EXERCISE = 4
@IntDef(EVENT_INSULIN, EVENT_DIET, EVENT_MEDICINE, EVENT_EXERCISE)
annotation class EventType

abstract class BaseEventViewModel<T: BaseEventEntity, D: BaseEventDetail, P: BasePresetEntity>: BaseViewModel() {

    companion object {
        const val TAG = "BaseEventViewModel"

        @SuppressLint("StaticFieldLeak")
        lateinit var periodMgr: EventParameterManager
    }

    private val _toSaveDetailList = mutableListOf<D>()
    val toSaveDetailList: List<D>
        get() = _toSaveDetailList.toList()

    private val _detailHistory = mutableListOf<D>()
    val detailHistory: List<D>
        get() = _detailHistory.toList()

    private val _presetList = mutableListOf<P>()
    val presetList: List<P>
        get() = _presetList.toList()

    protected var eventMomentTypeIndex = 0

    private val tClazz =
        (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>

    protected var eventTime: Date = Date()

    fun updateEventTime(date: Date = Date()): String {
        eventTime = date
        return eventTime.date2ymdhm() ?: Date().date2ymdhm()!!
    }

    protected abstract suspend fun queryPresetByName(name: String): List<P>
    protected abstract suspend fun genNewPreset(name: String): P
    protected abstract suspend fun getDetailHistory(): List<D>
    protected abstract suspend fun genEventEntityWhenSave(): T

    fun updateEventMomentTypeIndex(index: Int) {
        eventMomentTypeIndex = index
    }

    fun addToSaveDetailList(entity: BaseEventDetail) {
        _toSaveDetailList.add(0, entity as D)
    }

    fun updateSaveDetailList(position: Int, entity: BaseEventDetail) {
        _toSaveDetailList[position] = entity as D
    }

    fun removeSaveDetailList(position: Int) = _toSaveDetailList.removeAt(position)
    fun clearToSaveDetailList() = _toSaveDetailList.clear()

    fun clearPresetList() = _presetList.clear()


    suspend fun loadHistory() = flow {
        val result = getDetailHistory().take(15)
        result.sortedByDescending {
            it.createTime
        }

        _detailHistory.clear()
        _detailHistory.addAll(result)
        emit(_detailHistory.size)
    }.flowOn(Dispatchers.IO)

    suspend fun searchPresetByName(name: String) = flow {

        val result = queryPresetByName(name)

        _presetList.clear()
        if (result.isNotEmpty()) {
            result.sortedBy {
                PinyinUtils.getPinyinFirstLetter(it.name)
            }
            _presetList.addAll(result)
        }
        _presetList.add(genNewPreset(name))

        emit(_presetList.size)
    }.flowOn(Dispatchers.IO)


    suspend fun save() = flow {
        val eventEntity = genEventEntityWhenSave()
        val isSuccess = EventDbRepository.insertEvent(eventEntity) != null

        if (isSuccess) {
            EventBusManager.send(EventBusKey.EVENT_DATA_CHANGED, EventDataChangedInfo(
                DataChangedType.ADD, listOf(eventEntity)
            ))
            EventBusManager.sendDelay(EventBusKey.EVENT_JUMP_TO_TAB, MainActivity.HOME,1000)
        }
        clearToSaveDetailList()
        emit(isSuccess to eventEntity)
    }.flowOn(Dispatchers.IO)

    suspend fun savePreset(entity: P) = flow {
        val idx = EventDbRepository.insertPresetData(entity)
        idx?:let {
            LogUtil.xLogE("预设数据保存失败 $entity")
        }
        emit(idx)
    }.flowOn(Dispatchers.IO)


    suspend fun startSyncPreset(){
        viewModelScope.launch {

            val type: Int = when(tClazz) {
                MedicationEntity::class.java -> EVENT_MEDICINE
                InsulinEntity::class.java -> EVENT_INSULIN
                DietEntity::class.java -> EVENT_DIET
                ExerciseEntity::class.java -> EVENT_EXERCISE
                else -> null
            } ?: return@launch

            fetchPresetVersion(type).collect { vi ->
                vi?.let {
                    launch {
                        it.sysVersion?.let { sysV ->
                            if (sysV > MmkvManager.getPresetVersion(type, true)) {
                                syncPreset(type,true, it.sysVersion)
                            }
                        }
                    }

                    launch {
                        it.userVersion?.let { usrV ->
                            if (usrV > MmkvManager.getPresetVersion(type, false)) {
                                syncPreset(type,false, it.userVersion)
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun syncPreset(@EventType type: Int, isSys: Boolean, version: String) {
        val flow = when(type) {
            EVENT_MEDICINE -> {
                if (isSys) EventRepository.syncEventPreset<MedicineSysPresetEntity>()
                else EventRepository.syncEventPreset<MedicineUsrPresetEntity>()
            }
            EVENT_DIET -> {
                if (isSys) EventRepository.syncEventPreset<DietSysPresetEntity>()
                else EventRepository.syncEventPreset<DietUsrPresetEntity>()
            }
            EVENT_EXERCISE -> {
                if (isSys) EventRepository.syncEventPreset<SportSysPresetEntity>()
                else EventRepository.syncEventPreset<SportUsrPresetEntity>()
            }
            EVENT_INSULIN -> {
                if (isSys) EventRepository.syncEventPreset<InsulinSysPresetEntity>()
                else EventRepository.syncEventPreset<InsulinUsrPresetEntity>()
            }
            else -> null
        }

        flow?.let {
            it.collect { ret ->
                if (ret.first == true) {
                    MmkvManager.setPresetVersion(type, version, isSys)
                }
                LogUtil.d("预设数据同步 type=$type isDone=${ret.first} pageIdx=${ret.second}", TAG)
            }
        }
    }

    private suspend fun fetchPresetVersion(@EventType type: Int) = flow {
        when (val apiRet = EventRepository.fetchPresetVersion(type)) {
            is ApiResult.Success -> {
                apiRet.result.data?.let {
                    emit(if (it.isNotEmpty()) it[0] else null)
                } ?: emit(null)
            }
            is ApiResult.Failure -> {
                emit(null)
            }
        }
    }.flowOn(Dispatchers.IO)


    fun getEventPeriodTypes(@EventSlotType type: Int) = periodMgr.getTypes(type)

    @EventSlotType
    abstract fun getEventSlotType(): Int?

    fun refreshEventPeriod(): String {
        val type = getEventSlotType()
        type ?: return ""
        val idx = getEventSlotIndex(type)
        eventMomentTypeIndex = idx + 1 // 接口定义需要从1开始

        return periodMgr.getEventSlot(type)
    }
    fun getEventSlot(@EventSlotType type: Int): String = periodMgr.getEventSlot(type)

    fun getEventSlotIndex(@EventSlotType type: Int): Int  = periodMgr.getEventSlotIndex(type)

}