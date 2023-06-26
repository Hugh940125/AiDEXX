package com.microtech.aidexx.ui.main.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.net.repository.EventRepository
import com.microtech.aidexx.common.setDebounceClickListener
import com.microtech.aidexx.databinding.FragmentEventDietBinding
import com.microtech.aidexx.db.entity.event.BaseEventDetail
import com.microtech.aidexx.db.entity.event.DietDetail
import com.microtech.aidexx.db.entity.event.preset.DietUsrPresetEntity
import com.microtech.aidexx.ui.main.event.dialog.DietNewPresetDialog
import com.microtech.aidexx.ui.main.event.dialog.DietPresetDialog
import com.microtech.aidexx.ui.main.event.viewmodels.BaseEventViewModel
import com.microtech.aidexx.ui.main.event.viewmodels.DietViewModel
import com.microtech.aidexx.utils.LogUtil
import kotlinx.coroutines.launch


class EventDietFragment : BaseEventFragment<BaseViewModel, FragmentEventDietBinding>() {

    private val vm: DietViewModel by viewModels()
    private var isFirstIn = true

    override fun getViewModel(): BaseEventViewModel<*, *, *> = vm

    override fun getNoRecordView(): View = binding.tvDietNoRecord

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentEventDietBinding.inflate(inflater)

        initInputEvent(binding.slFoodPreset, binding.rvFoodPreset, binding.etFoodName)
        initToSaveList(binding.rvDietFoods)

        initEventClick()
        initHistory(binding.rvDietHistory)
        initEventMsg()

        return binding.root
    }

    override fun onRealResume(isFromSelfOnResume: Boolean) {
        if (isBindingInit()) {
            binding.apply {
                tvDietTime.text = vm.updateEventTime()
                tvDietType.text = vm.refreshEventPeriod()
            }
            if (!isFirstIn) {
                lifecycleScope.launch {
                    EventRepository.syncEventPreset<DietUsrPresetEntity>().collect {
                        LogUtil.d("down diet preset isDone=${it.first} page=${it.second}",
                            EventFragment.TAG
                        )
                    }
                }
            } else {
                isFirstIn = false
            }


        }
    }


    private fun initEventClick() {

        binding.apply {

            btSaveDiet.setDebounceClickListener {
                onSaveClick {
                    if (it) etFoodName.setText("")
                }
            }

            tvDietType.setDebounceClickListener {
                onEventTimeTypeClick(vm.getEventSlotType()) { str ->
                    tvDietType.text = str
                }
            }

            tvDietTime.setDebounceClickListener {
                onEventTimeClick { timeStr ->
                    tvDietTime.text = timeStr
                }
            }
        }
    }

    override fun showPresetDialog(
        detail: BaseEventDetail,
        isNewPreset: Boolean,
        supportDelete: Boolean,
        needSaveNewPreset: Boolean,
        onConfirmClick: (insulinDetailEntity: BaseEventDetail) -> Unit,
        onDeleteClick: ((insulinDetailEntity: BaseEventDetail?) -> Unit)?
    ) {
        if (isNewPreset) {
            DietNewPresetDialog(
                this,
                detail as DietDetail,
                onConfirmClick = { detail ->
                    vm.setScale(detail)
                    onConfirmClick(detail)
                },
            ).show()
        } else {
            DietPresetDialog(
                this,
                detail as DietDetail,
                supportDelete = supportDelete,
                onConfirmClick = { detail ->
                    vm.setScale(detail)
                    onConfirmClick(detail)
                },
                onDeleteClick = onDeleteClick
            ).show()
        }

    }
}


