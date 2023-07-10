package com.microtech.aidexx.ui.main.bg.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.formatToYMd
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.databinding.ActivityBloodGlucoseHistoryBinding
import com.microtech.aidexx.ui.main.bg.BgRepositoryApi
import com.microtech.aidexx.views.calendar.CalendarDialog
import com.microtech.aidexx.views.dialog.Dialogs
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

/**
 * author:ldp
 */
class BloodGlucoseHistoryActivity : BaseActivity<BaseViewModel, ActivityBloodGlucoseHistoryBinding>() {

    lateinit var historyAdapter: BloodGlucoseHistoryAdapter
    lateinit var root: View

    override fun getViewBinding(): ActivityBloodGlucoseHistoryBinding {
        return ActivityBloodGlucoseHistoryBinding.inflate(LayoutInflater.from(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
        initData()
    }

    private fun initView() {
        binding.apply {
            rlDateSpace.setOnClickListener {
                openCalendar()
            }
            ivBack.setOnClickListener {
                finish()
            }
        }
    }

    private fun initData() {
        historyAdapter = BloodGlucoseHistoryAdapter()
        binding.rvGlucoseHistory.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(this@BloodGlucoseHistoryActivity)
        }
        updateBgHistory(Dialogs.DateInfo.dateLastWeek, Dialogs.DateInfo.dateToday)
    }

    private fun openCalendar() {
        CalendarDialog(this, { position ->
            var startDate: Date = Dialogs.DateInfo.dateLastWeek!!
            when (position) {
                1 -> startDate = Dialogs.DateInfo.dateLastWeek!!
                2 -> startDate = Dialogs.DateInfo.dateLast14days!!
                3 -> startDate = Dialogs.DateInfo.dateLastMonth!!
            }
            updateBgHistory(startDate, Dialogs.DateInfo.dateToday)
        }, { startDate, endDate ->
            updateBgHistory(startDate, endDate)
        }).show()
    }

    private fun updateBgHistory(startDate: Date?, endDate: Date?) {
        if (startDate == null || endDate == null) {
            return
        }
        binding.timeBegin.text = startDate.formatToYMd()
        val calendar = Calendar.getInstance()
        calendar.time = endDate
        calendar.add(Calendar.DATE, -1)
        binding.timeEnd.text = calendar.time.formatToYMd()
        lifecycleScope.launch {
            historyAdapter.setList(
                BgRepositoryApi.getBloodGlucoseHistory(
                    startDate,
                    endDate, UserInfoManager.getCurShowUserId()
                )
            )
        }

    }

    companion object {
        private const val TAG = "BloodGlucoseHistoryActivity"
    }
}