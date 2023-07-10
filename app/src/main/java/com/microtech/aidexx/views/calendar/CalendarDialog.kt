package com.microtech.aidexx.views.calendar

import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.view.isVisible
import com.microtech.aidexx.R
import com.microtech.aidexx.common.formatToYM
import com.microtech.aidexx.common.setDebounceClickListener
import com.microtech.aidexx.common.toastShort
import com.microtech.aidexx.databinding.DialogCalendarBinding
import com.microtech.aidexx.utils.ThemeManager
import com.microtech.aidexx.views.dialog.bottom.BaseBottomPopupView
import com.microtech.aidexx.views.ruler.RulerWidget
import java.util.Calendar
import java.util.Date
import com.haibin.calendarview.Calendar as CalendarCustom

class CalendarDialog(
    val context: Context,
    onRangeSelected: (Int) -> Unit,
    onSelected: (Date, Date) -> Unit
): BaseBottomPopupView(context) {

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
        bind.calendarView.setMonthView(CustomRangeMonthView::class.java)
        bind.calendarView.scrollToCurrent()
        bind.calendarView.setSelectRangeMode()
        bind.calendarView.setOnMonthChangeListener { year, month ->
            bind.tvMonth.text = buildText(year, month)
        }
        var rangeIndex = 0
        bind.rgSwitch.setOnCheckedChangeListener { group, checkedId ->
            val today = Calendar.getInstance()
            today.time = dateToday
            when (group.checkedRadioButtonId) {
                bind.btn7.id -> {
                    rangeIndex = 1
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
                    rangeIndex = 2
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
                    rangeIndex = 3
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
            if (rangeIndex != 0) {
                onRangeSelected.invoke(rangeIndex)
                dismiss()
                return@setOnClickListener
            }
            val calendars: List<com.haibin.calendarview.Calendar> =
                bind.calendarView.selectCalendarRange
            if (calendars.size > 1) {
                val cal = Calendar.getInstance()
                cal[Calendar.HOUR_OF_DAY] = 0
                cal[Calendar.MINUTE] = 0
                cal[Calendar.SECOND] = 0
                cal[Calendar.MILLISECOND] = 0

                cal[Calendar.YEAR] = calendars[0].year
                cal[Calendar.MONTH] = calendars[0].month - 1
                cal[Calendar.DAY_OF_MONTH] = calendars[0].day
                val startDate = Date()
                startDate.time = cal.timeInMillis

                cal[Calendar.YEAR] = calendars[calendars.size - 1].year
                cal[Calendar.MONTH] = calendars[calendars.size - 1].month - 1
                cal[Calendar.DAY_OF_MONTH] = calendars[calendars.size - 1].day + 1
                val endDate = Date()
                endDate.time = cal.timeInMillis

                onSelected(startDate, endDate)
                dismiss()
            } else {
                val text =
                    if (bind.calendarView.selectedCalendar == null)
                        context.getString(R.string.start_date) else context.getString(R.string.end_date)
                Toast.makeText(context, text, Toast.LENGTH_SHORT).apply { show() }
            }
        }
        val btCancel = findViewById<RulerWidget>(R.id.bt_cancel)
        btCancel?.setOnClickListener {
            dismiss()
        }
        setKeyBackCancelable(true)
        setOutSideCancelable(true)
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

class CalendarSingleDialog(
    val context: Context,
    onSelected: (Date) -> Unit
): BaseBottomPopupView(context) {

    init {
        val bind = DialogCalendarBinding.inflate(LayoutInflater.from(context), contentContainer, true)
        bind.apply {

            calendarView.setTextColor(
                context.getColor(R.color.green_65),
                if (ThemeManager.isLight()) context.getColor(R.color.black_33) else context.getColor(R.color.white),
                if (ThemeManager.isLight()) context.getColor(R.color.gray_e6) else context.getColor(R.color.whiteAlpha30),
                if (ThemeManager.isLight()) context.getColor(R.color.black_33) else context.getColor(R.color.white),
                if (ThemeManager.isLight()) context.getColor(R.color.gray_e6) else context.getColor(R.color.whiteAlpha30)
            )

            tvMonth.text = buildText(calendarView.curYear, calendarView.curMonth)
            rgSwitch.isVisible = false
            calendarView.setOnMonthChangeListener { year, month ->
                tvMonth.text = buildText(year, month)
            }

            calendarView.setMonthView(CustomMonthView::class.java)
            calendarView.scrollToCurrent()
            calendarView.setSelectSingleMode()
            calendarView.setRange(
                bind.calendarView.curYear - 1,
                1,
                bind.calendarView.curDay,
                bind.calendarView.curYear,
                bind.calendarView.curMonth,
                bind.calendarView.curDay
            )

            btCancel.setDebounceClickListener {
                dismiss()
            }

            btOk.setDebounceClickListener {
                val calendars: CalendarCustom? = calendarView.selectedCalendar
                if (calendars != null) {
                    val calendar = Calendar.getInstance()
                    calendar[Calendar.HOUR_OF_DAY] = 0
                    calendar[Calendar.MINUTE] = 0
                    calendar[Calendar.SECOND] = 0
                    calendar[Calendar.MILLISECOND] = 0

                    calendar[Calendar.YEAR] = calendars.year
                    calendar[Calendar.MONTH] = calendars.month - 1
                    calendar[Calendar.DAY_OF_MONTH] = calendars.day
                    val selectedDate = Date()
                    selectedDate.time = calendar.timeInMillis
                    onSelected(selectedDate)
                    dismiss()
                } else {
                    context.getString(R.string.start_date).toastShort()
                }
            }
        }
        setKeyBackCancelable(true)
        setOutSideCancelable(false)
        show()
    }

    private fun buildText(year: Int, month: Int): String {

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)

        return calendar.time.formatToYM() ?: buildString {
            append(year)
            append("/")
            append(month)
        }
    }
}