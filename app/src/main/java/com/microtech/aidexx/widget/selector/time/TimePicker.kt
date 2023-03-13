package com.microtech.aidexx.widget.selector.time

import android.content.Context
import android.view.View
import android.widget.TextView
import com.microtech.aidexx.R
import java.util.*

class TimePicker(val context: Context) {

    lateinit var timePicker: TimePickerView

    fun pick(callBack: (date: Date) -> Unit) {
        /**
         * 注意事项：
         * 1.自定义布局中，id为 optionspicker 或者 timepicker 的布局以及其子控件必须要有，否则会报空指针.
         * 具体可参考demo 里面的两个自定义layout布局。
         * 2.因为系统Calendar的月份是从0-11的,所以如果是调用Calendar的set方法来设置时间,月份的范围也要是从0-11
         * setRangDate方法控制起始终止时间(如果不设置范围，则使用默认时间1900-2100年，此段代码可注释)
         */
        val startDate: Calendar = Calendar.getInstance()
        startDate.set(
            startDate[Calendar.YEAR] - 1,
            startDate[Calendar.MONTH],
            startDate[Calendar.DAY_OF_MONTH],
            startDate[Calendar.HOUR_OF_DAY],
            startDate[Calendar.MINUTE]
        )
        val endDate = Calendar.getInstance()
        timePicker = TimePickerBuilder(
            context
        ) { date, _ -> //选中事件回调
            callBack.invoke(date)
        }
            .setDate(endDate)
            .setRangDate(startDate, endDate)
            .setLayoutRes(
                R.layout.layout_time_pick
            ) { v ->
                val tvSubmit = v.findViewById<View>(R.id.tv_finish) as TextView
                val ivCancel = v.findViewById<View>(R.id.tv_cancel) as TextView
                tvSubmit.setOnClickListener {
                    timePicker.returnData()
                    timePicker.dismiss()
                }
                ivCancel.setOnClickListener { timePicker.dismiss() }
            }
            .setTextColorCenter(context.getColor(R.color.green_65))
            .setContentTextSize(20)
            .setType(booleanArrayOf(true, true, true, true, true, false))
            .setLabel("", "", "", "", "", "")
            .setLineSpacingMultiplier(1.2f)
            .setTextXOffset(0, 0, 0, 0, 0, 0)
            .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
            .setDividerColor(context.getColor(R.color.green_65))
            .setBgColor(context.getColor(R.color.bg_item_color))
            .build()
        timePicker.show()
    }

}