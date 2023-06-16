package com.microtech.aidexx.ui.main.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.microtech.aidexx.base.BaseFragment
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.databinding.FragmentHistoryEventBinding
import com.microtech.aidexx.ui.main.history.eventHistory.EventHistoryChartViewHolder
import com.microtech.aidexx.ui.main.history.eventHistory.EventHistoryCountViewHolder
import com.microtech.aidexx.ui.main.history.eventHistory.EventHistoryProportionViewHolder
import com.microtech.aidexx.ui.main.history.eventHistory.EventHistoryRecordsViewHolder

class EventHistoryFragment : BaseFragment<BaseViewModel, FragmentHistoryEventBinding>() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryEventBinding.inflate(layoutInflater)
        initViewHolders()
        return binding.root
    }

    private fun initViewHolders() {
        EventHistoryCountViewHolder(this)
        EventHistoryChartViewHolder(this)
        EventHistoryProportionViewHolder(this)
        EventHistoryRecordsViewHolder(this)
    }

    companion object {
        @JvmStatic
        fun newInstance() = EventHistoryFragment()
    }
}