package com.microtech.aidexx.widget.calendar

import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.microtech.aidexx.R
import com.microtech.aidexx.databinding.DialogCalendarBinding
import com.microtech.aidexx.utils.ThemeManager
import com.microtech.aidexx.widget.dialog.bottom.BaseBottomPopupView
import com.microtech.aidexx.widget.ruler.RulerWidget
import java.util.*

class CalendarDialog(val context: Context, onRangeSelected: (Int) -> Unit, onSelected: (Date, Date) -> Unit) :
    BaseBottomPopupView(context) {

    init {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, 1)
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        val nextZero = calendar.timeInMillis
        val dateToday = Date(nextZero)
        val last7days = Date(nextZero - 60 * 60 * 24 * 7 * 1000L)
        val last14days = Date(nextZero - 60 * 60 * 24 * 14 * 1000L)
        val last30days = Date(nextZero - 60 * 60 * 24 * 30 * 1000L)
        val view = LayoutInflater.from(context)
            .inflate(R.layout.dialog_calendar, contentContainer)
        val bind = DialogCalendarBinding.bind(view)
        bind.calendarView.setSelectSingleMode()
        bind.calendarView.setTextColor(
            context.getColor(R.color.green_65),
            if (ThemeManager.isLight()) context.getColor(R.color.black_33) else context.getColor(R.color.white),
            if (ThemeManager.isLight()) context.getColor(R.color.gray_e6) else context.getColor(R.color.whiteAlpha30),
            if (ThemeManager.isLight()) context.getColor(R.color.black_33) else context.getColor(R.color.white),
            if (ThemeManager.isLight()) context.getColor(R.color.gray_e6) else context.getColor(R.color.whiteAlpha30)
        )
        bind.tvMonth.text = buildText(bind.calendarView.curYear, bind.calendarView.curMonth)
        bind.calendarView.setRange(
            bind.calendarView.curYear - 1,
            1,
            bind.calendarView.curDay,
            bind.calendarView.curYear,
            bind.calendarView.curMonth,
            bind.calendarView.curDay
        )
        bind.calendarView.scrollToCurrent()
        bind.calendarView.setSelectRangeMode()
        bind.calendarView.setOnMonthChangeListener { year, month ->
            bind.tvMonth.text = buildText(year, month)
        }
        bind.rgSwitch.setOnCheckedChangeListener { group, checkedId ->
            val today = Calendar.getInstance()
            today.time = dateToday
            when (group.checkedRadioButtonId) {
                bind.btn7.id -> {
                    val days = Calendar.getInstance()
                    days.time = last7days
                    bind.calendarView.setSelectStartCalendar(
                        days.get(Calendar.YEAR),
                        days.get(Calendar.MONTH) + 1, days.get(Calendar.DAY_OF_MONTH)
                    )
                    bind.calendarView.setSelectEndCalendar(
                        today.get(Calendar.YEAR),
                        today.get(Calendar.MONTH) + 1, today.get(Calendar.DAY_OF_MONTH) - 1
                    )
                }
                bind.btn14.id -> {
                    val days = Calendar.getInstance()
                    days.time = last14days
                    bind.calendarView.setSelectStartCalendar(
                        days.get(Calendar.YEAR),
                        days.get(Calendar.MONTH) + 1, days.get(Calendar.DAY_OF_MONTH)
                    )
                    bind.calendarView.setSelectEndCalendar(
                        today.get(Calendar.YEAR),
                        today.get(Calendar.MONTH) + 1, today.get(Calendar.DAY_OF_MONTH) - 1
                    )
                }
                bind.btn30.id -> {
                    val days = Calendar.getInstance()
                    days.time = last30days
                    bind.calendarView.setSelectStartCalendar(
                        days.get(Calendar.YEAR),
                        days.get(Calendar.MONTH) + 1, days.get(Calendar.DAY_OF_MONTH)
                    )
                    bind.calendarView.setSelectEndCalendar(
                        today.get(Calendar.YEAR),
                        today.get(Calendar.MONTH) + 1, today.get(Calendar.DAY_OF_MONTH) - 1
                    )
                }
            }
        }
        val btOk = findViewById<RulerWidget>(R.id.bt_ok)
        btOk?.setOnClickListener {
            val calendars = bind.calendarView.selectedCalendar
            if (calendars != null) {
                val result = Calendar.getInstance()
                result[Calendar.HOUR_OF_DAY] = 0
                result[Calendar.MINUTE] = 0
                result[Calendar.SECOND] = 0
                result[Calendar.MILLISECOND] = 0

                result[Calendar.YEAR] = calendars.year
                result[Calendar.MONTH] = calendars.month - 1
                result[Calendar.DAY_OF_MONTH] = calendars.day
                val start = Date()
                start.time = result.timeInMillis
//                onSelected(start)
                dismiss()
            } else {
                val text = "请选择日期"
                Toast.makeText(context, text, Toast.LENGTH_SHORT).apply { show() }
            }
        }
        val btCancel = findViewById<RulerWidget>(R.id.bt_cancel)
        btCancel?.setOnClickListener {
            dismiss()
        }
        setKeyBackCancelable(true)
        setOutSideCancelable(false)
        show()
    }

    private fun buildText(year: Int, month: Int): String {
        return buildString {
            append(year)
            append("/")
            append(month)
        }
    }
}