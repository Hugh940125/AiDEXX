package com.microtech.aidexx.widget.dialog.bottom

import android.content.Context
import android.view.LayoutInflater
import com.microtech.aidexx.R
import com.microtech.aidexx.utils.ThresholdManager
import com.microtech.aidexx.widget.dialog.lib.util.toGlucoseStringWithUnit
import com.microtech.aidexx.widget.ruler.RulerWidget

class MethodSelectView(context: Context, type: RulerWidget.RulerType, var onValue: ((value: String) -> Unit)) :
    BaseBottomPopupView(context) {

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.layout_ruler_dialog, contentContainer)
        val rulerWidget = findViewById<RulerWidget>(R.id.rw_number) as RulerWidget
        rulerWidget.setType(
            type, if (type == RulerWidget.RulerType.HYPO) ThresholdManager.hypo
            else ThresholdManager.hyper
        )
        val btOk = findViewById<RulerWidget>(R.id.bt_ok)
        btOk?.setOnClickListener {
            val currentValue = rulerWidget.getCurrentValue()
            if (type == RulerWidget.RulerType.HYPO) ThresholdManager.hypo = currentValue
            else ThresholdManager.hyper = currentValue
            onValue.invoke(ThresholdManager.hypo.toGlucoseStringWithUnit())
            dismiss()
        }
        val btCancel = findViewById<RulerWidget>(R.id.bt_cancel)
        btCancel?.setOnClickListener {
            dismiss()
        }
        setKeyBackCancelable(true)
        setOutSideCancelable(true)
    }
}