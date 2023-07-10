package com.microtech.aidexx.ui.main.history

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseFragment
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.dp2px
import com.microtech.aidexx.common.formatToYMd
import com.microtech.aidexx.common.getStatusBarHeight
import com.microtech.aidexx.common.isSameDay
import com.microtech.aidexx.common.setDebounceClickListener
import com.microtech.aidexx.databinding.FragmentHistoryBinding
import com.microtech.aidexx.db.entity.BaseEventEntity
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.views.ScrollTab
import com.microtech.aidexx.views.calendar.CalendarSingleDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date


class HistoryFragment : BaseFragment<BaseViewModel, FragmentHistoryBinding>() {

    private val vm by activityViewModels<HistoryViewModel>()
    private var toJumpDate: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(layoutInflater)
        binding.root.setPadding(0, getStatusBarHeight() + 10.dp2px(), 0, 0 )

        initTitle()
        initScrollTab()
        initViewPager()

        EventBusManager.onReceive<BaseEventEntity>(EventBusKey.EVENT_GO_TO_HISTORY, this) {
            toJumpDate = Date(it.timestamp)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        vm.updateDate(toJumpDate ?: Date())
        toJumpDate = null
    }

    private fun initTitle() {

        binding.apply {
            lifecycleScope.launch {
                vm.curDate.collectLatest {
                    val newDate = it?:Date()
                    tvTimeSelected.text = newDate.formatToYMd()
                    if (Date().isSameDay(newDate)) {
                        btnNextDay.isClickable = false
                        btnNextDay.setColorFilter(resources.getColor(R.color.gray1,null), PorterDuff.Mode.SRC_IN)
                    } else {
                        btnNextDay.isClickable = true
                        btnNextDay.clearColorFilter()
                    }
                }
            }
            btnPreviousDay.setDebounceClickListener { vm.toPreviousDay() }
            btnNextDay.setDebounceClickListener { vm.toNextDay() }
            tvTimeSelected.setDebounceClickListener {
                CalendarSingleDialog(requireContext()) {
                    vm.updateDate(it)
                }
            }
        }
    }

    private fun initScrollTab() {
        val listTitles = mutableListOf(
            getString(R.string.history_blood_glucose),
            getString(R.string.alert_and_warnings)
        )
        binding.apply {
            stIndicator.setTitles(listTitles)
            stIndicator.setOnTabListener(object : ScrollTab.OnTabListener {
                override fun onChange(position: Int, v: View?): Boolean {
                    pageHistory.setCurrentItem(position, false)
                    return true
                }
            })
        }
    }

    private fun initViewPager() {
        binding.apply {
            pageHistory.isUserInputEnabled = false
            pageHistory.adapter = HistoryPageAdapter(requireActivity())
        }
    }

    class HistoryPageAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {

        val event by lazy { EventHistoryFragment.newInstance() }
        val alert by lazy { AlertHistoryFragment.newInstance() }

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment =
            when (position) {
                1 -> alert
                else -> event
            }
    }

    companion object {
        @JvmStatic
        fun newInstance() = HistoryFragment()
    }
}