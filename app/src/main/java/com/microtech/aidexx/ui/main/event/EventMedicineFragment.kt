package com.microtech.aidexx.ui.main.event

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.setDebounceClickListener
import com.microtech.aidexx.databinding.FragmentEventMedicineBinding
import com.microtech.aidexx.db.entity.event.BaseEventDetail
import com.microtech.aidexx.db.entity.event.MedicationDetail
import com.microtech.aidexx.ui.main.event.dialog.MedicinePresetDialog
import com.microtech.aidexx.ui.main.event.viewmodels.BaseEventViewModel
import com.microtech.aidexx.ui.main.event.viewmodels.MedicineViewModel
import java.util.*

class EventMedicineFragment: BaseEventFragment<BaseViewModel, FragmentEventMedicineBinding>() {


    companion object {
        private val TAG = EventMedicineFragment::class.java.simpleName
    }

    private val vm: MedicineViewModel by viewModels()

    override fun getViewModel(): BaseEventViewModel<*, *, *> = vm

    override fun getNoRecordView(): View = binding.tvMedicineNoRecord

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEventMedicineBinding.inflate(inflater)
        initInputEvent(binding.slMedicinePreset, binding.rvMedicinePreset, binding.etMedicineName)
        initToSaveList(binding.rvMedicineFoods)
        initEventClick()
        initHistory(binding.rvMedicineHistory)
        initEventMsg()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.apply {
            tvMedicineTime.text = vm.updateEventTime()
            tvMedicineType.text = vm.refreshEventPeriod()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initEventClick() {

        binding.apply {
            btSaveMedicine.setDebounceClickListener {
                onSaveClick {
                    if (it) etMedicineName.setText("")
                }
            }

            tvMedicineType.setDebounceClickListener {
                onEventTimeTypeClick(TYPE_SLOT_MEDICINE) { str ->
                    tvMedicineType.text = str
                }
            }

            tvMedicineTime.setDebounceClickListener {
                onEventTimeClick { timeStr ->
                    tvMedicineTime.text = timeStr
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
        MedicinePresetDialog(
            this@EventMedicineFragment,
            detail as MedicationDetail,
            supportDelete,
            needSaveNewPreset,
            onConfirmClick,
            onDeleteClick
        ).show()
    }

}