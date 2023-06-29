package com.microtech.aidexx.ui.main.history.eventHistory

import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.R
import com.microtech.aidexx.common.setDebounceClickListener
import com.microtech.aidexx.common.setScale
import com.microtech.aidexx.databinding.FragmentHistoryEventBinding
import com.microtech.aidexx.ui.main.history.EventHistoryFragment
import com.microtech.aidexx.ui.main.history.HistoryViewModel
import com.microtech.aidexx.utils.ThresholdManager
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.utils.toGlucoseValue
import com.microtech.aidexx.views.HollowPieChartView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EventHistoryProportionViewHolder(
    private val mFragment: EventHistoryFragment
) {

    private val vm by mFragment.activityViewModels<HistoryViewModel>()
    private val binding: FragmentHistoryEventBinding = mFragment.binding
    init {
        mFragment.lifecycleScope.launch {
            vm.proportionModel.collectLatest { pm ->
                pm?.let {
                    binding.apply {
                        pvHigh.setData(
                            HollowPieChartView.PieceDataHolder(
                                it.highCountPercent.toFloat(),
                                Color.parseColor("#FFF0BE5B"),
                                ContextCompat.getColor(mFragment.requireContext(), R.color.white_f5),
                                it.highCountPercentStr
                            )
                        )
                        pvNormal.setData(
                            HollowPieChartView.PieceDataHolder(
                                it.normalCountPercent.toFloat(),
                                ContextCompat.getColor(mFragment.requireContext(), R.color.colorGlucoseNormal),
                                ContextCompat.getColor(mFragment.requireContext(), R.color.white_f5),
                                it.normalCountPercentStr
                            )
                        )
                        pvLow.setData(
                            HollowPieChartView.PieceDataHolder(
                                it.lowCountPercent.toFloat(),
                                Color.parseColor("#FFE15D4D"),
                                ContextCompat.getColor(mFragment.requireContext(), R.color.white_f5),
                                it.lowCountPercentStr
                            )
                        )

                        val scale =
                            if (UnitManager.glucoseUnit == UnitManager.GlucoseUnit.MMOL_PER_L) 1 else 0

                        tvHighRange.text = buildString {
                            append(">")
                            append(ThresholdManager.hyper.toGlucoseValue().setScale(scale))
                            append(UnitManager.glucoseUnit.text)
                        }
                        tvNormalRange.text = buildString {
                            append(ThresholdManager.hypo.toGlucoseValue().setScale(scale))
                            append("-")
                            append(ThresholdManager.hyper.toGlucoseValue().setScale(scale))
                            append(UnitManager.glucoseUnit.text)
                        }
                        tvLowRange.text = buildString {
                            append("<")
                            append(ThresholdManager.hypo.toGlucoseValue().setScale(scale))
                            append( UnitManager.glucoseUnit.text)
                        }

                        tvHighPercent.text = it.highCountPercentStr
                        tvNormalPercent.text = it.normalCountPercentStr
                        tvLowPercent.text = it.lowCountPercentStr

                        if (it.totalCount > 0) {
                            tvLowGlucoseTime.text = it.lowCountMinutesStr
                            tvHighGlucoseTime.text = it.highCountMinutesStr
                            tvNormalGlucoseTime.text = it.normalCountMinutesStr
                        }

                        collapsePercentDetail()

                        flCollapse.setDebounceClickListener {
                            if (llPercentDetail.visibility != View.VISIBLE) {
                                expandPercentDetail()
                            } else {
                                collapsePercentDetail()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun collapsePercentDetail() {
        binding.apply {
            llPercentDetail.visibility = View.GONE
            ivCollapse.setImageDrawable(
                ResourcesCompat.getDrawable(
                    mFragment.resources,
                    R.drawable.ic_expand,
                    null
                )
            )
        }
    }

    private fun expandPercentDetail() {
        binding.apply {
            llPercentDetail.visibility = View.VISIBLE
            ivCollapse.setImageDrawable(
                ResourcesCompat.getDrawable(
                    mFragment.resources,
                    R.drawable.ic_collapse,
                    null
                )
            )
        }
    }


}

data class ProportionModel(
    var id: Long = System.currentTimeMillis(),
    var dirty:Boolean = false,
    var highCount: Int = 0,
    var normalCount: Int = 0,
    var lowCount: Int = 0,
    var totalCount: Int = 0,
    var highCountPercent: Int = 0,
    var highCountPercentStr: String = "--",
    var highCountMinutesStr: String = "--",
    var normalCountPercent: Int = 0,
    var normalCountPercentStr: String = "--",
    var normalCountMinutesStr: String = "--",
    var lowCountPercent: Int = 0,
    var lowCountPercentStr: String = "--",
    var lowCountMinutesStr: String = "--",
)