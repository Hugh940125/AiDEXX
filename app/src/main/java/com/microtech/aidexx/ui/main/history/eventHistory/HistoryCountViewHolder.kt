package com.microtech.aidexx.ui.main.history.eventHistory

import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.R
import com.microtech.aidexx.common.dp2px
import com.microtech.aidexx.common.stripTrailingZeros
import com.microtech.aidexx.databinding.FragmentHistoryEventBinding
import com.microtech.aidexx.ui.main.history.EventHistoryFragment
import com.microtech.aidexx.ui.main.history.HistoryViewModel
import com.microtech.aidexx.utils.blankj.SpanUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EventHistoryCountViewHolder(
    private val mFragment: EventHistoryFragment
) {

    private val vm by mFragment.activityViewModels<HistoryViewModel>()
    private val binding: FragmentHistoryEventBinding = mFragment.binding

    init {

        mFragment.lifecycleScope.launch {
            vm.countModel.collectLatest { cm ->
                cm?.let {
                    binding.apply {
                        setOverViewData(tvCarbohydrateWeight, it.carb, R.string.unit_g)
                        setOverViewData(tvProteinWeight, it.protein, R.string.unit_g)
                        setOverViewData(tvFatWeight, it.fat, R.string.unit_g)
                        setOverViewData(tvExerciseTime, it.exerciseTime, R.string.unit_min)
                        setOverViewData(tvMedicationTimes, it.medicationTimes, R.string.unit_times)
                        setOverViewData(tvInsulinDose, it.insulinTotal, R.string.unit_u)
                    }
                }
            }
        }
    }

    private fun setOverViewData(textView: TextView, value: Number?, @StringRes unitResId: Int) {
        if (null == value) {
            textView.text = "--"
            textView.textSize = 30f
            return
        }
        SpanUtils.with(textView).append(value.stripTrailingZeros(3))
            .setFontSize(30.dp2px())
            .append(mFragment.getString(unitResId)).setFontSize(12.dp2px())
            .setForegroundColor(
                mFragment.resources.getColor( R.color.history_count_unit, mFragment.requireContext().theme)
            )
            .create()
            .apply {
                textView.text = this
            }
    }

}

data class CountModel(
    var id: Long = System.currentTimeMillis(),
    var dirty:Boolean = false,
    var carb: Double? = null,
    var protein: Double? = null,
    var fat: Double? = null,
    var exerciseTime: Double? = null,
    var medicationTimes: Double? = null,
    var insulinTotal: Double? = null,
)