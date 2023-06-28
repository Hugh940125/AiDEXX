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
import com.microtech.aidexx.databinding.FragmentEventInsulinBinding
import com.microtech.aidexx.db.entity.event.BaseEventDetail
import com.microtech.aidexx.db.entity.event.InsulinDetail
import com.microtech.aidexx.db.entity.event.preset.InsulinUsrPresetEntity
import com.microtech.aidexx.ui.main.event.dialog.InsulinPresetDialog
import com.microtech.aidexx.ui.main.event.viewmodels.BaseEventViewModel
import com.microtech.aidexx.ui.main.event.viewmodels.InsulinViewModel
import com.microtech.aidexx.utils.LogUtil
import kotlinx.coroutines.launch

class EventInsulinFragment : BaseEventFragment<BaseViewModel, FragmentEventInsulinBinding>() {

    companion object {
        private val TAG = EventInsulinFragment::class.java.simpleName
    }

    private val vm: InsulinViewModel by viewModels()

    override fun getViewModel(): BaseEventViewModel<*, *, *> = vm

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = FragmentEventInsulinBinding.inflate(inflater)

        initInputEvent(binding.slInjectionPreset, binding.rvInjectionPreset, binding.etInsulinName)
        initToSaveList(binding.rvInjectInput)

        initEventClick()
        initHistory(binding.rvInjectionHistory)
        initEventMsg()
        
        return binding.root
    }

    override fun onRealResume(isFromSelfOnResume: Boolean) {
        if (isBindingInit()) {
            binding.apply {
                tvInjectionTime.text = vm.updateEventTime()
                tvInjectionType.text = vm.refreshEventPeriod()
            }
            lifecycleScope.launch {
                EventRepository.syncEventPreset<InsulinUsrPresetEntity>().collect {
                    LogUtil.d("down insulin preset isDone=${it.first} page=${it.second}",
                        EventFragment.TAG
                    )
                }
            }
        }
    }

    override fun getNoRecordView(): View = binding.tvInjectionNoRecord

    override fun showPresetDialog(
        detail: BaseEventDetail,
        isNewPreset: Boolean,
        supportDelete: Boolean,
        needSaveNewPreset: Boolean,
        onConfirmClick: (insulinDetailEntity: BaseEventDetail) -> Unit,
        onDeleteClick: ((insulinDetailEntity: BaseEventDetail?) -> Unit)?
    ) {
        InsulinPresetDialog(
            this@EventInsulinFragment,
            detail as InsulinDetail,
            supportDelete,
            needSaveNewPreset,
            onConfirmClick = onConfirmClick,
            onDeleteClick = onDeleteClick).show()
    }


    private fun initEventClick() {

        binding.apply {

            btSaveInjection.setDebounceClickListener {
                onSaveClick {
                    if (it) etInsulinName.setText("")
                }
            }

            tvInjectionType.setDebounceClickListener {
                onEventTimeTypeClick(vm.getEventSlotType()) { str ->
                    tvInjectionType.text = str
                }
            }

            tvInjectionTime.setDebounceClickListener {
                onEventTimeClick { timeStr ->
                    tvInjectionTime.text = timeStr
                }
            }

        }


    }

}