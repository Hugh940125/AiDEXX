package com.microtech.aidexx.views.dialog.bottom

import android.content.Context
import android.view.LayoutInflater
import com.microtech.aidexx.R
import com.microtech.aidexx.utils.ThresholdManager
import com.microtech.aidexx.utils.fromGlucoseValue
import com.microtech.aidexx.views.ruler.RulerWidget

class ThresholdSelectView(context: Context, type: RulerWidget.RulerType, var onValue: ((value: Float) -> Unit)) :
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
            onValue.invoke(currentValue.fromGlucoseValue())
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