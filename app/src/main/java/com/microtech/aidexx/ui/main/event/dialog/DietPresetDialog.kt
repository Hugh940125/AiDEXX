package com.microtech.aidexx.ui.main.event.dialog

//import com.blankj.utilcode.util.KeyboardUtils
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.microtech.aidexx.R
import com.microtech.aidexx.common.setDebounceClickListener
import com.microtech.aidexx.common.stripTrailingZeros
import com.microtech.aidexx.common.toast
import com.microtech.aidexx.data.EventUnitManager
import com.microtech.aidexx.databinding.DialogDietPresetBinding
import com.microtech.aidexx.db.entity.event.DietDetail
import com.microtech.aidexx.db.repository.EventDbRepository
import com.microtech.aidexx.ui.main.event.adapter.SpecificationAdapter
import com.microtech.aidexx.utils.DecimalInputTextWatcher
import com.microtech.aidexx.widget.dialog.Dialogs
import com.microtech.aidexx.widget.dialog.bottom.BaseBottomDialog
import kotlinx.coroutines.launch

class DietPresetDialog(
    private val mFragment: Fragment,
    private val oldDietEntity: DietDetail,
    private val supportDelete: Boolean = false, // 是否支持删除
    val onConfirmClick: (dietDetailEntity: DietDetail) -> Unit,
    val onDeleteClick: ((dietDetailEntity: DietDetail?) -> Unit)? = null
) : BaseBottomDialog(
    mFragment.requireContext()
) {
    lateinit var viewBinding: DialogDietPresetBinding

    private val dietEntity: DietDetail = copyOldDietEntity()
    private val specificationModelList = EventUnitManager.getDietUnitList()


    private lateinit var textWatcherWeight: DecimalInputTextWatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DialogDietPresetBinding.inflate(LayoutInflater.from(mFragment.requireContext()))
        setContentView(viewBinding.root)
        initView()
    }

    private fun initView() {
        viewBinding.apply {

            textWatcherWeight = DecimalInputTextWatcher(etWeight, 10, 3)
            etWeight.addTextChangedListener(textWatcherWeight)

            initUnitUi()
            initPresetValue()
            initButtonClick()
            intCustomButton()
            intDeleteUI()

        }

    }

    private fun intCustomButton() {
        viewBinding.apply {
            tvCustom.setOnClickListener {
                val dietDetailEntity = copyOldDietEntity()
                mFragment.lifecycleScope.launch {
                    setPropertyName(dietDetailEntity)
                    DietNewPresetDialog(mFragment, dietDetailEntity, onConfirmClick = onConfirmClick).show()
                    dismiss()
                }
            }
        }
    }

    private fun copyOldDietEntity() =
        oldDietEntity.copy().also {
            it.id = oldDietEntity.id
            it.presetType = oldDietEntity.presetType
            it.name = oldDietEntity.name
            it.unitStr = oldDietEntity.unitStr
        }

    private suspend fun setPropertyName(dietDetailEntity: DietDetail) {

        val presetList = EventDbRepository.queryDietPresetByName(dietDetailEntity.name)

        for (i in 1 until 10000) {
            var hasFound = false
            for (item in presetList) {
                if (item.name == dietDetailEntity.name + i) {
                    hasFound = true
                    break
                }
            }
            if (!hasFound) {
                dietDetailEntity.name = oldDietEntity.name + i
                break
            }
        }
    }

    /**
     * 是否有选中单位
     */
    private fun hasChooseUnit() = specificationModelList.find { it.check } != null

    private fun initButtonClick() {

        viewBinding.apply {
            btnOk.setDebounceClickListener {
                val quantity = textWatcherWeight.getDoubleValue()

                if (null == quantity || !hasChooseUnit()) {
                    mFragment.getString(R.string.please_input).toast()
                    return@setDebounceClickListener
                }

                val maxValue = 999999.99
                if(dietEntity.carbohydrate > maxValue || dietEntity.protein > maxValue || dietEntity.fat > maxValue) {
                    mFragment.getString(R.string.overflow_max_value).toast()
                    return@setDebounceClickListener
                }

                dietEntity.name = oldDietEntity.name
                quantity.apply {
                    dietEntity.quantity = quantity
                    onConfirmClick.invoke(dietEntity)
                    dismiss()
                }
            }

            btnCancel.setOnClickListener {
                dismiss()
            }
            viWhiteSpace.setOnClickListener {
                dismiss()
            }

        }

    }

    private fun initPresetValue() {
        viewBinding.apply {
            tvCategoryName.text = oldDietEntity.name
            if (0.0 == oldDietEntity.quantity) {
                oldDietEntity.quantity = 100.0
            }
            setPresetText()
            etWeight.addTextChangedListener {
                calculatePresetValue()
            }
            etWeight.setText(
                oldDietEntity.quantity.stripTrailingZeros()
            )

            llWeight.setOnClickListener {
//                KeyboardUtils.showSoftInput(etWeight)
                etWeight.setSelection(etWeight.text.length)
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun initUnitUi() {

        for (i in 0 until specificationModelList.size) {
            specificationModelList[i].check = false
        }

        specificationModelList.find { it.code == oldDietEntity.unit }?.let {
            it.check = true
        }

        viewBinding.rvSpecification.layoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.CENTER
            flexWrap = FlexWrap.WRAP
        }

        viewBinding.rvSpecification.adapter = SpecificationAdapter(specificationModelList).apply {
            setOnItemClickListener { adapter, view, position ->
                for (i in 0 until specificationModelList.size) {
                    if (specificationModelList[i].check) {
                        specificationModelList[i].check = false
                        notifyItemChanged(i)
                        break
                    }
                }
                specificationModelList[position].check = !specificationModelList[position].check
                dietEntity.unit = specificationModelList[position].code

                calculatePresetValue()
                notifyItemChanged(position)
            }
        }
    }

    private fun calculatePresetValue() {

        val weight = textWatcherWeight.getDoubleValue() ?: 0.0

        // 如果当前语言环境不存在之前的单位 就保持不变 相当于在输入新的预设
        val hasUnit = specificationModelList.find { it.code == oldDietEntity.unit } != null

        val factor = if (hasUnit) {
            (weight / oldDietEntity.quantity) *
                    (specificationModelList[dietEntity.unit].ratio / specificationModelList[oldDietEntity.unit].ratio)
        } else 1.0

        dietEntity.carbohydrate = oldDietEntity.carbohydrate * factor
        dietEntity.protein = oldDietEntity.protein * factor
        dietEntity.fat = oldDietEntity.fat * factor

        setPresetText()
    }

    private fun setPresetText() {
        viewBinding.apply {
            tvCarbohydrate.text = dietEntity.carbohydrate.stripTrailingZeros(3)
            tvFat.text = dietEntity.fat.stripTrailingZeros(3)
            tvProtein.text = dietEntity.protein.stripTrailingZeros(3)
        }
    }


    private fun intDeleteUI() {
        viewBinding.ivDelete.isVisible = if (supportDelete) {
            viewBinding.ivDelete.setOnClickListener {
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
        viewBinding.etWeight.removeTextChangedListener(textWatcherWeight)
    }

}

