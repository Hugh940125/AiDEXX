package com.microtech.aidexx.ui.main.event

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.microtech.aidexx.R
import com.microtech.aidexx.base.AfterLeaveCallback
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.setDebounceClickListener
import com.microtech.aidexx.common.toast
import com.microtech.aidexx.databinding.FragmentEventOthersBinding
import com.microtech.aidexx.db.entity.event.BaseEventDetail
import com.microtech.aidexx.ui.main.event.viewmodels.BaseEventViewModel
import com.microtech.aidexx.ui.main.event.viewmodels.OthersViewModel
import java.util.*

class EventOthersFragment : BaseEventFragment<BaseViewModel, FragmentEventOthersBinding>() {

    private val vm: OthersViewModel by viewModels()

    override fun getViewModel(): BaseEventViewModel<*, *, *> = vm

    override fun getNoRecordView(): View = binding.tvOthersNoRecord

    @SuppressLint("NotifyDataSetChanged")
    override fun canLeave(): AfterLeaveCallback? {
        return if (vm.content.isEmpty()) null else ({
            binding.etOthersContent.setText("")
        })
    }

    override fun showPresetDialog(
        detail: BaseEventDetail,
        isNewPreset: Boolean,
        supportDelete: Boolean,
        needSaveNewPreset: Boolean,
        onConfirmClick: (insulinDetailEntity: BaseEventDetail) -> Unit,
        onDeleteClick: ((insulinDetailEntity: BaseEventDetail?) -> Unit)?
    ) {
        binding.etOthersContent.setText(detail.name)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentEventOthersBinding.inflate(inflater)

        initEventClick()
        initHistory(binding.rvOthersHistory)
        initEventMsg()

        binding.etOthersContent.addTextChangedListener {
            vm.content = it.toString()
        }

        return binding.root
    }

    override fun onRealResume(isFromSelfOnResume: Boolean) {
        if (isBindingInit()) {
            binding.tvOthersTime.text = vm.updateEventTime()
        }
    }

    private fun initEventClick() {

        binding.apply {
            btSaveOthers.setDebounceClickListener {
                val content = etOthersContent.text.toString()
                if (content.isEmpty()) {
                    getString(R.string.other_input_tip).toast()
                    return@setDebounceClickListener
                }

                onSaveClick(ignoreDetailList = true) {
                    if (it) etOthersContent.setText("")
                }
            }

            tvOthersTime.setDebounceClickListener {
                onEventTimeClick {  timeStr ->
                    tvOthersTime.text = timeStr
                }
            }
        }
    }

}