package com.microtech.aidexx.views.ruler

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.microtech.aidexx.R
import com.microtech.aidexx.databinding.WidgetRulerBinding
import com.microtech.aidexx.utils.ThresholdManager
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.utils.toGlucoseValue

class RulerWidget : LinearLayout {

    var type: RulerType? = null
    lateinit var viewBinding: WidgetRulerBinding
    var isFirstSetting = 0

    enum class RulerType {
        NORMAL, HYPO, HYPER
    }

    constructor(context: Context) : super(context) {
        initLayout()
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initLayout()
        init()
    }

    private fun init() {
        viewBinding.rvNumber.setBarWidth(20f)
        viewBinding.rvNumber.setBarColors(
            intArrayOf(
                resources.getColor(R.color.red),
                resources.getColor(R.color.green),
                resources.getColor(R.color.yellow)
            )
        )
        viewBinding.rvNumber.setColorStartValues(
            floatArrayOf(
                0f.toGlucoseValue(),
                ThresholdManager.DEFAULT_HYPO.toGlucoseValue(),
                ThresholdManager.DEFAULT_HYPER.toGlucoseValue()
            )
        )
        viewBinding.rvNumber.setIndicatorColorChange(booleanArrayOf(true, true, false))

        viewBinding.rvNumber.setOnValueChangedListener { value ->
            type?.let { setCurrentValue(value, it) }
        }
        viewBinding.tvNumber.text = UnitManager.formatterUnitByIndex().format(5.0F)
        viewBinding.tvUnit.text = ""
    }

    private fun initLayout() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val inflate = inflater.inflate(R.layout.widget_ruler, this, true)
        viewBinding = WidgetRulerBinding.bind(inflate)
    }


    fun setType(type: RulerType, default: Float) {
        this.type = type
        if (type == RulerType.NORMAL) {
            viewBinding.tvNumber.text = null
            viewBinding.tvUnit.text = null
        }

        viewBinding.rvNumber.setColorStartValues(
            floatArrayOf(
                0f.toGlucoseValue(),
                ThresholdManager.hypo.toGlucoseValue(),
                ThresholdManager.hyper.toGlucoseValue()
            )
        )

        val unit = when (UnitManager.glucoseUnit) {
            UnitManager.GlucoseUnit.MMOL_PER_L -> 0.1f
            UnitManager.GlucoseUnit.MG_PER_DL -> 1f
        }

        when (type) {
            RulerType.NORMAL -> {
                val result = fitRange(default, 2.0F, 30.0F)
                viewBinding.rvNumber.setIndicatorColorChange(null)
                viewBinding.rvNumber.setStickyIndex(-1)
                viewBinding.rvNumber.setValue(
                    2f.toGlucoseValue(),
                    30f.toGlucoseValue(),
                    result,
                    unit,
                    10
                )
                setCurrentValue(result, type)
            }
            RulerType.HYPO -> {
                val result = fitRange(default, 3.0f * 18f, 5.0f * 18f)
                viewBinding.rvNumber.setIndicatorColorChange(booleanArrayOf(true, false, true))
                viewBinding.rvNumber.setStickyIndex(1)
                viewBinding.rvNumber.setValue(
                    (3.0f * 18f).toGlucoseValue(),
                    (5.0f * 18f).toGlucoseValue(),
                    result,
                    unit,
                    10
                )
                setCurrentValue(result, type)
            }
            RulerType.HYPER -> {
                val result = fitRange(default, 7.0f * 18f, 25.0f * 18f)
                viewBinding.rvNumber.setIndicatorColorChange(booleanArrayOf(true, false, true))
                viewBinding.rvNumber.setStickyIndex(2)
                viewBinding.rvNumber.setValue(
                    (7.0f * 18f).toGlucoseValue(),
                    (25.0f * 18f).toGlucoseValue(),
                    result,
                    unit,
                    10
                )
                setCurrentValue(result, type)
            }
        }
    }

    private fun fitRange(default: Float, start: Float, end: Float): Float {
        val result = if (default in start..end)
            default.toGlucoseValue() else {
            if (default < start) {
                start.toGlucoseValue()
            } else {
                end.toGlucoseValue()
            }
        }
        return result
    }

    fun getCurrentValue(): Float {
        if (viewBinding.tvNumber.text.isNullOrBlank()) {
            return 0f;
        }
        return viewBinding.rvNumber.currentValue
    }


    private fun setCurrentValue(
        value: Float,
        type: RulerType,
    ) {
        if (!(type == RulerType.NORMAL && isFirstSetting < 2)) {
            val unit = UnitManager.glucoseUnit.text
            viewBinding.tvNumber.text = UnitManager.formatterUnitByIndex().format(value)
            viewBinding.tvUnit.text = unit
        }
        isFirstSetting++
        val color = viewBinding.rvNumber.indicatorLineColor
        viewBinding.tvNumber.setTextColor(color)
        viewBinding.tvArrow.setTextColor(color)
        viewBinding.tvUnit.setTextColor(color)
    }
}