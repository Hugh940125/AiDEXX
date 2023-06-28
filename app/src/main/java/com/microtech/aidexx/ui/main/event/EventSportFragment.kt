package com.microtech.aidexx.ui.main.event

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.net.repository.EventRepository
import com.microtech.aidexx.common.setDebounceClickListener
import com.microtech.aidexx.databinding.FragmentEventSportBinding
import com.microtech.aidexx.db.entity.event.BaseEventDetail
import com.microtech.aidexx.db.entity.event.ExerciseDetail
import com.microtech.aidexx.db.entity.event.preset.SportUsrPresetEntity
import com.microtech.aidexx.ui.main.event.dialog.SportPresetDialog
import com.microtech.aidexx.ui.main.event.viewmodels.BaseEventViewModel
import com.microtech.aidexx.ui.main.event.viewmodels.SportViewModel
import com.microtech.aidexx.utils.LogUtil
import kotlinx.coroutines.launch
import java.util.*

class EventSportFragment : BaseEventFragment<BaseViewModel, FragmentEventSportBinding>() {


    private val vm: SportViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentEventSportBinding.inflate(inflater)
        initInputEvent(binding.slSportPreset, binding.rvSportPreset, binding.etSportName)
        initToSaveList(binding.rvSports)
        initEventClick()
        initHistory(binding.rvSportHistory)
        initEventMsg()
        return binding.root
    }

    override fun onRealResume(isFromSelfOnResume: Boolean) {
        if (isBindingInit()) {
            binding.tvSportTime.text = vm.updateEventTime()
        }
        lifecycleScope.launch {
            EventRepository.syncEventPreset<SportUsrPresetEntity>().collect {
                LogUtil.d("down sport preset isDone=${it.first} page=${it.second}",
                    EventFragment.TAG
                )
            }
        }
    }

    override fun getViewModel(): BaseEventViewModel<*, *, *> = vm

    override fun getNoRecordView(): View = binding.tvSportNoRecord

    override fun showPresetDialog(
        detail: BaseEventDetail,
        isNewPreset: Boolean,
        supportDelete: Boolean,
        needSaveNewPreset: Boolean,
        onConfirmClick: (insulinDetailEntity: BaseEventDetail) -> Unit,
        onDeleteClick: ((insulinDetailEntity: BaseEventDetail?) -> Unit)?
    ) {
        SportPresetDialog(
            this,
            detail as ExerciseDetail,
            supportDelete,
            needSaveNewPreset,
            onConfirmClick,
            onDeleteClick
        ).show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initEventClick() {

        binding.apply {

            btSaveSport.setDebounceClickListener {
                onSaveClick {
                    if (it) etSportName.setText("")
                }
            }

            tvSportTime.setDebounceClickListener {
                onEventTimeClick { timeStr ->
                    tvSportTime.text = timeStr
                }
            }
        }
    }

}