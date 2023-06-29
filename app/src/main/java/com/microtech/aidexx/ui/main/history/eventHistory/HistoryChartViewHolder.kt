package com.microtech.aidexx.ui.main.history.eventHistory

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.CombinedData
import com.microtech.aidexx.R
import com.microtech.aidexx.common.getContext
import com.microtech.aidexx.databinding.FragmentHistoryEventBinding
import com.microtech.aidexx.ui.main.history.EventHistoryFragment
import com.microtech.aidexx.ui.main.history.HistoryViewModel
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.views.chart.GlucoseChart
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EventHistoryChartViewHolder(
    private val mFragment: EventHistoryFragment
) {

    private val vm by mFragment.activityViewModels<HistoryViewModel>()
    private val binding: FragmentHistoryEventBinding = mFragment.binding
    init {

        binding.apply {
            chart.setAndUseLifecycleOwnerEvent(mFragment)
            chart.extraParams = object: GlucoseChart.ExtraParams {
                override var outerDescriptionView: View? = null
                override var llValue: LinearLayout? = null
                override var outerDescriptionY: TextView? = null
                override var outerDescriptionUnit: TextView? = null
                override var outerDescriptionX: TextView? = null
                override var rlDescription: RelativeLayout? = null
                override var outerDescriptionU: TextView? = null
                override var goToHistory: ImageView? = null
                override var onGoToHistory: (() -> Unit)? = null
                override var curDateTv: TextView? = null

                override fun xMax(): Float = vm.xMax()
                override fun xMin(): Float = vm.xMin()
                override fun xRange(): Float = vm.xRange()
                override fun xMargin(): Float = vm.xMargin()
                override fun lowerLimit(): Float = vm.lowerLimit
                override fun upperLimit(): Float = vm.upperLimit
                override fun getYAxisStyle(): Int = 2
            }

            mFragment.lifecycleScope.launch {
                vm.chartModel.collectLatest { cm ->
                    cm?.let {
                        if (chart.data == null) {
                            chart.initData(it.combinedData)
                        } else {
                            chart.notifyChanged(true)
                        }

                        tvMaxWave.text = it.cgmWaveText
                        tvMax.text = it.cgmHighestText
                        tvMaxText.text = it.cgmHighestTitleText
                        tvMin.text = it.cgmLowestText
                        tvMinText.text = it.cgmLowestTitleText
                        tvGlucoseUnit.text = UnitManager.glucoseUnit.text

                    }
                }
            }
        }
    }
}

data class ChartModel(
    var id: Long = System.currentTimeMillis(),
    var combinedData: CombinedData,
    var hasWave: Boolean = false,
    var cgmHighestGlucose: Float = 0f,
    var cgmHighestTime: Long = 0,
    var cgmLowestGlucose: Float = 0f,
    var cgmLowestTime: Long = 0,
    var cgmHighestTitleText: String = getContext().getString(R.string.highest),
    var cgmHighestText: String = "--",
    var cgmLowestTitleText: String = getContext().getString(R.string.lowest),
    var cgmLowestText: String = "--",
    var cgmWaveText: String = "--"
)