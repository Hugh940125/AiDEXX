package com.microtech.aidexx.ui.main.event

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.microtech.aidexx.R
import com.microtech.aidexx.base.AfterLeaveCallback
import com.microtech.aidexx.base.BaseFragment
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.toast
import com.microtech.aidexx.db.entity.event.BaseEventDetail
import com.microtech.aidexx.db.entity.event.DietEntity
import com.microtech.aidexx.db.entity.event.ExerciseEntity
import com.microtech.aidexx.db.entity.event.InsulinEntity
import com.microtech.aidexx.db.entity.event.MedicationEntity
import com.microtech.aidexx.db.entity.event.OthersEntity
import com.microtech.aidexx.db.entity.event.preset.BasePresetEntity
import com.microtech.aidexx.db.entity.event.preset.DietPresetEntity
import com.microtech.aidexx.db.entity.event.preset.InsulinPresetEntity
import com.microtech.aidexx.db.entity.event.preset.MedicinePresetEntity
import com.microtech.aidexx.db.entity.event.preset.SportPresetEntity
import com.microtech.aidexx.db.entity.event.preset.toDietDetailEntity
import com.microtech.aidexx.db.entity.event.preset.toExerciseDetailEntity
import com.microtech.aidexx.db.entity.event.preset.toInsulinDetailEntity
import com.microtech.aidexx.db.entity.event.preset.toMedicineDetailEntity
import com.microtech.aidexx.ui.main.event.adapter.EventInputAdapter
import com.microtech.aidexx.ui.main.event.adapter.EventPresetAdapter
import com.microtech.aidexx.ui.main.event.viewmodels.BaseEventViewModel
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.eventbus.DataChangedType
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.utils.eventbus.EventDataChangedInfo
import com.microtech.aidexx.views.dialog.Dialogs
import com.microtech.aidexx.views.selector.time.TimePicker
import kotlinx.coroutines.launch

abstract class BaseEventFragment<VM:BaseViewModel, VB: ViewBinding>: BaseFragment<VM, VB>() {

    lateinit var toSaveDetailAdapter: EventInputAdapter
    lateinit var detailHistoryAdapter: EventInputAdapter
    lateinit var presetAdapter: EventPresetAdapter

    abstract fun getViewModel():BaseEventViewModel<*,*,*>
    abstract fun getNoRecordView(): View
    abstract fun onRealResume(isFromSelfOnResume: Boolean)

