package com.microtech.aidexx.ui.main.bg.history

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.databinding.ActivityBloodGlucoseHistoryBinding
import com.microtech.aidexx.ui.main.bg.BgRepositoryApi
import com.microtech.aidexx.utils.LanguageUnitManager
import kotlinx.coroutines.launch
import java.util.*

/**
 * author:ldp
 */
class BloodGlucoseHistoryActivity : BaseActivity<BaseViewModel, ActivityBloodGlucoseHistoryBinding>() {

    lateinit var historyAdapter: BloodGlucoseHistoryAdapter
    lateinit var root: View

    private lateinit var dateLastWeek: Date
    private lateinit var dateLast14days: Date
    private lateinit var dateLastMonth: Date
    private lateinit var dateToday: Date

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
        calculateDate()
        update(dateLastWeek, dateToday)
    }

    private fun calculateDate() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, 1)
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        val timeNextZero = calendar.timeInMillis
        dateToday = Date(timeNextZero)
        dateLastWeek = Date(timeNextZero - 60 * 60 * 24 * 7 * 1000L)
        dateLast14days = Date(timeNextZero - 60 * 60 * 24 * 14 * 1000L)
        dateLastMonth = Date(timeNextZero - 60 * 60 * 24 * 30 * 1000L)
    }

    fun openCalendar() {
//        CalendarDialog.show(this as AppCompatActivity, { position ->
//            when (position) {
//                1 -> {
//                    update(dateLastWeek, dateToday)
//                }
//                2 -> {
//                    update(dateLast14days, dateToday)
//                }
//                3 -> {
//                    update(dateLastMonth, dateToday)
//                }
//            }
//        }, { startDate, endDate ->
//            update(startDate, endDate)
//
//        })
    }

    private fun update(startDate: Date, endDate: Date) {
        val formatter =
            LanguageUnitManager.languageUnitByIndex(this).dmyFormat

        binding.timeBegin.text = formatter.format(startDate)
        val calendar = Calendar.getInstance()
        calendar.time = endDate
        calendar.add(Calendar.DATE, -1)
        binding.timeEnd.text = formatter.format(calendar.time)
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