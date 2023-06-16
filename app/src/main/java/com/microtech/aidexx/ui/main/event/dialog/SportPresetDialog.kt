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
import com.microtech.aidexx.databinding.DialogSportPresetBinding
import com.microtech.aidexx.db.entity.event.ExerciseDetail
import com.microtech.aidexx.db.entity.event.preset.SportUsrPresetEntity
import com.microtech.aidexx.ui.main.event.adapter.SpecificationAdapter
import com.microtech.aidexx.ui.main.event.viewmodels.SportViewModel
import com.microtech.aidexx.utils.DecimalInputTextWatcher
import com.microtech.aidexx.widget.dialog.Dialogs
import com.microtech.aidexx.widget.dialog.bottom.BaseBottomDialog
import kotlinx.coroutines.launch

class SportPresetDialog(
    var mFragment: Fragment,
    private val sportDetailEntity: ExerciseDetail,
    private val supportDelete: Boolean = false, // 是否支持删除
    private val needSaveNewPreset: Boolean = false, // 是否需要保存新的预设
    private val onConfirmClick: (sportDetailEntity: ExerciseDetail) -> Unit,
    private val onDeleteClick: ((sportDetailEntity: ExerciseDetail?) -> Unit)? = null
) : BaseBottomDialog(
    mFragment.requireContext()
) {

    private val vm: SportViewModel by mFragment.viewModels()


    private val timeUnits = EventUnitManager.getTimeUnitList()
    lateinit var binding: DialogSportPresetBinding
    private lateinit var textWatcher: DecimalInputTextWatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogSportPresetBinding.inflate(LayoutInflater.from(mFragment.requireContext()))
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.apply {

            textWatcher = DecimalInputTextWatcher(etDuration, 10, 3)
            etDuration.addTextChangedListener(textWatcher)

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
                val duration = textWatcher.getDoubleValue()
                if (duration == null) {
                    mFragment.getString(R.string.hint_exercise_time).toast()
                    return@setDebounceClickListener
                }

                sportDetailEntity.quantity = duration

                mFragment.lifecycleScope.launch {
                    if (needSaveNewPreset && sportDetailEntity.exercisePresetId == "") {

                        val presetEntity = SportUsrPresetEntity()
                        presetEntity.name = sportDetailEntity.name
                        presetEntity.userId = UserInfoManager.instance().userId()

                        vm.savePreset(presetEntity).collect {
                            it?.let {
                                dismiss()
                                sportDetailEntity.exercisePresetId = presetEntity.getPresetId()
                                onConfirmClick.invoke(sportDetailEntity)
                            }
                        }
                    } else {
                        dismiss()
                        onConfirmClick.invoke(sportDetailEntity)
                    }
                }

            }

        }
    }

    private fun initPresetValue() {

        binding.apply {
            tvCategoryName.text = sportDetailEntity.name
            if (0.0 != sportDetailEntity.quantity) {
                etDuration.setText(
                    sportDetailEntity.quantity.stripTrailingZeros()
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initUnitUi() {

        for (i in 0 until timeUnits.size) {
            timeUnits[i].check = false
        }

        var defaultModel: SpecificationModel? = null
        timeUnits.find { it.code == sportDetailEntity.unit }?.let {
            defaultModel = it
            it.check = true
        } ?:let {
            timeUnits.find { it.isDefault }?.let {
                defaultModel = it
                it.check = true
            }
        }
        defaultModel?.let {
            sportDetailEntity.unit = it.code
            sportDetailEntity.unitStr = it.specification
        }

        binding.rvSpecification.layoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.CENTER
            flexWrap = FlexWrap.WRAP
        }

        binding.rvSpecification.adapter = SpecificationAdapter(timeUnits).apply {
            setOnItemClickListener { adapter, view, position ->
                for (i in 0 until timeUnits.size) {
                    if (timeUnits[i].check) {
                        timeUnits[i].check = false
                        notifyItemChanged(i)
                        break
                    }
                }
                timeUnits[position].check = !timeUnits[position].check
                sportDetailEntity.unit = timeUnits[position].code
                sportDetailEntity.unitStr = timeUnits[position].specification

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
                    confirm = {
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
        binding.etDuration.removeTextChangedListener(textWatcher)
    }

}