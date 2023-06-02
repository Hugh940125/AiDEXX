package com.microtech.aidexx.ui.main.event.dialog

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.microtech.aidexx.R
import com.microtech.aidexx.common.setDebounceClickListener
import com.microtech.aidexx.common.stripTrailingZeros
import com.microtech.aidexx.common.toast
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.data.EventUnitManager
import com.microtech.aidexx.data.SpecificationModel
import com.microtech.aidexx.databinding.DialogMedicinePresetBinding
import com.microtech.aidexx.db.entity.event.MedicationDetail
import com.microtech.aidexx.db.entity.event.preset.MedicineUsrPresetEntity
import com.microtech.aidexx.ui.main.event.adapter.SpecificationAdapter
import com.microtech.aidexx.ui.main.event.viewmodels.MedicineViewModel
import com.microtech.aidexx.utils.DecimalInputTextWatcher
import com.microtech.aidexx.widget.dialog.Dialogs
import com.microtech.aidexx.widget.dialog.bottom.BaseBottomDialog
import kotlinx.coroutines.launch

class MedicinePresetDialog(
    var mFragment: Fragment,
    private val medicineDetailEntity: MedicationDetail,
    private val supportDelete: Boolean = false, // 是否支持删除
    private val needSaveNewPreset: Boolean = false, // 是否需要保存新的预设
    private val onConfirmClick: (medicineDetailEntity: MedicationDetail) -> Unit,
    private val onDeleteClick: ((medicineDetailEntity: MedicationDetail?) -> Unit)? = null
) : BaseBottomDialog(
    mFragment.requireContext()
) {

    private val vm: MedicineViewModel by mFragment.viewModels()

    private lateinit var binding: DialogMedicinePresetBinding
    private lateinit var textWatcher: DecimalInputTextWatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogMedicinePresetBinding.inflate(LayoutInflater.from(mFragment.requireContext()))
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.apply {

            textWatcher = DecimalInputTextWatcher(etDose, 10, 3)
            etDose.addTextChangedListener(textWatcher)

            initUnitUi()
            intDeleteUI()
            initPresetValue()
            initBtnEvent()
        }

    }

    private fun initBtnEvent() {

        binding.apply {
            btnCancel.setDebounceClickListener {
                dismiss()
            }

            btnOk.setDebounceClickListener {
                val quantity = textWatcher.getDoubleValue()
                if (quantity == null) {
                    context.getString(R.string.hint_enter_dose).toast()
                    return@setDebounceClickListener
                }
                medicineDetailEntity.quantity = quantity

                val presetEntity = MedicineUsrPresetEntity()
                presetEntity.name = medicineDetailEntity.name
                presetEntity.userId = UserInfoManager.instance().userId()

                mFragment.lifecycleScope.launch {
                    if (needSaveNewPreset && medicineDetailEntity.presetId == null) {
                        vm.savePreset(presetEntity).collect {
                            it?.let {
                                dismiss()
                                medicineDetailEntity.presetId = it
                                onConfirmClick.invoke(medicineDetailEntity)
                            }
                        }
                    } else {
                        dismiss()
                        onConfirmClick.invoke(medicineDetailEntity)
                    }
                }
            }
        }
    }

    private fun initPresetValue() {
        binding.apply {
            val info = medicineDetailEntity.tradeName.ifEmpty {
                medicineDetailEntity.manufacturer
            }
            tvCategoryName.text = buildString {
                append(medicineDetailEntity.name)
                append(if (info.isEmpty()) "" else "($info)")
            }
            if (0.0 != medicineDetailEntity.quantity) {
                etDose.setText(
                    medicineDetailEntity.quantity.stripTrailingZeros()
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initUnitUi() {
        val medicationUnits = EventUnitManager.getMedicationUnitList()
        for (i in 0 until medicationUnits.size) {
            medicationUnits[i].check = false
        }

        var defaultModel: SpecificationModel? = null
        medicationUnits.find { it.code == medicineDetailEntity.unit }?.let {
            defaultModel = it
            it.check = true
        } ?:let {
            medicationUnits.find { it.isDefault }?.let {
                defaultModel = it
                it.check = true
            }
        }
        defaultModel?.let {
            medicineDetailEntity.unit = it.code
            medicineDetailEntity.unitStr = it.specification
        }

        binding.rvSpecification.layoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.CENTER
            flexWrap = FlexWrap.WRAP
        }

        binding.rvSpecification.adapter = SpecificationAdapter(medicationUnits).apply {
            setOnItemClickListener { adapter, view, position ->
                for (i in 0 until medicationUnits.size) {
                    if (medicationUnits[i].check) {
                        medicationUnits[i].check = false
                        notifyItemChanged(i)
                        break
                    }
                }
                medicationUnits[position].check = !medicationUnits[position].check
                medicineDetailEntity.unit = medicationUnits[position].code
                medicineDetailEntity.unitStr = medicationUnits[position].specification

                notifyItemChanged(position)
            }
        }
    }

    private fun intDeleteUI() {

        binding.ivDelete.isVisible = if (supportDelete) {
            binding.ivDelete.setOnClickListener {
                Dialogs.showWhether(
                    mFragment.requireContext(),
                    content = mFragment.getString(R.string.title_share_delete),
                    confirm =  {
                        dismiss()
                        onDeleteClick?.invoke(null)
                    }
                )
            }
            true
        } else false
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        binding.etDose.removeTextChangedListener(textWatcher)
    }
}