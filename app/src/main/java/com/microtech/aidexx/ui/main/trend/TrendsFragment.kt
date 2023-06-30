package com.microtech.aidexx.ui.main.trend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.microtech.aidexx.base.BaseFragment
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.setDebounceClickListener
import com.microtech.aidexx.databinding.FragmentTrendBinding
import com.microtech.aidexx.utils.LanguageUnitManager
import com.microtech.aidexx.views.calendar.CalendarDialog
import com.microtech.aidexx.views.dialog.Dialogs
import java.util.Calendar
import java.util.Date

class TrendsFragment : BaseFragment<TrendsViewModel, FragmentTrendBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        updateTrends(Dialogs.DateInfo.dateLastWeek, Dialogs.DateInfo.dateToday)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrendBinding.inflate(layoutInflater)
        binding.rlDateSpace.setDebounceClickListener {
            openCalendar()
        }
        return binding.root
    }

    private fun openCalendar() {
        CalendarDialog(requireContext(), { position ->
            var startDate: Date = Dialogs.DateInfo.dateLastWeek!!
            when (position) {
                1 -> startDate = Dialogs.DateInfo.dateLastWeek!!
                2 -> startDate = Dialogs.DateInfo.dateLast14days!!
                3 -> startDate = Dialogs.DateInfo.dateLastMonth!!
            }
            updateTrends(startDate, Dialogs.DateInfo.dateToday)
        }, { startDate, endDate ->
            updateTrends(startDate, endDate)
        }).show()
    }

    private fun updateTrends(startDate: Date?, endDate: Date?) {
        if (startDate == null || endDate == null) {
            return
        }
        val formatter =
            LanguageUnitManager.getCurLanguageConf(requireContext()).dmyFormat
        binding.timeBegin.text = formatter.format(startDate)
        val calendar = Calendar.getInstance()
        calendar.time = endDate
        calendar.add(Calendar.DATE, -1)
        binding.timeEnd.text = formatter.format(calendar.time)
//        viewModel.funCgat()
    }

    companion object {
        @JvmStatic
        fun newInstance() = TrendsFragment()
    }
}