package com.microtech.aidexx.ui.main.event.dialog

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.microtech.aidexx.BuildConfig
import com.microtech.aidexx.R
import com.microtech.aidexx.common.format
import com.microtech.aidexx.common.stripTrailingZeros
import com.microtech.aidexx.common.toast
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.data.resource.EventUnitManager
import com.microtech.aidexx.data.resource.SpecificationModel
import com.microtech.aidexx.databinding.DialogDietNewPresetBinding
import com.microtech.aidexx.db.entity.event.DietDetail
import com.microtech.aidexx.db.entity.event.preset.DietUsrPresetEntity
import com.microtech.aidexx.ui.main.event.adapter.SpecificationAdapter
import com.microtech.aidexx.ui.main.event.viewmodels.DietViewModel
import com.microtech.aidexx.utils.DecimalInputTextWatcher
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.widget.dialog.bottom.BaseBottomDialog
import kotlinx.coroutines.launch

class DietNewPresetDialog(
    var mFragment: Fragment,
    private val dietDetail: DietDetail,
    val onConfirmClick: (dietDetail: DietDetail) -> Unit,
) : BaseBottomDialog(
    mFragment.requireContext()
) {

    private val vm: DietViewModel by mFragment.viewModels()

    private val defaultWeight = 100.0
    lateinit var binding: DialogDietNewPresetBinding

    private val specificationModelList = EventUnitManager.getDietUnitList()
    private lateinit var textWatcherWeight: DecimalInputTextWatcher
    private lateinit var textWatcherCarbohydrate: DecimalInputTextWatcher
    private lateinit var textWatcherFat: DecimalInputTextWatcher
    private lateinit var textWatcherProtein: DecimalInputTextWatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogDietNewPresetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    /**
     * 是否有选中单位
     */
    private fun hasChooseUnit() = specificationModelList.find { it.check } != null

    private fun initView() {
        binding.apply {

            textWatcherWeight = DecimalInputTextWatcher(etWeight, 10, 3)
            textWatcherCarbohydrate = DecimalInputTextWatcher(etCarbohydrate, 10, 3)
            textWatcherFat = DecimalInputTextWatcher(etFat, 10, 3)
            textWatcherProtein = DecimalInputTextWatcher(etProtein, 10, 3)

            etWeight.addTextChangedListener(textWatcherWeight)
            etProtein.addTextChangedListener(textWatcherProtein)
            etFat.addTextChangedListener(textWatcherFat)
            etCarbohydrate.addTextChangedListener(textWatcherCarbohydrate)

            initUnitUi()
            initPresetValue()
            btnOk.setOnClickListener {
                try {
                    val weightValue = textWatcherWeight.getDoubleValue()
                    val proteinValue = textWatcherProtein.getDoubleValue()
                    val fatValue = textWatcherFat.getDoubleValue()
                    val carbohydrateValue = textWatcherCarbohydrate.getDoubleValue()

                    if (weightValue == null || proteinValue == null
                        || fatValue == null || carbohydrateValue == null
                        || !hasChooseUnit()
                    ) {
                        mFragment.getString(R.string.please_input).toast()
                        return@setOnClickListener
                    }

                    dietDetail.quantity = weightValue
                    dietDetail.protein = proteinValue
                    dietDetail.carbohydrate = carbohydrateValue
                    dietDetail.fat = fatValue

                    // 自定义预设需要计算100g中的含量 计算当前系数 默认100克
                    val factor =
                        (dietDetail.quantity * (specificationModelList.find { it.code == dietDetail.unit }?.ratio
                            ?: 1.0)) / 100.0

                    val presetEntity = DietUsrPresetEntity()
                    presetEntity.idx = 0
                    presetEntity.name = dietDetail.name
                    presetEntity.carbohydrate = (dietDetail.carbohydrate / factor).format()
                    presetEntity.protein = (dietDetail.protein / factor).format()
                    presetEntity.fat = (dietDetail.fat / factor).format()
                    presetEntity.userId = UserInfoManager.instance().userId()

                    mFragment.lifecycleScope.launch {
                        vm.savePreset(presetEntity).collect {
                            it?.let {
                                dismiss()
                                dietDetail.foodPresetId = presetEntity.getPresetId()
                                onConfirmClick.invoke(dietDetail)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (BuildConfig.DEBUG) TODO("崩溃了")
                    else LogUtil.xLogE("新增食物预设崩溃 e=$e")
                }
            }

            btnCancel.setOnClickListener {
                dismiss()
            }

        }

    }

    private fun initPresetValue() {
        binding.apply {
            tvCategoryName.text = dietDetail.name
            if (0.0 == dietDetail.quantity) {
                dietDetail.quantity = defaultWeight
            }
            setPresetValue()
            etWeight.addTextChangedListener {
//            setPresetValue()
            }
            etWeight.setText(
                dietDetail.quantity.stripTrailingZeros(3)
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initUnitUi() {

        for (i in 0 until specificationModelList.size) {
            specificationModelList[i].check = false
        }
        var defaultModel: SpecificationModel? = null
        specificationModelList.find { it.code == dietDetail.unit }?.let {
            defaultModel = it
            it.check = true
        } ?: let {
            specificationModelList.find { it.isDefault }?.let {
                defaultModel = it
                it.check = true
            }
        }
        defaultModel?.let {
            dietDetail.unit = it.code
            dietDetail.unitStr = it.specification
        }


        binding.rvSpecification.layoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.CENTER
            flexWrap = FlexWrap.WRAP
        }

        binding.rvSpecification.adapter = SpecificationAdapter(specificationModelList).apply {
            setOnItemClickListener { adapter, view, position ->
                for (i in 0 until specificationModelList.size) {
                    if (specificationModelList[i].check) {
                        specificationModelList[i].check = false
                        notifyItemChanged(i)
                        break
                    }
                }
                specificationModelList[position].check = !specificationModelList[position].check
                dietDetail.unit = specificationModelList[position].code
                dietDetail.unitStr = specificationModelList[position].specification
                notifyItemChanged(position)
            }
        }
    }

    private fun setPresetValue() {
        binding.apply {
            etCarbohydrate.setText(dietDetail.carbohydrate.stripTrailingZeros(3))
            etFat.setText(dietDetail.fat.stripTrailingZeros(3))
            etProtein.setText(dietDetail.protein.stripTrailingZeros(3))
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        binding.apply {
            binding.etWeight.removeTextChangedListener(textWatcherWeight)
            etProtein.removeTextChangedListener(textWatcherProtein)
            etFat.removeTextChangedListener(textWatcherFat)
            etCarbohydrate.removeTextChangedListener(textWatcherCarbohydrate)
        }
    }

}