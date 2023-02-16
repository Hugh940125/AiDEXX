package com.microtech.aidexx.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.microtech.aidexx.ui.main.bg.BgFragment
import com.microtech.aidexx.ui.main.event.EventFragment
import com.microtech.aidexx.ui.main.history.HistoryFragment
import com.microtech.aidexx.ui.main.home.HomeFragment
import com.microtech.aidexx.ui.main.trend.TrendFragment

/**
 *@date 2023/2/15
 *@author Hugh
 *@desc
 */
class MainViewPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 5
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HistoryFragment.newInstance()
            1 -> TrendFragment.newInstance()
            2 -> HomeFragment.newInstance()
            3 -> BgFragment.newInstance()
            4 -> EventFragment.newInstance()
            else -> {
                HomeFragment.newInstance()
            }
        }
    }
}