    override fun onResume() {
        super.onResume()
        onRealResume(true)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun canLeave(): AfterLeaveCallback? {
        return if (getViewModel().toSaveDetailList.isEmpty()) null else ({
            getViewModel().clearToSaveDetailList()
            toSaveDetailAdapter.data.clear()
            toSaveDetailAdapter.notifyDataSetChanged()
        })
    }

    fun initEventMsg() {

        EventBusManager.onReceive<EventDataChangedInfo>(EventBusKey.EVENT_DATA_CHANGED,this) {
            if (it.first == DataChangedType.DELETE) {
                lifecycleScope.launch {
                    it.second.firstOrNull()?.let { clazz ->
                        if (
                            (this@BaseEventFragment is EventDietFragment && clazz is DietEntity) ||
                            (this@BaseEventFragment is EventSportFragment && clazz is ExerciseEntity) ||
                            (this@BaseEventFragment is EventMedicineFragment && clazz is MedicationEntity) ||
                            (this@BaseEventFragment is EventInsulinFragment && clazz is InsulinEntity) ||
                            (this@BaseEventFragment is EventOthersFragment && clazz is OthersEntity)) {

                            updateHistoryUi()
                        }

                    }
                }
            }
        }
    }


    private fun presetToDetail (p: BasePresetEntity) =
        when (p) {
            is MedicinePresetEntity -> p.toMedicineDetailEntity()
            is DietPresetEntity -> p.toDietDetailEntity()
            is InsulinPresetEntity -> p.toInsulinDetailEntity()
            is SportPresetEntity -> p.toExerciseDetailEntity()
            else -> null
        }

    open fun initInputEvent(presetRecyclerViewContainer: ViewGroup,presetRecyclerView: RecyclerView, inputEditText: EditText) {
        presetAdapter = EventPresetAdapter(getViewModel().presetList.toMutableList(), onItemClick = { position ->

            val preset = getViewModel().presetList[position]
            val insulinDetailEntity = presetToDetail(preset)

            showPresetDialog(
                insulinDetailEntity!!,
                isNewPreset = preset.idx == 0L,
                needSaveNewPreset = true,
                onConfirmClick = { detailEntity ->
                    detailEntity.id = 0
                    getViewModel().addToSaveDetailList(detailEntity)
                    toSaveDetailAdapter.data.add(0, detailEntity)
                    toSaveDetailAdapter.notifyItemInserted(0)
                }
            )

            presetRecyclerViewContainer.isVisible = false

        })

        presetRecyclerView.adapter = presetAdapter
        presetRecyclerView.layoutManager =
            LinearLayoutManager(
                requireContext(),
                RecyclerView.VERTICAL,
                false
            )

        inputEditText.addTextChangedListener {
            val inputString = it.toString().trim()
            if (inputString.isEmpty()) {
                presetRecyclerViewContainer.isVisible = false
                getViewModel().clearPresetList()
                presetAdapter.data.clear()
                presetAdapter.notifyDataSetChanged()
                return@addTextChangedListener
            }
            lifecycleScope.launch {
                getViewModel().searchPresetByName(inputString.trim()).collect {
                    presetAdapter.data = getViewModel().presetList.toMutableList()
                    presetRecyclerViewContainer.isVisible = true
                    presetAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    abstract fun showPresetDialog(
        detail: BaseEventDetail,
        isNewPreset: Boolean = false,
        supportDelete: Boolean = false, // 是否支持删除
        needSaveNewPreset: Boolean = false, // 是否需要保存新的预设
        onConfirmClick: (insulinDetailEntity: BaseEventDetail) -> Unit,
        onDeleteClick: ((insulinDetailEntity: BaseEventDetail?) -> Unit)? = null
    )

    open fun initToSaveList(toSaveRecyclerView: RecyclerView) {

        toSaveDetailAdapter = EventInputAdapter(getViewModel().toSaveDetailList.toMutableList())

        toSaveRecyclerView.adapter = toSaveDetailAdapter

        toSaveRecyclerView.layoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.CENTER
            flexWrap = FlexWrap.WRAP
        }

        toSaveDetailAdapter.setOnItemClickListener { adapter, view, position ->

            showPresetDialog(
                getViewModel().toSaveDetailList[position],
                supportDelete = true,
                onConfirmClick = {
                    getViewModel().updateSaveDetailList(position, it)
                    toSaveDetailAdapter.data[position] = it
                    toSaveDetailAdapter.notifyItemChanged(position)
                },
                onDeleteClick = {
                    getViewModel().removeSaveDetailList(position)
                    toSaveDetailAdapter.data.removeAt(position)
                    toSaveDetailAdapter.notifyItemRemoved(position)
                }
            )

        }
    }

    fun initHistory(historyRecyclerView: RecyclerView) {
        detailHistoryAdapter = EventInputAdapter(getViewModel().detailHistory.toMutableList(), isHistory = true)

        detailHistoryAdapter.setOnItemClickListener { _, _, position ->

            showPresetDialog(
                getViewModel().detailHistory[position],
                onConfirmClick = { detailEntity ->
                    detailEntity.id = 0
                    getViewModel().addToSaveDetailList(detailEntity)
                    toSaveDetailAdapter.data.add(0, detailEntity)
                    toSaveDetailAdapter.notifyItemInserted(0)
                }
            )
        }

        historyRecyclerView.adapter = detailHistoryAdapter
        historyRecyclerView.layoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.CENTER
            flexWrap = FlexWrap.WRAP
        }
        lifecycleScope.launch {
            updateHistoryUi()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    suspend fun updateHistoryUi() {
        lifecycleScope.launch {
            getViewModel().loadHistory().collect {
                detailHistoryAdapter.data = getViewModel().detailHistory.toMutableList()
                detailHistoryAdapter.notifyDataSetChanged()
                getNoRecordView().isVisible = it <= 0
            }
        }
    }

    fun onSaveClick(ignoreDetailList: Boolean = false, cb: ((Boolean)->Unit)?) {
        if (!ignoreDetailList) {
            if (getViewModel().toSaveDetailList.isEmpty()) {
                getString(R.string.please_input).toast()
                return
            }
        }

        lifecycleScope.launch {
            getViewModel().save().collect {
                if (it.first) {
                    getString(R.string.save_complete).toast()
                    if (!ignoreDetailList) {
                        toSaveDetailAdapter.data.clear()
                        toSaveDetailAdapter.notifyDataSetChanged()
                    }
                    updateHistoryUi()
                    cb?.invoke(true)
                } else {
                    LogUtil.eAiDEX("事件保存失败 ${it.second}")
                    cb?.invoke(false)
                }
            }
        }
    }

    fun onEventTimeClick(cb: (time: String)->Unit) {
        TimePicker(requireContext()).pick {
            cb(getViewModel().updateEventTime(it))
        }
    }

    fun onEventTimeTypeClick(@EventSlotType type: Int, cb: (time: String) -> Unit) {
        val types = getViewModel().getEventPeriodTypes(type)
        val curEventPeriodIndex = getViewModel().getEventSlotIndex(type)
        Dialogs.Picker(requireContext()).singlePick(types, curEventPeriodIndex) {
            cb(types[it])
            getViewModel().updateEventMomentTypeIndex(it + 1)
        }
    }



}