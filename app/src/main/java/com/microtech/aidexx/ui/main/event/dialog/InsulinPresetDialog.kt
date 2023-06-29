package com.microtech.aidexx.ui.main.event.dialog

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.R
import com.microtech.aidexx.common.setDebounceClickListener
import com.microtech.aidexx.common.stripTrailingZeros
import com.microtech.aidexx.common.toast
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.databinding.DialogInsulinPresetBinding
import com.microtech.aidexx.db.entity.event.InsulinDetail
import com.microtech.aidexx.db.entity.event.preset.InsulinUsrPresetEntity
import com.microtech.aidexx.ui.main.event.viewmodels.InsulinViewModel
import com.microtech.aidexx.utils.DecimalInputTextWatcher
import com.microtech.aidexx.views.dialog.Dialogs
import com.microtech.aidexx.views.dialog.bottom.BaseBottomDialog
import kotlinx.coroutines.launch

class InsulinPresetDialog(
    var mFragment: Fragment,
    private val oldInsulinEntity: InsulinDetail,
    private val supportDelete: Boolean = false, // 是否支持删除
    private val needSaveNewPreset: Boolean = false, // 是否需要保存新的预设
    private val onConfirmClick: (insulinDetailEntity: InsulinDetail) -> Unit,
    private val onDeleteClick: ((insulinDetailEntity: InsulinDetail) -> Unit)? = null
) : BaseBottomDialog(
    mFragment.requireContext()
) {

    private val vm: InsulinViewModel by mFragment.viewModels()


    lateinit var binding: DialogInsulinPresetBinding
    private lateinit var textWatcher: DecimalInputTextWatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogInsulinPresetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.apply {
            textWatcher = DecimalInputTextWatcher(etDosage, 10, 3)
            etDosage.addTextChangedListener(textWatcher)
            initPresetValue()
            intDeleteUI()
            initBtnEvent()
        }

    }

    private fun initBtnEvent() {
        binding.apply {
            btnCancel.setDebounceClickListener {
                dismiss()
            }
            viWhiteSpace.setDebounceClickListener {
                dismiss()
            }

            btnOk.setDebounceClickListener {

                val quantity = textWatcher.getDoubleValue()

                if (quantity == null) {
                    mFragment.getString(R.string.hint_enter_dose).toast()
                    return@setDebounceClickListener
                }

                oldInsulinEntity.quantity = quantity

                mFragment.lifecycleScope.launch {
                    if (needSaveNewPreset && oldInsulinEntity.insulinPresetId == "") {

                        val presetEntity = InsulinUsrPresetEntity()
                        presetEntity.name = oldInsulinEntity.name
                        presetEntity.userId = UserInfoManager.instance().userId()

                        vm.savePreset(presetEntity).collect {
                            it?.let {
                                dismiss()
                                oldInsulinEntity.insulinPresetId = presetEntity.getPresetId()
                                onConfirmClick.invoke(oldInsulinEntity)
                            }
                        }
                    } else {
                        dismiss()
                        onConfirmClick.invoke(oldInsulinEntity)
                    }
                }
            }
        }
    }


    private fun initPresetValue() {
        binding.apply {

            val info = oldInsulinEntity.tradeName.ifEmpty {
                oldInsulinEntity.manufacturer
            }

            tvInsulinName.text = buildString {
                append(oldInsulinEntity.name)
                append(if (info.isEmpty()) "" else "($info)")
            }

            if (oldInsulinEntity.quantity > 0) {
                etDosage.setText(oldInsulinEntity.quantity.stripTrailingZeros())
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
                        onDeleteClick?.invoke(oldInsulinEntity)
                    }
                )
            }
            true
        } else false
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        binding.etDosage.removeTextChangedListener(textWatcher)
    }

}

