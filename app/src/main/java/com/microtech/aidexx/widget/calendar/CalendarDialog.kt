package com.microtech.aidexx.widget.calendar

import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.microtech.aidexx.R
import com.microtech.aidexx.databinding.DialogCalendarBinding
import com.microtech.aidexx.widget.dialog.bottom.BaseBottomPopupView
import com.microtech.aidexx.widget.ruler.RulerWidget
import java.util.*

class CalendarDialog(context: Context, type: RulerWidget.RulerType, var onValue: ((value: String) -> Unit)) :
    BaseBottomPopupView(context) {

    fun show(context: AppCompatActivity, startDate: Date, onSelected: (Date) -> Unit) {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.dialog_calendar, contentContainer)
        val bind = DialogCalendarBinding.bind(view)
        bind.calendarView.setSelectSingleMode()
        bind.tvMonth.text = buildText(bind.calendarView.curYear, bind.calendarView.curMonth)
        val begin = Calendar.getInstance()
        begin.time = startDate
        bind.calendarView.setRange(
            begin.get(Calendar.YEAR),
            begin.get(Calendar.MONTH) + 1,
            begin.get(Calendar.DAY_OF_MONTH),
            bind.calendarView.curYear,
            bind.calendarView.curMonth,
            bind.calendarView.curDay
        )
        bind.calendarView.scrollToCurrent()
        bind.calendarView.setOnMonthChangeListener { year, month ->
            bind.tvMonth.text = buildText(year, month)
        }
        val btOk = findViewById<RulerWidget>(R.id.bt_ok)
        btOk?.setOnClickListener {
            val calendars = bind.calendarView.selectedCalendar
            if (calendars != null) {
                val calendar = Calendar.getInstance()
                calendar[Calendar.HOUR_OF_DAY] = 0
                calendar[Calendar.MINUTE] = 0
                calendar[Calendar.SECOND] = 0
                calendar[Calendar.MILLISECOND] = 0

                calendar[Calendar.YEAR] = calendars.year
                calendar[Calendar.MONTH] = calendars.month - 1
                calendar[Calendar.DAY_OF_MONTH] = calendars.day
                val start = Date()
                start.time = calendar.timeInMillis
                onSelected(start)
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
    }

    private fun buildText(year: Int, month: Int): String {
        return buildString {
            append(year)
            append("/")
            append(month)
        }
    }
}