package com.microtech.aidexx.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.microtech.aidexx.ui.main.bg.BgFragment
import com.microtech.aidexx.ui.main.event.EventFragment
import com.microtech.aidexx.ui.main.history.HistoryFragment
import com.microtech.aidexx.ui.main.home.HomeFragment
import com.microtech.aidexx.ui.main.trend.TrendsFragment

/**
 *@date 2023/2/15
 *@author Hugh
 *@desc
 */
class MainViewPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {


    private val historyFragment by lazy { HistoryFragment.newInstance() }
    private val trendFragment by lazy { TrendsFragment.newInstance() }
    private val homeFragment by lazy { HomeFragment.newInstance() }
    private val bgFragment by lazy { BgFragment.newInstance() }
    private val eventFragment by lazy { EventFragment.newInstance() }

    override fun getItemCount(): Int {
        return 5
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            MainActivity.HISTORY -> historyFragment
            MainActivity.TRENDS -> trendFragment
            MainActivity.HOME -> homeFragment
            MainActivity.BG -> bgFragment
            MainActivity.EVENT -> eventFragment
            else -> homeFragment
        }
    }

    fun getItem(position: Int): Fragment? =
        when(position) {
            MainActivity.HISTORY -> historyFragment
            MainActivity.TRENDS -> trendFragment
            MainActivity.HOME -> homeFragment
            MainActivity.BG -> bgFragment
            MainActivity.EVENT -> eventFragment
            else -> null
        }


}